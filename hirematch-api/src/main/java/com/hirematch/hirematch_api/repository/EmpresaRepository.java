package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    List<Empresa> findByUsuarioUsuarioId(Long usuarioId);

    boolean existsByNombreEmpresa(String nombreEmpresa);
}