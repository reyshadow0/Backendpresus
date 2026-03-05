package ec.edu.uteq.presustentaciones.controllers;

import ec.edu.uteq.presustentaciones.entities.Cronograma;
import ec.edu.uteq.presustentaciones.services.CronogramaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/cronogramas")
public class CronogramaController {

    private final CronogramaService cronogramaService;

    public CronogramaController(CronogramaService cronogramaService) {
        this.cronogramaService = cronogramaService;
    }

    @PostMapping("/crear")
    public Cronograma crear(@RequestParam Long solicitudId,
                            @RequestParam Long salaId,
                            @RequestParam LocalDate fecha,
                            @RequestParam LocalTime hora) {
        return cronogramaService.crearCronograma(solicitudId, salaId, fecha, hora);
    }

    @GetMapping
    public List<Cronograma> listar() {
        return cronogramaService.listarCronogramas();
    }

    @GetMapping("/estudiante/{estudianteId}")
    public List<Cronograma> listarPorEstudiante(@PathVariable Long estudianteId) {
        return cronogramaService.listarPorEstudiante(estudianteId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Cronograma> listarPorUsuario(@PathVariable Long usuarioId) {
        return cronogramaService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<Cronograma> porSolicitud(@PathVariable Long solicitudId) {
        return cronogramaService.buscarPorSolicitud(solicitudId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cronogramaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
