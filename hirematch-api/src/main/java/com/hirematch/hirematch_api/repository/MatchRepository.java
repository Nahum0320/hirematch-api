package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Like;
import com.hirematch.hirematch_api.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByLike_Oferta_Id(Long ofertaId);

    Optional<Match> findByLikeId(Long likeId);
}