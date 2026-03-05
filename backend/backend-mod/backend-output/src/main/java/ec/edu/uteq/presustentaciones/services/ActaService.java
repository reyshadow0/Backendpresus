package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Acta;
import java.util.List;
import java.util.Optional;

public interface ActaService {
    Acta generarActa(Long solicitudId);
    Acta firmarActa(Long actaId);
    List<Acta> listarActas();
    Optional<Acta> buscarPorSolicitud(Long solicitudId);
}
