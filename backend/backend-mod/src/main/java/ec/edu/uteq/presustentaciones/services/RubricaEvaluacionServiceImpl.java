package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.dto.EscalaCriterioDTO;
import ec.edu.uteq.presustentaciones.dto.EvaluacionRubricaRequest;
import ec.edu.uteq.presustentaciones.dto.EvaluacionRubricaResponse;
import ec.edu.uteq.presustentaciones.entities.*;
import ec.edu.uteq.presustentaciones.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RubricaEvaluacionServiceImpl implements RubricaEvaluacionService {

    private final EvaluacionCriterioRepository evalCriterioRepo;
    private final CriterioRubricaRepository criterioRepo;
    private final JuradoRepository juradoRepo;
    private final SolicitudRepository solicitudRepo;
    private final RubricaRepository rubricaRepo;

    @Override
    @Transactional
    public EvaluacionRubricaResponse registrarEvaluacion(EvaluacionRubricaRequest req) {
        Solicitud solicitud = solicitudRepo.findById(req.getSolicitudId())
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + req.getSolicitudId()));

        Jurado jurado = juradoRepo.findById(req.getJuradoId())
                .orElseThrow(() -> new RuntimeException("Jurado no encontrado: " + req.getJuradoId()));

        if (!jurado.getSolicitud().getId().equals(req.getSolicitudId())) {
            throw new RuntimeException("El jurado no pertenece a esta solicitud.");
        }

        rubricaRepo.findById(req.getRubricaId())
                .orElseThrow(() -> new RuntimeException("Rúbrica no encontrada: " + req.getRubricaId()));

        List<CriterioRubrica> criterios = criterioRepo.findByRubricaIdOrderByOrdenAsc(req.getRubricaId());
        if (criterios.isEmpty()) {
            throw new RuntimeException("La rúbrica no tiene criterios definidos.");
        }
        if (req.getCriterios() == null || req.getCriterios().size() != criterios.size()) {
            throw new RuntimeException("Debe evaluar todos los " + criterios.size() + " criterios de la rúbrica.");
        }
        for (EscalaCriterioDTO c : req.getCriterios()) {
            if (!EvaluacionCriterio.esEscalaValida(c.getEscala())) {
                throw new RuntimeException("Escala inválida: " + c.getEscala() + ". Use: 100, 67, 33 o 0.");
            }
        }

        // Permite re-evaluación: eliminar la anterior
        evalCriterioRepo.deleteBySolicitudIdAndJuradoId(req.getSolicitudId(), req.getJuradoId());

        List<EvaluacionCriterio> guardadas = new ArrayList<>();
        for (EscalaCriterioDTO cDto : req.getCriterios()) {
            CriterioRubrica criterio = criterios.stream()
                    .filter(c -> c.getId().equals(cDto.getCriterioId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Criterio no encontrado: " + cDto.getCriterioId()));

            double notaObtenida = Math.round(criterio.getPonderacion() * cDto.getEscala() / 100.0 * 100.0) / 100.0;

            EvaluacionCriterio ec = EvaluacionCriterio.builder()
                    .solicitud(solicitud)
                    .jurado(jurado)
                    .criterio(criterio)
                    .escala(cDto.getEscala())
                    .notaObtenida(notaObtenida)
                    .observaciones(cDto.getObservaciones())
                    .build();
            guardadas.add(evalCriterioRepo.save(ec));
        }

        return buildResponse(jurado, guardadas, req.getSolicitudId());
    }

    @Override
    public EvaluacionRubricaResponse obtenerEvaluacionJurado(Long solicitudId, Long juradoId) {
        Jurado jurado = juradoRepo.findById(juradoId)
                .orElseThrow(() -> new RuntimeException("Jurado no encontrado: " + juradoId));
        List<EvaluacionCriterio> evals = evalCriterioRepo.findBySolicitudIdAndJuradoId(solicitudId, juradoId);
        return buildResponse(jurado, evals, solicitudId);
    }

    @Override
    public List<EvaluacionRubricaResponse> obtenerEvaluacionesSolicitud(Long solicitudId) {
        List<Jurado> jurados = juradoRepo.findBySolicitudId(solicitudId);
        return jurados.stream()
                .map(j -> {
                    List<EvaluacionCriterio> evals = evalCriterioRepo.findBySolicitudIdAndJuradoId(solicitudId, j.getId());
                    return buildResponse(j, evals, solicitudId);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Double calcularNotaTribunal(Long solicitudId) {
        return promedioTribunal(solicitudId);
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    /** Calcula el promedio de (suma de notas por jurado) en Java para evitar subqueries en JPQL */
    private Double promedioTribunal(Long solicitudId) {
        List<Object[]> filas = evalCriterioRepo.sumaPorJurado(solicitudId);
        if (filas == null || filas.isEmpty()) return null;
        double suma = filas.stream()
                .mapToDouble(f -> ((Number) f[1]).doubleValue())
                .sum();
        double promedio = suma / filas.size();
        return Math.round(promedio * 100.0) / 100.0;
    }

    private EvaluacionRubricaResponse buildResponse(Jurado jurado,
                                                     List<EvaluacionCriterio> evals,
                                                     Long solicitudId) {
        String nombre = jurado.getDocente() != null && jurado.getDocente().getUsuario() != null
                ? jurado.getDocente().getUsuario().getNombre() + " " + jurado.getDocente().getUsuario().getApellido()
                : "Docente #" + jurado.getId();

        List<EvaluacionRubricaResponse.CriterioResultado> detalles = evals.stream()
                .map(ec -> EvaluacionRubricaResponse.CriterioResultado.builder()
                        .criterioId(ec.getCriterio().getId())
                        .nombreCriterio(ec.getCriterio().getNombre())
                        .ponderacion(ec.getCriterio().getPonderacion())
                        .escala(ec.getEscala())
                        .notaObtenida(ec.getNotaObtenida())
                        .observaciones(ec.getObservaciones())
                        .build())
                .collect(Collectors.toList());

        double notaTotal = evals.stream()
                .mapToDouble(EvaluacionCriterio::getNotaObtenida)
                .sum();
        notaTotal = Math.round(notaTotal * 100.0) / 100.0;

        Double notaPromedio = promedioTribunal(solicitudId);

        List<Jurado> todosJurados = juradoRepo.findBySolicitudId(solicitudId);
        boolean completo = !todosJurados.isEmpty() && todosJurados.stream()
                .allMatch(j -> evalCriterioRepo.existsBySolicitudIdAndJuradoId(solicitudId, j.getId()));

        return EvaluacionRubricaResponse.builder()
                .solicitudId(solicitudId)
                .juradoId(jurado.getId())
                .nombreJurado(nombre)
                .rolJurado(jurado.getRol())
                .detalles(detalles)
                .notaTotalJurado(evals.isEmpty() ? null : notaTotal)
                .notaPromedioTribunal(notaPromedio)
                .tribunalCompleto(completo)
                .build();
    }
}
