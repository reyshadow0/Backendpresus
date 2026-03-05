package ec.edu.uteq.presustentaciones.controllers;

import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.services.SolicitudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping("/crear/{estudianteId}")
    public Solicitud crear(@PathVariable Long estudianteId, @RequestBody Solicitud datos) {
        return solicitudService.crearSolicitud(estudianteId, datos);
    }

    /** Crear solicitud usando el usuarioId del JWT (frontend lo usa directamente) */
    @PostMapping("/crear-por-usuario/{usuarioId}")
    public ResponseEntity<?> crearPorUsuario(@PathVariable Long usuarioId, @RequestBody Solicitud datos) {
        try {
            return ResponseEntity.ok(solicitudService.crearSolicitudPorUsuario(usuarioId, datos));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /** Listar solicitudes del usuario logueado (por usuarioId) */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long usuarioId) {
        try {
            return ResponseEntity.ok(solicitudService.listarPorUsuario(usuarioId));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(java.util.List.of()); // devolver lista vacía si no tiene perfil
        }
    }

    @PostMapping("/enviar/{id}")
    public Solicitud enviar(@PathVariable Long id) {
        return solicitudService.enviarSolicitud(id);
    }

    @PostMapping("/aprobar/{id}")
    public Solicitud aprobar(@PathVariable Long id) {
        return solicitudService.aprobarSolicitud(id);
    }

    @PostMapping("/rechazar/{id}")
    public Solicitud rechazar(@PathVariable Long id) {
        return solicitudService.rechazarSolicitud(id);
    }

    @GetMapping
    public List<Solicitud> listar() {
        return solicitudService.listarSolicitudes();
    }

    @GetMapping("/estudiante/{estudianteId}")
    public List<Solicitud> listarPorEstudiante(@PathVariable Long estudianteId) {
        return solicitudService.listarPorEstudiante(estudianteId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> obtener(@PathVariable Long id) {
        return solicitudService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
