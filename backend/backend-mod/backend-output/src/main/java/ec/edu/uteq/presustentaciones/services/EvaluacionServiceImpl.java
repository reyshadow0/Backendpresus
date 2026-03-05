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

    @Override
    public Evaluacion evaluarSolicitud(Long solicitudId, Long rubricaId, Double notaFinal, String observaciones) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        Rubrica rubrica = rubricaRepository.findById(rubricaId)
                .orElseThrow(() -> new RuntimeException("Rúbrica no encontrada"));

        Evaluacion e = Evaluacion.builder()
                .solicitud(solicitud).rubrica(rubrica)
                .notaFinal(notaFinal).observaciones(observaciones)
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
