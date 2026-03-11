package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Cronograma;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CronogramaService {
    Cronograma crearCronograma(Long solicitudId, Long salaId, LocalDate fecha, LocalTime hora);
    List<Cronograma> listarCronogramas();
    List<Cronograma> listarPorEstudiante(Long estudianteId);
    List<Cronograma> listarPorUsuario(Long usuarioId);
    Optional<Cronograma> buscarPorSolicitud(Long solicitudId);
    void eliminar(Long id);
}
