package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Cronograma;
import ec.edu.uteq.presustentaciones.entities.Sala;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.repositories.CronogramaRepository;
import ec.edu.uteq.presustentaciones.repositories.SalaRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CronogramaServiceImpl implements CronogramaService {

    private final CronogramaRepository cronogramaRepository;
    private final SolicitudRepository solicitudRepository;
    private final SalaRepository salaRepository;

    @Override
    public Cronograma crearCronograma(Long solicitudId, Long salaId, LocalDate fecha, LocalTime hora) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

        Cronograma c = Cronograma.builder()
                .solicitud(solicitud)
                .sala(sala)
                .fechaInicio(LocalDateTime.of(fecha, hora))
                .duracionMin(45)
                .estado("ACTIVO")
                .build();
        return cronogramaRepository.save(c);
    }

    @Override
    public List<Cronograma> listarCronogramas() {
        return cronogramaRepository.findAll();
    }

    @Override
    public List<Cronograma> listarPorEstudiante(Long estudianteId) {
        return cronogramaRepository.findByEstudianteId(estudianteId);
    }

    @Override
    public List<Cronograma> listarPorUsuario(Long usuarioId) {
        return cronogramaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Optional<Cronograma> buscarPorSolicitud(Long solicitudId) {
        return cronogramaRepository.findBySolicitudId(solicitudId);
    }

    @Override
    public void eliminar(Long id) {
        cronogramaRepository.deleteById(id);
    }
}
