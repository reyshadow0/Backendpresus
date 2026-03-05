package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Notificacion;
import ec.edu.uteq.presustentaciones.entities.Usuario;
import ec.edu.uteq.presustentaciones.repositories.NotificacionRepository;
import ec.edu.uteq.presustentaciones.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public Notificacion crearNotificacion(Long usuarioId, String mensaje) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return notificacionRepository.save(
            Notificacion.builder().usuario(usuario).mensaje(mensaje)
                .fecha(LocalDateTime.now()).leida(false).build());
    }

    @Override
    public List<Notificacion> listarNotificaciones() {
        return notificacionRepository.findAll();
    }

    @Override
    public List<Notificacion> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    @Override
    public long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Override
    public Notificacion marcarComoLeida(Long id) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        n.setLeida(true);
        return notificacionRepository.save(n);
    }

    @Override
    public void marcarTodasLeidas(Long usuarioId) {
        List<Notificacion> pendientes = notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
        pendientes.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(pendientes);
    }
}
