package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.service.OnvoPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private OnvoPayService onvoPayService;
    
    @PostMapping("/create-checkout-session")
public ResponseEntity<?> createCheckoutSession(
        @RequestParam Long userId,
        @RequestParam Long productId,
        @RequestParam String successUrl,
        @RequestParam String cancelUrl) {
    try {
        Map<String, Object> session = onvoPayService.createCheckoutSession(
            userId, productId, successUrl, cancelUrl
        );
        // Extraer la URL de pago del objeto session
        String paymentUrl = (String) session.get("url");
        
        // Crear un objeto de respuesta con toda la información relevante
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.get("id"));
        response.put("paymentUrl", paymentUrl);
        response.put("status", session.get("status"));
        response.put("fullResponse", session);
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
    
    @GetMapping("/verify/{transactionId}")
    public ResponseEntity<?> verifyPayment(@PathVariable String transactionId) {
        try {
            Map<String, Object> status = onvoPayService.checkTransactionStatus(transactionId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/process/{transactionId}")
    public ResponseEntity<?> processPayment(@PathVariable String transactionId) {
        try {
            onvoPayService.processCompletedTransaction(transactionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
