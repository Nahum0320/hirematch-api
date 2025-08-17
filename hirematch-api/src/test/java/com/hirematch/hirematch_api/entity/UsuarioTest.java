package com.hirematch.hirematch_api.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UsuarioTest {

    @Autowired
    private TestEntityManager entityManager;
    private Validator validator;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        usuario = new Usuario();
        usuario.setNombre("Juan Pérez");
        usuario.setEmail("juan.perez@example.com");
        usuario.setPasswordHash("$2a$10$XXXXXXXXXXXXXXXXXXXXXXXX"); // Example hashed password
        usuario.setEmailVerificado(false);
    }

    @Test
    void whenAllFieldsAreValidThenValidationSucceeds() {
        var violations = validator.validate(usuario);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenEmailIsInvalidThenValidationFails() {
        usuario.setEmail("invalid-email");
        var violations = validator.validate(usuario);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("email")));
    }

    @Test
    void whenNombreExceedsLengthThenValidationFails() {
        String longNombre = "a".repeat(151);
        usuario.setNombre(longNombre);
        var violations = validator.validate(usuario);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("150 caracteres")));
    }

    @Test
    void whenNombreIsBlankThenValidationFails() {
        usuario.setNombre("");
        var violations = validator.validate(usuario);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("nombre")));
    }

    @Test
    void whenPasswordHashIsBlankThenValidationFails() {
        usuario.setPasswordHash("");
        var violations = validator.validate(usuario);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("contraseña")));
    }

    @Test
    void testPrePersist() {
        assertNull(usuario.getFechaRegistro());
        usuario = entityManager.persistAndFlush(usuario);
        assertNotNull(usuario.getFechaRegistro());
    }

    @Test
    void testOptionalFields() {
        usuario.setCodigoVerificacion("123456");
        usuario.setFechaExpiracionCodigo(LocalDateTime.now().plusHours(24));
        usuario.setLlaveUnica("unique-key-123");
        usuario.setUltimoAcceso(LocalDateTime.now());
        
        var violations = validator.validate(usuario);
        assertTrue(violations.isEmpty());
        
        usuario = entityManager.persistAndFlush(usuario);
        assertNotNull(usuario.getUsuarioId());
    }
}