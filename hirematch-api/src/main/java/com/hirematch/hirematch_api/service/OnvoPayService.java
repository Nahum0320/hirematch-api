package com.hirematch.hirematch_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hirematch.hirematch_api.entity.Product;
import com.hirematch.hirematch_api.entity.Transaction;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.ProductRepository;
import com.hirematch.hirematch_api.repository.TransactionRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OnvoPayService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserStatsService userStatsService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${onvopay.api.url:https://api.onvopay.com}")
    private String apiUrl;
    
    @Value("${onvopay.api.secret-key:onvo_test_secret_key_BKmmZtDeLSyf-_O7FAg0BEbm0lm8EbDEYfcOv_QbNhLhubTC9tGxfidzd6SX0r4-uL9Msjbzo3-5ddPw5kkK2w}")
    private String secretKey;
    
    @Value("${onvopay.api.public-key:onvo_test_publishable_key__JO8hXwNYgXACHBykeYCDbixxXaixvMU3bpHBh45k32AT2_B_uBAVM62oFikBivjMyEmG932YJUDPqrJBkglNw}")
    private String publicKey;
    
    /**
     * Crear una sesión de pago con Onvo Pay
     */
    public Map<String, Object> createCheckoutSession(Long usuarioId, Long productId, String successUrl, String cancelUrl) throws Exception {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
        // Crear la transacción pendiente
        Transaction transaction = new Transaction();
        transaction.setUsuario(usuario);
        transaction.setProduct(product);
        transaction.setAmount(product.getPrice());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("PENDING");
        transaction = transactionRepository.save(transaction);
        
        // Preparar la petición para Onvo Pay
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Metadatos de la transacción
        Map<String, String> metadata = new HashMap<>();
        metadata.put("transactionId", transaction.getId().toString());
        metadata.put("usuarioId", usuarioId.toString());
        metadata.put("productId", productId.toString());
        metadata.put("productType", product.getType().toString());
        
        // Estructura correcta según documentación de Onvo Pay
        Map<String, Object> requestBody = new HashMap<>();
        
        // Información del cliente
        requestBody.put("customerName", usuario.getNombre() + " " + usuario.getApellido());
        requestBody.put("customerEmail", usuario.getEmail());
        
        // URLs de redirección
        requestBody.put("redirectUrl", successUrl);
        requestBody.put("cancelUrl", cancelUrl);
        
        // Para lineItems, la documentación muestra un array vacío
        // Vamos a crear un objeto con el producto de Onvo Pay
        Map<String, Object> lineItem = new HashMap<>();
        // Utilizamos el ID del producto en Onvo Pay en lugar de propiedades directas
        lineItem.put("priceId", product.getOnvoPayProductId());
        lineItem.put("quantity", 1);
        
        requestBody.put("lineItems", List.of(lineItem));
        requestBody.put("metadata", metadata);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        // El endpoint correcto según la documentación
        ResponseEntity<Map> response = restTemplate.postForEntity(
            apiUrl + "/v1/checkout/sessions/one-time-link",
            request,
            Map.class
        );
        
        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> responseBody = response.getBody();
            transaction.setTransactionId(responseBody.get("id").toString());
            transaction.setTransactionData(objectMapper.writeValueAsString(responseBody));
            transactionRepository.save(transaction);
            return responseBody;
        } else {
            throw new RuntimeException("Error al crear la sesión de pago: " + response.getBody());
        }
    }
    
    /**
     * Verificar el estado de una transacción
     */
    public Map<String, Object> checkTransactionStatus(String transactionId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            apiUrl + "/v1/checkout/sessions/" + transactionId,
            HttpMethod.GET,
            request,
            Map.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Error al verificar la transacción: " + response.getBody());
        }
    }
    
    /**
     * Procesar una transacción completada
     */
    @Transactional
    public void processCompletedTransaction(String transactionId) throws Exception {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
            
        // Verificar el estado de la transacción con Onvo Pay
        Map<String, Object> paymentInfo = checkTransactionStatus(transactionId);
        
        // Si el pago fue exitoso
        if ("completed".equals(paymentInfo.get("status"))) {
            transaction.setStatus("COMPLETED");
            transaction.setTransactionData(objectMapper.writeValueAsString(paymentInfo));
            transactionRepository.save(transaction);
            
            // Aplicar el producto comprado
            applyPurchase(transaction);
        } else {
            transaction.setStatus("FAILED");
            transaction.setTransactionData(objectMapper.writeValueAsString(paymentInfo));
            transactionRepository.save(transaction);
        }
    }
    
    /**
     * Aplicar los beneficios de una compra
     */
    private void applyPurchase(Transaction transaction) {
        Usuario usuario = transaction.getUsuario();
        Product product = transaction.getProduct();
        
        switch (product.getType()) {
            case LIKE_PACKAGE:
                userStatsService.addLikes(usuario.getUsuarioId(), product.getQuantity());
                break;
                
            case SUPERLIKE_PACKAGE:
                userStatsService.addSuperlikes(usuario.getUsuarioId(), product.getQuantity());
                break;
                
            case SUBSCRIPTION:
                userStatsService.activateSubscription(
                    usuario.getUsuarioId(),
                    product.getName(),
                    product.getDurationInDays()
                );
                break;
        }
    }
}