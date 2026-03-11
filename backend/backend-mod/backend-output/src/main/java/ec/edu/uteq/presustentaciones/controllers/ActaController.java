package ec.edu.uteq.presustentaciones.controllers;

import ec.edu.uteq.presustentaciones.entities.Acta;
import ec.edu.uteq.presustentaciones.services.ActaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/actas")
public class ActaController {

    private final ActaService actaService;

    public ActaController(ActaService actaService) {
        this.actaService = actaService;
    }

    @PostMapping("/generar/{solicitudId}")
    public Acta generarActa(@PathVariable Long solicitudId) {
        return actaService.generarActa(solicitudId);
    }

    @PostMapping("/firmar/{actaId}")
    public Acta firmarActa(@PathVariable Long actaId) {
        return actaService.firmarActa(actaId);
    }

    @GetMapping
    public List<Acta> listar() {
        return actaService.listarActas();
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<Acta> porSolicitud(@PathVariable Long solicitudId) {
        return actaService.buscarPorSolicitud(solicitudId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
