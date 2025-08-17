package com.hirematch.hirematch_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties") 
class PasswordServiceTest {

    @Autowired
    private PasswordService passwordService;

    @BeforeEach
    void SetUp() {
    }

    @Test
    void ShouldEncrypt() {
        String passwd = "123456";
        String hashed = passwordService.encriptar(passwd);
        assertTrue(passwordService.verificar(passwd, hashed));
    }

    @Test
    void ShouldNotVerifyWrongPassword() {
        String passwd = "correctPassword";
        String wrongPass = "wrongPassword"; 
        String hashed = passwordService.encriptar(passwd);
        assertFalse(passwordService.verificar(wrongPass, hashed));
    }

    @Test
    void ShouldThrowExceptionOnNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordService.encriptar(null);
        });
    }

    @Test 
    void ShouldThrowExceptionOnEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordService.encriptar("");
        });
    }

    @Test
    void HashedPasswordShouldBeDifferentFromOriginal() {
        String passwd = "myPassword123";
        String hashed = passwordService.encriptar(passwd);
        assertNotEquals(passwd, hashed);
    }

    @Test
    void ShouldGenerateDifferentHashesForSamePassword() {
        String passwd = "testPassword";
        String hash1 = passwordService.encriptar(passwd);
        String hash2 = passwordService.encriptar(passwd);
        assertNotEquals(hash1, hash2);
    }
}
