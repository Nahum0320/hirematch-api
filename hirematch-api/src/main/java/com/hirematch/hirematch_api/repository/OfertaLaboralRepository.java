package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.OfertaLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfertaLaboralRepository extends JpaRepository<OfertaLaboral, Long> {
}

