package com.hirematch.hirematch_api.repository;
import com.hirematch.hirematch_api.entity.Like;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import com.hirematch.hirematch_api.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPerfilAndOferta(Perfil perfil, OfertaLaboral oferta);

    List<Like> findByOferta(OfertaLaboral oferta);

    List<Like> findByOferta_Empresa_EmpresaId(Long empresaId);

    List<Like> findByPerfil(Perfil perfil);
}
