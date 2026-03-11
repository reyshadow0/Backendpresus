package ec.edu.uteq.presustentaciones.repositories;

import ec.edu.uteq.presustentaciones.entities.Jurado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JuradoRepository extends JpaRepository<Jurado, Long> {

    List<Jurado> findBySolicitudId(Long solicitudId);

    @Query("SELECT j FROM Jurado j WHERE j.docente.id = :docenteId")
    List<Jurado> findByDocenteId(Long docenteId);

    @Query("SELECT COUNT(j) FROM Jurado j WHERE j.docente.id = :docenteId AND j.solicitud.estado NOT IN ('RECHAZADA')")
    long contarAsignacionesActivasByDocente(Long docenteId);
}
