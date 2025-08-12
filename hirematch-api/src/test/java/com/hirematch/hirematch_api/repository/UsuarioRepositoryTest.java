package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setNombre("Test User");
        usuario.setEmail("test@example.com");
        usuario.setPasswordHash("$2a$10$XXXXXXXXXXXXXXXXXXXXXXXX");
        usuario.setEmailVerificado(false);
        
        // Clear and persist fresh test data
        entityManager.clear();
        entityManager.persistAndFlush(usuario);
    }

    @Test
    void shouldFindUserByEmail() {
        Optional<Usuario> found = usuarioRepository.findByEmail(usuario.getEmail());
        
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo(usuario.getNombre());
    }

    @Test
    void shouldNotFindUserByNonExistentEmail() {
        Optional<Usuario> found = usuarioRepository.findByEmail("nonexistent@example.com");
        
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckExistingEmail() {
        boolean exists = usuarioRepository.existsByEmail(usuario.getEmail());
        
        assertThat(exists).isTrue();
    }

    @Test
    void shouldCheckNonExistingEmail() {
        boolean exists = usuarioRepository.existsByEmail("nonexistent@example.com");
        
        assertThat(exists).isFalse();
    }

    @Test
    void shouldSaveUser() {
        Usuario newUser = new Usuario();
        newUser.setNombre("New User");
        newUser.setEmail("new@example.com");
        newUser.setPasswordHash("$2a$10$YYYYYYYYYYYYYYYYYYYYYYYY");
        
        Usuario saved = usuarioRepository.save(newUser);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNombre()).isEqualTo(newUser.getNombre());
    }

    @Test
    void shouldDeleteUser() {
        usuarioRepository.deleteById(usuario.getId());
        
        Optional<Usuario> deleted = usuarioRepository.findById(usuario.getId());
        assertThat(deleted).isEmpty();
    }
}