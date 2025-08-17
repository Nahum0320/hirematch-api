package com.hirematch.hirematch_api.service;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private static final int SALT_ROUNDS = 11;
    /** Encripta una contraseña utilizando BCrypt.
     * @param contrasena la contrasena a encriptar
     * @return contrasena encriptada
     * @throws IllegalArgumentException si la contrasena es nula o vacia
     */
    public String encriptar(String contrasena)
    {
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
        }
        try{
            return BCrypt.hashpw(contrasena, BCrypt.gensalt(SALT_ROUNDS));
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar la contraseña", e);
        }
    }

    /**
     *  Verifica la contraseña para que coincida con el hash
     *
     * @param contrasena contraseña para validar
     * @param hashed hash para validar la contraseña
     * @return si es valida o no
     * @throws IllegalArgumentException si no es valida
     */

    public boolean verificar(String contrasena, String hashed) {
        if (contrasena==null||contrasena.trim().isEmpty()){
            throw new IllegalArgumentException("la contraseña no puede ser nula ni vacia");
        }
        try {
            return BCrypt.checkpw(contrasena, hashed);
        }
        catch (Exception e){
            throw new RuntimeException("Error al Validar la contraseña");
        }
    }

}





