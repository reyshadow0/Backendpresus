package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Acta;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.repositories.ActaRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActaServiceImpl implements ActaService {

    private final ActaRepository actaRepository;
    private final SolicitudRepository solicitudRepository;

    @Override
    public Acta generarActa(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        return actaRepository.save(Acta.builder()
                .fechaGeneracion(LocalDate.now()).firmada(false)
                .archivoPdf("acta_solicitud_" + solicitudId + ".pdf")
                .solicitud(solicitud).build());
    }

    @Override
    public Acta firmarActa(Long actaId) {
        Acta acta = actaRepository.findById(actaId)
                .orElseThrow(() -> new RuntimeException("Acta no encontrada"));
        acta.setFirmada(true);
        return actaRepository.save(acta);
    }

    @Override
    public List<Acta> listarActas() {
        return actaRepository.findAll();
    }

    @Override
    public Optional<Acta> buscarPorSolicitud(Long solicitudId) {
        return actaRepository.findBySolicitudId(solicitudId);
    }
}
