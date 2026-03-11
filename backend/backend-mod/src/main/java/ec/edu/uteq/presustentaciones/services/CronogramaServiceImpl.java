package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Cronograma;
import ec.edu.uteq.presustentaciones.entities.Sala;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.repositories.CronogramaRepository;
import ec.edu.uteq.presustentaciones.repositories.SalaRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CronogramaServiceImpl implements CronogramaService {

    private final CronogramaRepository cronogramaRepository;
    private final SolicitudRepository solicitudRepository;
    private final SalaRepository salaRepository;

    // Horario laboral: 08:00 – 17:00, franjas de 45 min
    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN    = LocalTime.of(17, 0);
    private static final int DURACION = 45;

    @Override
    @Transactional
    public Cronograma crearCronograma(Long solicitudId, Long salaId, LocalDate fecha, LocalTime hora) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

        LocalDateTime inicio = LocalDateTime.of(fecha, hora);
        LocalDateTime fin = inicio.plusMinutes(DURACION);

        // RF-04: verificar conflicto antes de guardar
        List<Cronograma> conflictos = cronogramaRepository.findConflictos(salaId, inicio, fin);
        if (!conflictos.isEmpty()) {
            throw new RuntimeException(
                "Conflicto de horario: la sala '" + sala.getNombre() +
                "' ya tiene una pre-sustentación programada en esa franja.");
        }

        return cronogramaRepository.save(Cronograma.builder()
                .solicitud(solicitud).sala(sala)
                .fechaInicio(inicio).duracionMin(DURACION).estado("ACTIVO").build());
    }

    @Override
    @Transactional
    public Cronograma asignarAutomatico(Long solicitudId) {
        solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Si ya tiene cronograma activo, retornarlo
        Optional<Cronograma> existente = cronogramaRepository.findBySolicitudId(solicitudId);
        if (existente.isPresent() && "ACTIVO".equals(existente.get().getEstado())) {
            return existente.get();
        }

        List<Sala> salas = salaRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getDisponible())).toList();
        if (salas.isEmpty()) throw new RuntimeException("No hay salas disponibles.");

        // Buscar desde mañana en adelante (máx 30 días)
        for (int diasAdelantar = 1; diasAdelantar <= 30; diasAdelantar++) {
            LocalDate fecha = LocalDate.now().plusDays(diasAdelantar);
            // Saltar fines de semana
            if (fecha.getDayOfWeek().getValue() >= 6) continue;

            List<LocalDateTime> franjas = franjasDisponibles(fecha, DURACION);
            for (LocalDateTime franja : franjas) {
                for (Sala sala : salas) {
                    List<Cronograma> conflictos = cronogramaRepository
                            .findConflictos(sala.getId(), franja, franja.plusMinutes(DURACION));
                    if (conflictos.isEmpty()) {
                        // Encontramos franja libre → asignar
                        Solicitud solicitud = solicitudRepository.findById(solicitudId).get();
                        return cronogramaRepository.save(Cronograma.builder()
                                .solicitud(solicitud).sala(sala)
                                .fechaInicio(franja).duracionMin(DURACION).estado("ACTIVO")
                                .build());
                    }
                }
            }
        }
        throw new RuntimeException(
            "No se encontró disponibilidad en los próximos 30 días. Verifique las salas o el calendario.");
    }

    @Override
    public boolean estaDisponible(Long salaId, LocalDateTime inicio, int duracionMin) {
        return cronogramaRepository.findConflictos(salaId, inicio, inicio.plusMinutes(duracionMin)).isEmpty();
    }

    @Override
    public List<LocalDateTime> franjasDisponibles(LocalDate fecha, int duracionMin) {
        List<LocalDateTime> franjas = new ArrayList<>();
        LocalDateTime cursor = LocalDateTime.of(fecha, HORA_INICIO);
        LocalDateTime limite = LocalDateTime.of(fecha, HORA_FIN);
        while (!cursor.plusMinutes(duracionMin).isAfter(limite)) {
            franjas.add(cursor);
            cursor = cursor.plusMinutes(duracionMin);
        }
        return franjas;
    }

    @Override public List<Cronograma> listarCronogramas() { return cronogramaRepository.findAll(); }
    @Override public List<Cronograma> listarPorEstudiante(Long id) { return cronogramaRepository.findByEstudianteId(id); }
    @Override public List<Cronograma> listarPorUsuario(Long id) { return cronogramaRepository.findByUsuarioId(id); }
    @Override public Optional<Cronograma> buscarPorSolicitud(Long id) { return cronogramaRepository.findBySolicitudId(id); }
    @Override public void eliminar(Long id) { cronogramaRepository.deleteById(id); }
}
