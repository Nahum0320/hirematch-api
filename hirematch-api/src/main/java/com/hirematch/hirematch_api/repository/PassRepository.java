package com.hirematch.hirematch_api.repository;
import com.hirematch.hirematch_api.entity.Pass;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import com.hirematch.hirematch_api.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PassRepository extends JpaRepository<Pass, Long> {
    Optional<Pass> findByPerfilAndOferta(Perfil perfil, OfertaLaboral oferta);
    List<Pass> findByPerfil(Perfil perfil);
    List<Pass> findByOferta_Id(Long id);
}
