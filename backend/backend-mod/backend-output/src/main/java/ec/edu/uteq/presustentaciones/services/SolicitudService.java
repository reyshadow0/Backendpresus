package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Solicitud;
import java.util.List;
import java.util.Optional;

public interface SolicitudService {
    Solicitud crearSolicitud(Long estudianteId, Solicitud datos);
    Solicitud enviarSolicitud(Long solicitudId);
    Solicitud aprobarSolicitud(Long solicitudId);
    Solicitud rechazarSolicitud(Long solicitudId);
    List<Solicitud> listarSolicitudes();
    List<Solicitud> listarPorEstudiante(Long estudianteId);
    Optional<Solicitud> obtenerPorId(Long id);
}
