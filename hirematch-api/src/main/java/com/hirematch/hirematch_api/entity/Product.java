package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    private ProductType type;
    
    // Para paquetes de likes/superlikes
    private Integer quantity;
    
    // Para suscripciones
    private Integer durationInDays;
    
    // ID del producto en Onvo Pay
    private String onvoPayProductId;

    public enum ProductType {
        LIKE_PACKAGE,
        SUPERLIKE_PACKAGE,
        SUBSCRIPTION
    }
}