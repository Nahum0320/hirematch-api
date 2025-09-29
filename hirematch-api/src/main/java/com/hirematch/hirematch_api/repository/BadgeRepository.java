package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByActivoTrueOrderByFechaCreacionAsc();

    Optional<Badge> findByNombreAndActivoTrue(String nombre);

    List<Badge> findByTipoAndActivoTrue(Badge.TipoBadge tipo);

    @Query("SELECT b FROM Badge b WHERE b.activo = true AND b.tipo = :tipo AND b.condicionRequerida <= :valor ORDER BY b.condicionRequerida DESC")
    List<Badge> findBadgesDisponiblesByTipoAndValor(@Param("tipo") Badge.TipoBadge tipo, @Param("valor") Integer valor);

    @Query("SELECT b FROM Badge b WHERE b.activo = true ORDER BY b.tipo, b.condicionRequerida ASC")
    List<Badge> findAllActiveBadgesOrdered();

    @Query("SELECT COUNT(b) FROM Badge b WHERE b.activo = true")
    Long countActiveBadges();

    @Query("SELECT b FROM Badge b WHERE b.activo = true AND b.condicionRequerida IS NOT NULL AND b.tipo = :tipo ORDER BY b.condicionRequerida ASC")
    List<Badge> findProgressiveBadgesByTipo(@Param("tipo") Badge.TipoBadge tipo);

    @Query("SELECT b FROM Badge b WHERE b.activo = true AND b.condicionRequerida IS NULL")
    List<Badge> findSpecialBadges();
}