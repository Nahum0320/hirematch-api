package com.hirematch.hirematch_api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hirematch.hirematch_api.entity.Sesion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.secret}")
    private String apiSecret;

    public String generarToken(Sesion sesion) {
        Algorithm algorithm = Algorithm.HMAC256(apiSecret);
        return JWT.create()
                .withIssuer("hirematch-api")
                .withSubject(sesion.getNumeroSesion().toString()) // numero de Sesion como subject
                .withExpiresAt(generarFechaExpiracion())
                .sign(algorithm);
    }

    public String getSubject(String token) {
        Algorithm algorithm = Algorithm.HMAC256(apiSecret);
        DecodedJWT decodedJWT = JWT.require(algorithm)
                .withIssuer("hirematch-api")
                .build()
                .verify(token);

        return decodedJWT.getSubject();
    }

    private Instant generarFechaExpiracion() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-05:00"));
    }
}

