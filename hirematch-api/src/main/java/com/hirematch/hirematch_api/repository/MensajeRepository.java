package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Chat;
import com.hirematch.hirematch_api.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("SELECT m FROM Mensaje m WHERE m.chat = :chat ORDER BY m.fechaEnvio DESC")
    List<Mensaje> findByChatOrderByFechaEnvioDesc(@Param("chat") Chat chat);

    @Query("SELECT m FROM Mensaje m WHERE m.chat = :chat ORDER BY m.fechaEnvio DESC LIMIT 1")
    Optional<Mensaje> findTopByChatOrderByFechaEnvioDesc(@Param("chat") Chat chat);
}
