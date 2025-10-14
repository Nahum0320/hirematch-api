package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id") // Cambiado de user_id a usuario_id
    private Usuario usuario; // Cambiado de User a Usuario

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "order_number") // ⭐ NUEVO CAMPO
    private String orderNumber; // Nuestro orderNumber único

    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String transactionId;  // ID de transacción de Onvo Pay
    private String status;  // "PENDING", "COMPLETED", "FAILED"
    
    @Column(columnDefinition = "TEXT")
    private String transactionData;  // JSON con datos adicionales
}
