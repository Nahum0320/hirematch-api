package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.PagoSuscripcionRequest;
import com.hirematch.hirematch_api.DTO.PagoSuscripcionResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Transaction;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.ProductRepository;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.TiloPayService;

// Removed incorrect import for MediaType
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TiloPayService tiloPayService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Value("${tilopay.callback.url:hirematch://payment-result}")
    private String callbackUrl;

    @Value("${tilopay.webhook.url:https://f310a5d17046.ngrok-free.app/api/payments/webhook}")
    private String webhookUrl;

    @PostMapping("/suscripcion/premium")
public ResponseEntity<PagoSuscripcionResponse> createPremiumSubscription(
        @Valid @RequestBody PagoSuscripcionRequest request,
        @RequestHeader("Authorization") String authHeader) {
    try {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        Long userId = usuario.getUsuarioId();

        logger.info("Processing premium subscription purchase for usuarioId: {}, productId: {}", userId, 7);

        // Use the callback URL from the request (mobile deep link) or fallback to configured
        String callbackUrl = request.getCallbackUrl() != null ? request.getCallbackUrl() : this.callbackUrl;

        Map<String, Object> paymentLink = tiloPayService.createPaymentLink(userId, Long.valueOf(7), callbackUrl, webhookUrl);

        PagoSuscripcionResponse response = new PagoSuscripcionResponse();
        response.setPaymentUrl(paymentLink.get("url").toString());
        response.setTilopayLinkId(paymentLink.get("id").toString());
        return ResponseEntity.ok(response);
    } catch (ValidacionException e) {
        logger.error("Validation error: {}", e.getMessage());
        return ResponseEntity.badRequest().body(new PagoSuscripcionResponse(null, null, e.getMessage()));
    } catch (Exception e) {
        logger.error("Error creating premium subscription payment link: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(new PagoSuscripcionResponse(null, null, "Error al procesar la solicitud: " + e.getMessage()));
    }
}

    @PostMapping("/verify-and-apply/{tilopayLinkId}")
    public ResponseEntity<?> verifyAndApplyTransaction(
            @PathVariable String tilopayLinkId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Autenticar usuario
            obtenerUsuarioAutenticado(authHeader);
            
            logger.info("Verifying transaction: {}", tilopayLinkId);
            
            Map<String, Object> result = tiloPayService.verifyAndApplyTransaction(tilopayLinkId);
            
            logger.info("Verification result: {}", result.get("status"));
            
            return ResponseEntity.ok(result);
        } catch (ValidacionException e) {
            logger.error("Validation error: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error verifying transaction: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Verificación segura (no lanza excepciones)
     */
    @GetMapping("/verify/{tilopayLinkId}")
    public ResponseEntity<?> safeVerifyTransaction(
            @PathVariable String tilopayLinkId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Autenticar usuario
            obtenerUsuarioAutenticado(authHeader);
            
            logger.info("Safe verifying transaction: {}", tilopayLinkId);
            
            Map<String, Object> result = tiloPayService.safeVerifyTransaction(tilopayLinkId);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error in safe verify: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.ok(error); // Devolver 200 siempre
        }
    }

    @PostMapping(value = "/webhook", consumes = {MediaType.APPLICATION_JSON_VALUE, "application/octet-stream"})
    public ResponseEntity<?> paymentWebhook(HttpServletRequest request) {
        try {
            // Read the raw request body
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            logger.info("Received webhook request. Content-Type: {}, Body: {}", 
                request.getContentType(), requestBody);

            // Parse the body as JSON
            Map<String, Object> webhookData = objectMapper.readValue(requestBody.toString(), Map.class);

            logger.info("Parsed webhook data: {}", webhookData);

            // Process the webhook
            tiloPayService.processWebhookResponse(webhookData);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            // Return 200 OK to prevent TiloPay from retrying
            return ResponseEntity.ok().body("Error procesando webhook: " + e.getMessage());
        }
    }

    /**
     * Consultar estado de transacción
     */
    @GetMapping("/status/{transactionId}")
    public ResponseEntity<?> checkTransactionStatus(
            @PathVariable String transactionId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            obtenerUsuarioAutenticado(authHeader);
            
            Transaction transaction = tiloPayService.getTransaction(transactionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactionId", transaction.getTransactionId());
            response.put("status", transaction.getStatus());
            response.put("product", transaction.getProduct().getName());
            response.put("amount", transaction.getAmount());
            response.put("date", transaction.getTransactionDate());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking status: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private Usuario obtenerUsuarioAutenticado(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Token de autorización requerido");
            throw new ValidacionException("Token de autorización requerido");
        }

        String token = authHeader.substring(7);
        if (token.trim().isEmpty()) {
            logger.error("Token vacío");
            throw new ValidacionException("Token vacío");
        }

        try {
            String subject = tokenService.getSubject(token);
            if (subject == null || subject.trim().isEmpty()) {
                logger.error("Token inválido");
                throw new ValidacionException("Token inválido");
            }

            Long numeroSesion = Long.parseLong(subject);
            Sesion sesion = sesionRepository.findById(numeroSesion)
                    .orElseThrow(() -> {
                        logger.error("Sesión no encontrada con numero_sesion: {}", numeroSesion);
                        return new ValidacionException("Sesión no encontrada");
                    });

            if (!sesion.isActiva() || sesion.hasExpired()) {
                logger.error("Sesión inactiva o expirada para numero_sesion: {}", numeroSesion);
                throw new ValidacionException("Sesión inactiva o expirada");
            }

            return sesion.getUsuario();
        } catch (NumberFormatException e) {
            logger.error("Token de sesión inválido: {}", e.getMessage());
            throw new ValidacionException("Token de sesión inválido");
        } catch (Exception e) {
            logger.error("Error al procesar el token: {}", e.getMessage());
            throw new ValidacionException("Error al procesar el token: " + e.getMessage());
        }
    }
}