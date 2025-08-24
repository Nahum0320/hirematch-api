package com.hirematch.hirematch_api.DTO;

public class LogoutResponse {
    private String message;

    public LogoutResponse() {
        this.setMessage("Sesión cerrada correctamente");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
