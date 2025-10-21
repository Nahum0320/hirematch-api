package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.EstadoReporte;
import com.hirematch.hirematch_api.entity.Reporte;
import com.hirematch.hirematch_api.entity.TipoReporte;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReporteRepositoryImpl implements ReporteRepositoryCustom {

    private final EntityManager em;

    public ReporteRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<Reporte> buscarFiltros(EstadoReporte estado, TipoReporte tipo, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Reporte> cq = cb.createQuery(Reporte.class);
        Root<Reporte> root = cq.from(Reporte.class);

        List<Predicate> predicates = new ArrayList<>();
        if (estado != null) predicates.add(cb.equal(root.get("estado"), estado));
        if (tipo != null) predicates.add(cb.equal(root.get("tipo"), tipo));
        if (desde != null) predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
        if (hasta != null) predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("fecha")));

        TypedQuery<Reporte> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Reporte> results = query.getResultList();

        // total count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Reporte> countRoot = countQuery.from(Reporte.class);
        countQuery.select(cb.count(countRoot));
        List<Predicate> countPredicates = new ArrayList<>();
        if (estado != null) countPredicates.add(cb.equal(countRoot.get("estado"), estado));
        if (tipo != null) countPredicates.add(cb.equal(countRoot.get("tipo"), tipo));
        if (desde != null) countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("fecha"), desde));
        if (hasta != null) countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("fecha"), hasta));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }
}
