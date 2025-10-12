package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.entity.Transaction;
import com.hirematch.hirematch_api.service.TiloPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private TiloPayService tiloPayService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @PostMapping("/create-payment-link")
    public ResponseEntity<?> createPaymentLink(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam(required = false) String returnUrl) {
        try {
            // Construir las URLs de callback y webhook
            String callbackUrl = baseUrl + "/api/payments/callback";
            String webhookUrl = baseUrl + "/api/payments/webhook";
            
            // Si se proporciona una URL de retorno, agregarla como parámetro al callback
            if (returnUrl != null && !returnUrl.isEmpty()) {
                callbackUrl += "?returnUrl=" + returnUrl;
            }
            
            Map<String, Object> paymentLink = tiloPayService.createPaymentLink(
                userId, productId, callbackUrl, webhookUrl
            );
            
            // Extraer la URL de pago y otros datos relevantes
            Map<String, Object> response = new HashMap<>();
            response.put("paymentUrl", paymentLink.get("linkUrl"));
            response.put("tilopayLinkId", paymentLink.get("tilopayLinkId"));
            response.put("fullResponse", paymentLink);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/callback")
    public ResponseEntity<?> paymentCallback(
            @RequestParam("code") String code,
            @RequestParam("tilopayLinkId") String tilopayLinkId,
            @RequestParam(required = false) String returnUrl) {
        
        try {
            Transaction transaction = tiloPayService.processCallbackResponse(code, tilopayLinkId);
            
            // Si hay una URL de retorno, redirigir al usuario
            if (returnUrl != null && !returnUrl.isEmpty()) {
                // Agregar parámetros de status a la URL de retorno
                String redirectUrl = returnUrl + "?status=" + transaction.getStatus() 
                    + "&transactionId=" + tilopayLinkId;
                return ResponseEntity.status(302).header("Location", redirectUrl).build();
            }
            
            // Si no hay URL de retorno, mostrar un mensaje HTML
            String status = transaction.getStatus();
            String message = "COMPLETED".equals(status) 
                ? "¡Pago completado con éxito!" 
                : "El pago no pudo ser procesado.";
            
            String htmlResponse = String.format(
                "<!DOCTYPE html><html><head><title>Estado del Pago</title></head><body>"
                + "<h1>%s</h1><p>Estado: %s</p><p>ID de transacción: %s</p>"
                + "<p><a href='/'>Volver al inicio</a></p></body></html>",
                message, status, tilopayLinkId
            );
            
            return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(htmlResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/webhook")
    public ResponseEntity<?> paymentWebhook(@RequestBody Map<String, Object> webhookData) {
        try {
            tiloPayService.processWebhookResponse(webhookData);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Es importante devolver 200 OK incluso en caso de error para que TiloPay no reintente
            return ResponseEntity.ok().body("Error procesando webhook: " + e.getMessage());
        }
    }
    
    @GetMapping("/status/{transactionId}")
    public ResponseEntity<?> checkTransactionStatus(@PathVariable String transactionId) {
        try {
            Transaction transaction = tiloPayService.getTransaction(transactionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactionId", transaction.getTransactionId());
            response.put("status", transaction.getStatus());
            response.put("product", transaction.getProduct().getName());
            response.put("amount", transaction.getAmount());
            response.put("date", transaction.getTransactionDate());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}