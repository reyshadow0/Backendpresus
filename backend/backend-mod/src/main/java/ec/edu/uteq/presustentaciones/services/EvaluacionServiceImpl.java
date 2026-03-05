package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Evaluacion;
import ec.edu.uteq.presustentaciones.entities.Rubrica;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.repositories.EvaluacionRepository;
import ec.edu.uteq.presustentaciones.repositories.RubricaRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final RubricaRepository rubricaRepository;

    /**
     * RF-09: Registrar evaluación con ponderación configurable 60/40.
     * La nota final se calcula automáticamente: (notaInstructor * pesoInstructor/100) + (notaJurado * pesoJurado/100)
     */
    @Override
    public Evaluacion evaluarSolicitud(Long solicitudId, Long rubricaId,
                                        Double notaInstructor, Double notaJurado,
                                        String observaciones,
                                        Double pesoInstructor, Double pesoJurado) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + solicitudId));
        Rubrica rubrica = rubricaRepository.findById(rubricaId)
                .orElseThrow(() -> new RuntimeException("Rúbrica no encontrada: " + rubricaId));

        // Validar que los pesos sumen 100
        double sumaPesos = (pesoInstructor != null ? pesoInstructor : 60.0)
                         + (pesoJurado != null ? pesoJurado : 40.0);
        if (Math.abs(sumaPesos - 100.0) > 0.01) {
            throw new RuntimeException("Los pesos deben sumar 100. Suma actual: " + sumaPesos);
        }

        // Validar rango de notas (0–10)
        if (notaInstructor < 0 || notaInstructor > 10 || notaJurado < 0 || notaJurado > 10) {
            throw new RuntimeException("Las notas deben estar entre 0 y 10.");
        }

        Evaluacion e = Evaluacion.builder()
                .solicitud(solicitud)
                .rubrica(rubrica)
                .notaInstructor(notaInstructor)
                .notaJurado(notaJurado)
                .pesoInstructor(pesoInstructor != null ? pesoInstructor : 60.0)
                .pesoJurado(pesoJurado != null ? pesoJurado : 40.0)
                .observaciones(observaciones)
                .build();

        e.calcularNotaFinal();
        return evaluacionRepository.save(e);
    }

    /** Compatibilidad: acepta nota final directa cuando ya viene calculada */
    @Override
    public Evaluacion evaluarSolicitud(Long solicitudId, Long rubricaId,
                                        Double notaFinal, String observaciones) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        Rubrica rubrica = rubricaRepository.findById(rubricaId)
                .orElseThrow(() -> new RuntimeException("Rúbrica no encontrada"));

        Evaluacion e = Evaluacion.builder()
                .solicitud(solicitud).rubrica(rubrica)
                .notaFinal(notaFinal).observaciones(observaciones)
                .pesoInstructor(60.0).pesoJurado(40.0)
                .resultado(notaFinal >= 7 ? "APROBADO" : "REPROBADO")
                .build();
        return evaluacionRepository.save(e);
    }

    @Override
    public List<Evaluacion> listarEvaluaciones() {
        return evaluacionRepository.findAll();
    }

    @Override
    public List<Evaluacion> listarPorEstudiante(Long estudianteId) {
        return evaluacionRepository.findByEstudianteId(estudianteId);
    }

    @Override
    public List<Evaluacion> listarPorUsuario(Long usuarioId) {
        return evaluacionRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Optional<Evaluacion> buscarPorSolicitud(Long solicitudId) {
        return evaluacionRepository.findBySolicitudId(solicitudId);
    }
}
