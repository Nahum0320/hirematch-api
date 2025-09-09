package com.hirematch.hirematch_api.entity;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

   @OneToOne(optional = false) 
    @JoinColumn(name = "like_id", nullable = false, unique = true)
    private Like like;

    @Column(name = "fecha_match", nullable = false)
    private LocalDateTime fechaMatch;

    @PrePersist
    protected void onCreate() {
        fechaMatch = LocalDateTime.now();
    }


}
