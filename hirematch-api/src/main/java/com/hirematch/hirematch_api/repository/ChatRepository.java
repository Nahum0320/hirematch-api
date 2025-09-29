package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Chat;
import com.hirematch.hirematch_api.entity.Empresa;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import com.hirematch.hirematch_api.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByPostulanteAndEmpresaAndOferta(Perfil postulante, Empresa empresa, OfertaLaboral oferta);

    List<Chat> findByPostulante(Perfil postulante);

    List<Chat> findByEmpresa(Empresa empresa);

    List<Chat> findByOferta_Id(Long ofertaId);
}