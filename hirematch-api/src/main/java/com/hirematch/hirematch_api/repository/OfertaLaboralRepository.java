package com.hirematch.hirematch_api.repository;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfertaLaboralRepository extends JpaRepository<OfertaLaboral, Long> {
    Page<OfertaLaboral> findByEmpresa_EmpresaId(Long empresaId, Pageable pageable);
}

