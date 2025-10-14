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

    // Almacén en memoria del token (en producción usar una solución más robusta
    // como Redis)
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
                    java.net.http.HttpResponse.BodyHandlers.ofString());

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

                System.out.println("Token obtenido exitosamente: "
                        + authToken.substring(0, Math.min(20, authToken.length())) + "...");
                tokenExpiration = LocalDateTime.now().plusHours(24);
                return authToken;
            } else {
                throw new RuntimeException(
                        "Error al obtener el token: " + response.statusCode() + " " + response.body());
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
    public Map<String, Object> createPaymentLink(Long usuarioId, Long productId, String callbackUrl, String webhookUrl)
            throws Exception {
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
        requestBody.put("reference", product.getName());
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
                    Map.class);

            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + objectMapper.writeValueAsString(response.getBody()));

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // TiloPay devuelve "id" en lugar de "tilopayLinkId" y "url" en lugar de
                // "linkUrl"
                Object linkId = responseBody.get("id");
                if (linkId == null) {
                    throw new RuntimeException("No se recibió el ID del link de pago. Response: "
                            + objectMapper.writeValueAsString(responseBody));
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
        System.out.println("=== PROCESANDO WEBHOOK ===");
        System.out.println("Datos recibidos: " + objectMapper.writeValueAsString(webhookData));

        // Extraer la información relevante del webhook
        String code = webhookData.get("code") != null ? webhookData.get("code").toString() : null;

        // TiloPay puede enviar "tilopayLinkId" o necesitamos usar "tilopayOrderId"
        final String resolvedTiloPayLinkId;
        if (webhookData.containsKey("tilopayLinkId")) {
            resolvedTiloPayLinkId = webhookData.get("tilopayLinkId").toString();
        } else if (webhookData.containsKey("tilopayOrderId")) {
            // Si no viene tilopayLinkId, intentar buscar por orderNumber
            String orderNumber = webhookData.get("orderNumber") != null ? webhookData.get("orderNumber").toString()
                    : null;

            if (orderNumber != null) {
                // Buscar la transacción por referencia (orderNumber)
                Transaction transaction = transactionRepository.findByOrderNumber(orderNumber)
                        .orElseThrow(() -> new RuntimeException(
                                "Transacción no encontrada con orderNumber: " + orderNumber));
                resolvedTiloPayLinkId = transaction.getTransactionId();
            } else {
                resolvedTiloPayLinkId = null;
            }
        } else {
            resolvedTiloPayLinkId = null;
        }

        if (resolvedTiloPayLinkId == null) {
            throw new RuntimeException("No se pudo identificar la transacción en el webhook");
        }

        // Buscar la transacción por el ID de TiloPay
        Transaction transaction = transactionRepository.findByTransactionId(resolvedTiloPayLinkId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada con ID: " + resolvedTiloPayLinkId));

        System.out.println("Transacción encontrada: " + transaction.getId());
        System.out.println("Code recibido: " + code);

        // Verificar si el pago fue exitoso (code "1" significa aprobado)
        if ("1".equals(code)) {
            // Verificar si ya fue procesada para evitar duplicados
            if ("COMPLETED".equals(transaction.getStatus())) {
                System.out.println("Transacción ya fue procesada anteriormente");
                return;
            }

            transaction.setStatus("COMPLETED");
            transaction.setTransactionData(objectMapper.writeValueAsString(webhookData));
            transactionRepository.save(transaction);

            System.out.println("Aplicando beneficios de la compra...");
            // Aplicar los beneficios de la compra
            applyPurchase(transaction);

            System.out.println("Webhook procesado exitosamente");
        } else {
            transaction.setStatus("FAILED");
            transaction.setTransactionData(objectMapper.writeValueAsString(webhookData));
            transactionRepository.save(transaction);

            System.out.println("Pago rechazado con code: " + code);
        }

        System.out.println("========================");
    }

    /**
     * Verificar el estado de una transacción en TiloPay y aplicar beneficios si
     * está aprobada
     */
    @Transactional
    public Map<String, Object> verifyAndApplyTransaction(String tilopayLinkId) throws Exception {
        System.out.println("=== VERIFICANDO TRANSACCIÓN: " + tilopayLinkId + " ===");

        // Buscar la transacción en tu base de datos
        Transaction transaction = transactionRepository.findByTransactionId(tilopayLinkId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada con ID: " + tilopayLinkId));

        // Si ya está completada, no hacer nada más
        if ("COMPLETED".equals(transaction.getStatus())) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "COMPLETED");
            result.put("message", "La transacción ya fue procesada anteriormente");
            result.put("transaction", transaction);
            System.out.println("Transacción ya completada");
            return result;
        }

        // Obtener el token de autenticación
        String token = getAuthToken();

        // Preparar la petición para consultar el estado en TiloPay
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", apiKey);
        requestBody.put("orderNumber", tilopayLinkId); // Usar el tilopayLinkId como orderNumber
        requestBody.put("merchantId", ""); // Opcional

        System.out.println("=== TILOPAY VERIFY REQUEST ===");
        System.out.println("URL: " + apiUrl + "/consult");
        System.out.println("Request Body: " + objectMapper.writeValueAsString(requestBody));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl + "/consult",
                    request,
                    Map.class);

            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + objectMapper.writeValueAsString(response.getBody()));

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // Verificar si hay respuestas
                Object responseObj = responseBody.get("response");
                if (responseObj instanceof List) {
                    List<Map<String, Object>> responses = (List<Map<String, Object>>) responseObj;

                    if (!responses.isEmpty()) {
                        Map<String, Object> paymentData = responses.get(0);
                        String code = paymentData.get("code") != null ? paymentData.get("code").toString() : null;

                        // Guardar los datos completos de la verificación
                        transaction.setTransactionData(objectMapper.writeValueAsString(paymentData));

                        // code "1" significa aprobado
                        if ("1".equals(code)) {
                            transaction.setStatus("COMPLETED");
                            transactionRepository.save(transaction);

                            // Aplicar los beneficios de la compra
                            applyPurchase(transaction);

                            Map<String, Object> result = new HashMap<>();
                            result.put("status", "COMPLETED");
                            result.put("message", "Pago verificado y beneficios aplicados exitosamente");
                            result.put("transaction", transaction);
                            result.put("paymentDetails", paymentData);

                            System.out.println("Pago verificado y completado");
                            return result;
                        } else {
                            transaction.setStatus("FAILED");
                            transactionRepository.save(transaction);

                            Map<String, Object> result = new HashMap<>();
                            result.put("status", "FAILED");
                            result.put("message", "El pago no fue aprobado");
                            result.put("transaction", transaction);
                            result.put("paymentDetails", paymentData);

                            System.out.println("Pago no aprobado, code: " + code);
                            return result;
                        }
                    } else {
                        // No hay datos de la transacción aún, mantener como PENDING
                        Map<String, Object> result = new HashMap<>();
                        result.put("status", "PENDING");
                        result.put("message", "La transacción aún está siendo procesada");
                        result.put("transaction", transaction);

                        System.out.println("Transacción pendiente");
                        return result;
                    }
                } else {
                    throw new RuntimeException("Formato de respuesta inesperado de TiloPay");
                }
            } else {
                throw new RuntimeException("Error al consultar la transacción: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error al verificar la transacción: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> result = new HashMap<>();
            result.put("status", "ERROR");
            result.put("message", "Error al verificar el pago: " + e.getMessage());
            result.put("transaction", transaction);

            return result;
        }
    }

    /**
     * Verificar transacción de manera segura sin lanzar excepciones
     */
    public Map<String, Object> safeVerifyTransaction(String tilopayLinkId) {
        try {
            return verifyAndApplyTransaction(tilopayLinkId);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "ERROR");
            result.put("message", "Error al verificar la transacción: " + e.getMessage());
            return result;
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

        // Verificar si el pago fue exitoso (code "1" significa aprobado según la
        // documentación)
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
                        product.getDurationInDays());
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