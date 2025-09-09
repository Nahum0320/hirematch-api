package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByLike_Oferta_Id(Long ofertaId);
}