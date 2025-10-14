package com.hirematch.hirematch_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hirematch.hirematch_api.entity.Transaction;
import com.hirematch.hirematch_api.entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUsuario(Usuario usuario);
    List<Transaction> findByUsuarioUsuarioId(Long usuarioId);
    Optional<Transaction> findByTransactionId(String transactionId);
    Optional<Transaction> findByOrderNumber(String orderNumber);
}