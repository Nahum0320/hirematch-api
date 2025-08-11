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
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setEmail("juan.perez@example.com");
        usuario.setPassword("password123");
        usuario.setActivo(true);
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
        assertEquals(1, violations.size());
    }

    @Test
    void whenPasswordIsTooShortThenValidationFails() {
        usuario.setPassword("123");
        var violations = validator.validate(usuario);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void whenNombreIsBlankThenValidationFails() {
        usuario.setNombre("");
        var violations = validator.validate(usuario);
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }

    @Test
    void testPrePersist() {
        assertNull(usuario.getFechaCreacion());
        usuario = entityManager.persistAndFlush(usuario);
        assertNotNull(usuario.getFechaCreacion());
    }

    @Test
    void testPreUpdate() {
        // First persist and get initial state
        usuario = entityManager.persistAndFlush(usuario);
        entityManager.clear();

        // Get a fresh instance
        Usuario managedUsuario = entityManager.find(Usuario.class, usuario.getId());
        LocalDateTime originalUpdate = managedUsuario.getFechaActualizacion();

        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Make a change
        managedUsuario.setNombre("Updated Name");
        entityManager.persistAndFlush(managedUsuario);
        
        // Verify the update timestamp changed
        assertNotNull(managedUsuario.getFechaActualizacion());
        if (originalUpdate != null) {
            assertTrue(managedUsuario.getFechaActualizacion().isAfter(originalUpdate));
        }
    }
}