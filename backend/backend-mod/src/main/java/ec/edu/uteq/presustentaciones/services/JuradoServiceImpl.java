package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Docente;
import ec.edu.uteq.presustentaciones.entities.Jurado;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.entities.Tutor;
import ec.edu.uteq.presustentaciones.repositories.DocenteRepository;
import ec.edu.uteq.presustentaciones.repositories.JuradoRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import ec.edu.uteq.presustentaciones.repositories.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JuradoServiceImpl implements JuradoService {

    private final JuradoRepository juradoRepository;
    private final TutorRepository tutorRepository;
    private final DocenteRepository docenteRepository;
    private final SolicitudRepository solicitudRepository;

    // ── Jurados ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Jurado asignarJurado(Long solicitudId, Long docenteId, String rol) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + solicitudId));
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado: " + docenteId));

        // Verificar que el docente no esté ya asignado como jurado en esta solicitud
        boolean yaAsignado = juradoRepository.findBySolicitudId(solicitudId).stream()
                .anyMatch(j -> j.getDocente().getId().equals(docenteId));
        if (yaAsignado) {
            throw new RuntimeException("El docente ya está asignado como jurado en esta solicitud.");
        }

        // Verificar rol válido
        List<String> rolesValidos = List.of("PRESIDENTE", "VOCAL_1", "VOCAL_2");
        if (!rolesValidos.contains(rol.toUpperCase())) {
            throw new RuntimeException("Rol inválido. Use: PRESIDENTE, VOCAL_1 o VOCAL_2");
        }

        // Verificar que el rol no esté ocupado
        boolean rolOcupado = juradoRepository.findBySolicitudId(solicitudId).stream()
                .anyMatch(j -> j.getRol().equalsIgnoreCase(rol));
        if (rolOcupado) {
            throw new RuntimeException("El rol '" + rol + "' ya está asignado en esta solicitud.");
        }

        Jurado jurado = Jurado.builder()
                .solicitud(solicitud)
                .docente(docente)
                .rol(rol.toUpperCase())
                .confirmado(false)
                .build();

        // Incrementar carga horaria del docente
        docente.setCargaHorariaSemanal(docente.getCargaHorariaSemanal() + 1);
        docenteRepository.save(docente);

        return juradoRepository.save(jurado);
    }

    @Override
    public List<Jurado> listarPorSolicitud(Long solicitudId) {
        return juradoRepository.findBySolicitudId(solicitudId);
    }

    @Override
    public List<Jurado> listarTodos() {
        return juradoRepository.findAll();
    }

    @Override
    @Transactional
    public void eliminarJurado(Long juradoId) {
        Jurado jurado = juradoRepository.findById(juradoId)
                .orElseThrow(() -> new RuntimeException("Jurado no encontrado: " + juradoId));
        // Decrementar carga
        Docente docente = jurado.getDocente();
        int nuevaCarga = Math.max(0, docente.getCargaHorariaSemanal() - 1);
        docente.setCargaHorariaSemanal(nuevaCarga);
        docenteRepository.save(docente);
        juradoRepository.deleteById(juradoId);
    }

    // ── Tutor ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Tutor asignarTutor(Long solicitudId, Long docenteId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + solicitudId));
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado: " + docenteId));

        // Si ya existe tutor, reemplazarlo
        tutorRepository.findBySolicitudId(solicitudId).ifPresent(t -> {
            t.setEstado("REEMPLAZADO");
            tutorRepository.save(t);
        });

        Tutor tutor = Tutor.builder()
                .solicitud(solicitud)
                .docente(docente)
                .estado("ACTIVO")
                .build();
        return tutorRepository.save(tutor);
    }

    @Override
    public Optional<Tutor> obtenerTutorDeSolicitud(Long solicitudId) {
        return tutorRepository.findBySolicitudId(solicitudId)
                .filter(t -> "ACTIVO".equals(t.getEstado()));
    }

    @Override
    @Transactional
    public void eliminarTutor(Long tutorId) {
        tutorRepository.deleteById(tutorId);
    }

    // ── Sugerencia automática ─────────────────────────────────────────────────

    @Override
    public List<Docente> sugerirDocentes(Long solicitudId, int cantidad) {
        // Obtener docentes ya asignados en esta solicitud (jurados + tutor)
        List<Long> idsOcupados = new ArrayList<>();
        juradoRepository.findBySolicitudId(solicitudId)
                .forEach(j -> idsOcupados.add(j.getDocente().getId()));
        tutorRepository.findBySolicitudId(solicitudId)
                .ifPresent(t -> idsOcupados.add(t.getDocente().getId()));

        // Intentar primero con disponible=true, si no hay suficientes usar todos
        List<Docente> candidatos = docenteRepository.findDisponiblesOrdenadosPorCarga().stream()
                .filter(d -> !idsOcupados.contains(d.getId()))
                .collect(Collectors.toList());

        if (candidatos.size() < cantidad) {
            // Fallback: usar todos los docentes ordenados por carga
            candidatos = docenteRepository.findTodosOrdenadosPorCarga().stream()
                    .filter(d -> !idsOcupados.contains(d.getId()))
                    .collect(Collectors.toList());
        }

        return candidatos.stream().limit(cantidad).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void asignarJuradosAutomaticamente(Long solicitudId) {
        // Excluir roles ya asignados
        List<String> rolesOcupados = juradoRepository.findBySolicitudId(solicitudId)
                .stream().map(Jurado::getRol).collect(Collectors.toList());
        List<String> rolesFaltantes = new ArrayList<>(List.of("PRESIDENTE", "VOCAL_1", "VOCAL_2"))
                .stream().filter(r -> !rolesOcupados.contains(r)).collect(Collectors.toList());

        if (rolesFaltantes.isEmpty()) return; // ya completo

        List<Docente> sugeridos = sugerirDocentes(solicitudId, rolesFaltantes.size());

        if (sugeridos.size() < rolesFaltantes.size()) {
            throw new RuntimeException(
                "No hay suficientes docentes para asignar automáticamente. " +
                "Disponibles: " + sugeridos.size() + ", requeridos: " + rolesFaltantes.size()
            );
        }

        for (int i = 0; i < rolesFaltantes.size(); i++) {
            asignarJurado(solicitudId, sugeridos.get(i).getId(), rolesFaltantes.get(i));
        }
    }

    @Override
    public List<Jurado> listarPorDocente(Long docenteId) {
        return juradoRepository.findByDocenteId(docenteId);
    }

    @Override
    public List<Tutor> listarTutoriasPorDocente(Long docenteId) {
        return tutorRepository.findByDocenteId(docenteId);
    }
}
