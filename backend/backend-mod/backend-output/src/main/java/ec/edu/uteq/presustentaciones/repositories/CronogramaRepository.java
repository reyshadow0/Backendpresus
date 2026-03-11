package ec.edu.uteq.presustentaciones.repositories;

import ec.edu.uteq.presustentaciones.entities.Cronograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CronogramaRepository extends JpaRepository<Cronograma, Long> {
    @Query("SELECT c FROM Cronograma c JOIN c.solicitud s JOIN s.estudiante e WHERE e.id = :estudianteId")
    List<Cronograma> findByEstudianteId(@Param("estudianteId") Long estudianteId);

    @Query("SELECT c FROM Cronograma c JOIN c.solicitud s JOIN s.estudiante e JOIN e.usuario u WHERE u.id = :usuarioId")
    List<Cronograma> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    Optional<Cronograma> findBySolicitudId(Long solicitudId);
}
