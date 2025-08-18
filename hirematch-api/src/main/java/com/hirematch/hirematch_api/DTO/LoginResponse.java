package com.hirematch.hirematch_api.DTO;

public class LoginResponse {

    private String mensaje;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse( String mensaje, String token) {
        this.mensaje = mensaje;
        this.token = token;
    }

    // --- Getters & Setters --

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setToken(String token)
    {
        this.token = token;
    }
    public String getToken()
    {
        return token;
    }
}
