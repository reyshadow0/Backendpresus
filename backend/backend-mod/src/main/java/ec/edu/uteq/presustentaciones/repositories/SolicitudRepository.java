package ec.edu.uteq.presustentaciones.repositories;

import ec.edu.uteq.presustentaciones.entities.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByEstudianteId(Long estudianteId);

    @Query("SELECT s FROM Solicitud s " +
           "JOIN FETCH s.estudiante e " +
           "JOIN FETCH e.usuario u " +
           "WHERE s.id = :id")
    Optional<Solicitud> findByIdWithEstudiante(@Param("id") Long id);

    long countByEstado(String estado);
}
