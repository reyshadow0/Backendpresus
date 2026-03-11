package ec.edu.uteq.presustentaciones.controllers;

import ec.edu.uteq.presustentaciones.entities.Anteproyecto;
import ec.edu.uteq.presustentaciones.services.AnteproyectoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/anteproyectos")
public class AnteproyectoController {

    private final AnteproyectoService anteproyectoService;

    @Value("${app.upload.dir:uploads/anteproyectos}")
    private String uploadDir;

    public AnteproyectoController(AnteproyectoService anteproyectoService) {
        this.anteproyectoService = anteproyectoService;
    }

    /** Estudiante sube el PDF de su anteproyecto */
    @PostMapping(value = "/enviar/{solicitudId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Anteproyecto> enviarAnteproyecto(
            @PathVariable Long solicitudId,
            @RequestParam("archivo") MultipartFile archivo) {
        Anteproyecto resultado = anteproyectoService.enviarAnteproyecto(solicitudId, archivo);
        return ResponseEntity.ok(resultado);
    }

    /** Docente/Admin: consulta el anteproyecto de una solicitud */
    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<?> obtenerPorSolicitud(@PathVariable Long solicitudId) {
        return anteproyectoService.buscarPorSolicitud(solicitudId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Docente/Admin: descarga/visualiza el PDF en el navegador */
    @GetMapping("/ver/{solicitudId}")
    public ResponseEntity<Resource> verPdf(@PathVariable Long solicitudId) {
        Anteproyecto anteproyecto = anteproyectoService.buscarPorSolicitud(solicitudId)
                .orElseThrow(() -> new RuntimeException("Anteproyecto no encontrado para solicitud " + solicitudId));

        try {
            Path rutaArchivo = Paths.get(uploadDir).resolve(anteproyecto.getArchivoPdf()).normalize();
            Resource resource = new UrlResource(rutaArchivo.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    // inline → el navegador lo muestra; usar attachment para forzar descarga
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + anteproyecto.getArchivoPdf() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/aprobar/{id}")
    public ResponseEntity<Anteproyecto> aprobar(@PathVariable Long id,
                                                @RequestParam String observaciones) {
        return ResponseEntity.ok(anteproyectoService.aprobarAnteproyecto(id, observaciones));
    }

    @PostMapping("/rechazar/{id}")
    public ResponseEntity<Anteproyecto> rechazar(@PathVariable Long id,
                                                 @RequestParam String observaciones) {
        return ResponseEntity.ok(anteproyectoService.rechazarAnteproyecto(id, observaciones));
    }
}
