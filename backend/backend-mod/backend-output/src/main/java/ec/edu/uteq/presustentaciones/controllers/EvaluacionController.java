package ec.edu.uteq.presustentaciones.controllers;

import ec.edu.uteq.presustentaciones.entities.Evaluacion;
import ec.edu.uteq.presustentaciones.services.EvaluacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    public EvaluacionController(EvaluacionService evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    @PostMapping("/evaluar")
    public Evaluacion evaluar(@RequestParam Long solicitudId,
                              @RequestParam Long rubricaId,
                              @RequestParam Double notaFinal,
                              @RequestParam String observaciones) {
        return evaluacionService.evaluarSolicitud(solicitudId, rubricaId, notaFinal, observaciones);
    }

    @GetMapping
    public List<Evaluacion> listar() {
        return evaluacionService.listarEvaluaciones();
    }

    @GetMapping("/estudiante/{estudianteId}")
    public List<Evaluacion> listarPorEstudiante(@PathVariable Long estudianteId) {
        return evaluacionService.listarPorEstudiante(estudianteId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Evaluacion> listarPorUsuario(@PathVariable Long usuarioId) {
        return evaluacionService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<Evaluacion> porSolicitud(@PathVariable Long solicitudId) {
        return evaluacionService.buscarPorSolicitud(solicitudId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
