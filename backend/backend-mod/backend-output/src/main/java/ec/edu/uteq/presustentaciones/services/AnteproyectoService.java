package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Anteproyecto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface AnteproyectoService {

    Anteproyecto enviarAnteproyecto(Long solicitudId, MultipartFile archivo);

    Anteproyecto aprobarAnteproyecto(Long anteproyectoId, String observaciones);

    Anteproyecto rechazarAnteproyecto(Long anteproyectoId, String observaciones);

    Optional<Anteproyecto> buscarPorSolicitud(Long solicitudId);
}
