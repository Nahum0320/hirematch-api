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
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TiloPayService {

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
    
    @Value("${tilopay.api.url:https://app.tilopay.com/api/v1}")
    private String apiUrl;
    
    @Value("${tilopay.api.user:8Zcdzr}")
    private String apiUser;
    
    @Value("${tilopay.api.password:DBpKhG}")
    private String apiPassword;

    @Value("${tilopay.api.key:8387-7075-8927-5775-3356}")
    private String apiKey;
    
    // Almacén en memoria del token (en producción usar una solución más robusta como Redis)
    private String authToken;
    private LocalDateTime tokenExpiration;
    
    /**
     * Obtener o renovar el token de autenticación
     */
    private String getAuthToken() throws Exception {
        // Si ya tenemos un token y no ha expirado, lo reutilizamos
        if (authToken != null && tokenExpiration != null && LocalDateTime.now().isBefore(tokenExpiration)) {
            return authToken;
        }
        
        try {
            // URL para la solicitud de autenticación
            String url = apiUrl + "/login";
            
            // Crear objeto para la solicitud JSON
            String jsonBody = "{\"apiuser\":\"" + apiUser + "\",\"password\":\"" + apiPassword + "\"}";
            
            // Crear cliente HTTP
            java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder().build();
            
            // Construir la solicitud
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
            
            // Imprimir detalles de la solicitud
            System.out.println("=== TILOPAY AUTH REQUEST ===");
            System.out.println("URL: " + url);
            System.out.println("Request Body: " + jsonBody);
            
            // Enviar la solicitud
            java.net.http.HttpResponse<String> response = httpClient.send(
                request, 
                java.net.http.HttpResponse.BodyHandlers.ofString()
            );
            
            // Imprimir respuesta
            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            System.out.println("=========================");
            
            if (response.statusCode() == 200) {
                // Parsear la respuesta
                Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
                // CORRECCIÓN: TiloPay devuelve "access_token" en lugar de "token"
                authToken = (String) responseMap.get("access_token");
                
                // Validar que el token no sea null
                if (authToken == null || authToken.isEmpty()) {
                    throw new RuntimeException("Token recibido es nulo o vacío. Response: " + response.body());
                }
                
                System.out.println("Token obtenido exitosamente: " + authToken.substring(0, Math.min(20, authToken.length())) + "...");
                tokenExpiration = LocalDateTime.now().plusHours(24);
                return authToken;
            } else {
                throw new RuntimeException("Error al obtener el token: " + response.statusCode() + " " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Error en la autenticación: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Crear un link de pago con TiloPay
     */
    public Map<String, Object> createPaymentLink(Long usuarioId, Long productId, String callbackUrl, String webhookUrl) throws Exception {
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
        
        // Obtener el token de autenticación
        String token = getAuthToken();
        
        // Preparar la petición para TiloPay
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token); // CAMBIO: Bearer con B mayúscula
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", apiKey);
        requestBody.put("amount", product.getPrice().toString());
        requestBody.put("currency", "CRC");
        requestBody.put("reference", transaction.getId().toString());
        requestBody.put("type", 1);
        requestBody.put("description", product.getDescription());
        requestBody.put("client", usuario.getNombre() + " " + usuario.getApellido());
        requestBody.put("callback_url", callbackUrl);
        requestBody.put("webhook_url", webhookUrl);
        
        // Log de la petición
        System.out.println("=== TILOPAY CREATE PAYMENT REQUEST ===");
        System.out.println("URL: " + apiUrl + "/createLinkPayment");
        System.out.println("Token: " + token.substring(0, Math.min(20, token.length())) + "...");
        System.out.println("Request Body: " + objectMapper.writeValueAsString(requestBody));
        System.out.println("====================================");
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                apiUrl + "/createLinkPayment",
                request,
                Map.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + objectMapper.writeValueAsString(response.getBody()));
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
    
            // TiloPay devuelve "id" en lugar de "tilopayLinkId" y "url" en lugar de "linkUrl"
            Object linkId = responseBody.get("id");
            if (linkId == null) {
                throw new RuntimeException("No se recibió el ID del link de pago. Response: " + objectMapper.writeValueAsString(responseBody));
            }
            
            // Normalizar la respuesta para mantener consistencia en el código
            responseBody.put("tilopayLinkId", linkId.toString());
            if (responseBody.containsKey("url")) {
                responseBody.put("linkUrl", responseBody.get("url"));
            }
            
            // Guardar los datos de la transacción
            transaction.setTransactionId(linkId.toString());
            transaction.setTransactionData(objectMapper.writeValueAsString(responseBody));
            transactionRepository.save(transaction);
            
            return responseBody;
            } else {
                throw new RuntimeException("Error al crear el link de pago: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error al crear el link de pago: " + e.getMessage());
            e.printStackTrace();
            
            // Invalidar el token para forzar renovación en el próximo intento
            authToken = null;
            tokenExpiration = null;
            
            throw e;
        }
    }
    
    /**
     * Procesar la respuesta de webhook de TiloPay
     */
    @Transactional
    public void processWebhookResponse(Map<String, Object> webhookData) throws Exception {
        // Extraer la información relevante del webhook
        String code = (String) webhookData.get("code");
        String tilopayLinkId = webhookData.get("tilopayLinkId").toString();
        
        // Buscar la transacción por el ID de TiloPay
        Transaction transaction = transactionRepository.findByTransactionId(tilopayLinkId)
            .orElseThrow(() -> new RuntimeException("Transacción no encontrada con ID: " + tilopayLinkId));
        
        // Verificar si el pago fue exitoso (code "1" significa aprobado según la documentación)
        if ("1".equals(code)) {
            transaction.setStatus("COMPLETED");
            transaction.setTransactionData(objectMapper.writeValueAsString(webhookData));
            transactionRepository.save(transaction);
            
            // Aplicar los beneficios de la compra
            applyPurchase(transaction);
        } else {
            transaction.setStatus("FAILED");
            transaction.setTransactionData(objectMapper.writeValueAsString(webhookData));
            transactionRepository.save(transaction);
        }
    }
    
    /**
     * Procesar la respuesta del callback (redirección del usuario)
     */
    @Transactional
    public Transaction processCallbackResponse(String code, String tilopayLinkId) {
        // Buscar la transacción por el ID de TiloPay
        Transaction transaction = transactionRepository.findByTransactionId(tilopayLinkId)
            .orElseThrow(() -> new RuntimeException("Transacción no encontrada con ID: " + tilopayLinkId));
        
        // Verificar si el pago fue exitoso (code "1" significa aprobado según la documentación)
        if ("1".equals(code)) {
            transaction.setStatus("COMPLETED");
            transactionRepository.save(transaction);
            
            // Aplicar los beneficios de la compra
            applyPurchase(transaction);
        } else {
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
        }
        
        return transaction;
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
    
    /**
     * Verificar el estado de una transacción
     */
    public Transaction getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new RuntimeException("Transacción no encontrada con ID: " + transactionId));
    }
}