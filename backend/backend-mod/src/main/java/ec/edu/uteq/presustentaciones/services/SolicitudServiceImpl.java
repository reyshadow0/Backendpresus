package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Estudiante;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.repositories.EstudianteRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final EstudianteRepository estudianteRepository;

    @Override
    public Solicitud crearSolicitud(Long estudianteId, Solicitud datos) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + estudianteId));

        datos.setEstado("CREADA");
        datos.setEstudiante(estudiante);
        datos.setCreadoPor(estudiante.getUsuario());
        datos.setActualizadoPor(estudiante.getUsuario());
        datos.setFechaRegistro(LocalDateTime.now());
        datos.setActualizadoEn(LocalDateTime.now());
        return solicitudRepository.save(datos);
    }

    @Override
    public Solicitud crearSolicitudPorUsuario(Long usuarioId, Solicitud datos) {
        Estudiante estudiante = estudianteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("No existe perfil de estudiante para el usuario ID: " + usuarioId));
        return crearSolicitud(estudiante.getId(), datos);
    }

    @Override
    public List<Solicitud> listarPorUsuario(Long usuarioId) {
        Estudiante estudiante = estudianteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("No existe perfil de estudiante para el usuario ID: " + usuarioId));
        return solicitudRepository.findByEstudianteId(estudiante.getId());
    }

    @Override
    public Solicitud enviarSolicitud(Long solicitudId) {
        Solicitud s = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        s.setEstado("ENVIADA");
        return solicitudRepository.save(s);
    }

    @Override
    public Solicitud aprobarSolicitud(Long solicitudId) {
        Solicitud s = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        s.setEstado("APROBADA");
        return solicitudRepository.save(s);
    }

    @Override
    public Solicitud rechazarSolicitud(Long solicitudId) {
        Solicitud s = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        s.setEstado("RECHAZADA");
        return solicitudRepository.save(s);
    }

    @Override
    public List<Solicitud> listarSolicitudes() {
        return solicitudRepository.findAll();
    }

    @Override
    public List<Solicitud> listarPorEstudiante(Long estudianteId) {
        return solicitudRepository.findByEstudianteId(estudianteId);
    }

    @Override
    public Optional<Solicitud> obtenerPorId(Long id) {
        return solicitudRepository.findById(id);
    }
}
