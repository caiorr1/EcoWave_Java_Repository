package br.com.ecowave.repository;

import br.com.ecowave.model.Amigos;
import br.com.ecowave.model.Amigos.AmigosId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AmigosRepository extends JpaRepository<Amigos, AmigosId> {
    List<Amigos> findByIdUsuario(Long idUsuario);

    Page<Amigos> findByIdUsuario(Long idUsuario, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Amigos a WHERE a.idUsuario = :idUsuario AND a.idAmigo = :idAmigo")
    void deleteByIds(@Param("idUsuario") Long idUsuario, @Param("idAmigo") Long idAmigo);
}
