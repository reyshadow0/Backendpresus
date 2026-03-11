package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Docente;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.entities.Tutor;
import ec.edu.uteq.presustentaciones.repositories.DocenteRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import ec.edu.uteq.presustentaciones.repositories.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final SolicitudRepository solicitudRepository;
    private final DocenteRepository docenteRepository;

    @Override
    public Tutor asignarTutor(Long solicitudId, Long docenteId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + solicitudId));
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado: " + docenteId));

        // Reasignar si ya existe
        tutorRepository.findBySolicitudId(solicitudId).ifPresent(t -> tutorRepository.delete(t));

        Tutor tutor = Tutor.builder()
                .solicitud(solicitud)
                .docente(docente)
                .estado("ACTIVO")
                .build();
        return tutorRepository.save(tutor);
    }

    @Override
    public Optional<Tutor> buscarPorSolicitud(Long solicitudId) {
        return tutorRepository.findBySolicitudId(solicitudId);
    }

    @Override
    public List<Tutor> listarTodos() {
        return tutorRepository.findAll();
    }

    @Override
    public void eliminarTutor(Long tutorId) {
        tutorRepository.deleteById(tutorId);
    }
}
