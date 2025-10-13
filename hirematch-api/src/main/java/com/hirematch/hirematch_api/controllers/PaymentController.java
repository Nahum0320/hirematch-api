package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.PagoSuscripcionRequest;
import com.hirematch.hirematch_api.DTO.PagoSuscripcionResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Product;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.ProductRepository;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.TiloPayService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private TiloPayService tiloPayService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Value("${tilopay.callback.url:http://localhost:8080/api/payments/callback}")
    private String callbackUrl;

    @Value("${tilopay.webhook.url:http://localhost:8080/api/payments/webhook}")
    private String webhookUrl;

    @PostMapping("/suscripcion/premium")
    public ResponseEntity<PagoSuscripcionResponse> createPremiumSubscription(
            @Valid @RequestBody PagoSuscripcionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Usuario usuario = obtenerUsuarioAutenticado(authHeader);
            Long userId = usuario.getUsuarioId();

            logger.info("Processing premium subscription purchase for usuarioId: {}, productId: {}", userId, 7);

            // Use configured callbackUrl and webhookUrl
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