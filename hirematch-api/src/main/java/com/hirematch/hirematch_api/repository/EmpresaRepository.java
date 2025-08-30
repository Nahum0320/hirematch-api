package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Empresa;
import com.hirematch.hirematch_api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    // Este método ya lo tienes y funciona correctamente
    List<Empresa> findByUsuarioUsuarioId(Long usuarioId);

    boolean existsByNombreEmpresa(String nombreEmpresa);

    // Opción 1: Usar el método que ya funciona, pero devolviendo Optional de la primera empresa
    default Optional<Empresa> findPrimeraEmpresaByUsuarioId(Long usuarioId) {
        List<Empresa> empresas = findByUsuarioUsuarioId(usuarioId);
        return empresas.isEmpty() ? Optional.empty() : Optional.of(empresas.get(0));
    }

    // Opción 2: Usar consulta JPQL explícita (recomendado)
    @Query("SELECT e FROM Empresa e WHERE e.usuario.usuarioId = :usuarioId")
    Optional<Empresa> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Opción 3: Buscar por objeto Usuario directamente (más eficiente)
    Optional<Empresa> findByUsuario(Usuario usuario);
}