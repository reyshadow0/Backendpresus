package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Anteproyecto;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.repositories.AnteproyectoRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnteproyectoServiceImpl implements AnteproyectoService {

    private final AnteproyectoRepository anteproyectoRepository;
    private final SolicitudRepository solicitudRepository;

    @Value("${app.upload.dir:uploads/anteproyectos}")
    private String uploadDir;

    public AnteproyectoServiceImpl(AnteproyectoRepository anteproyectoRepository,
                                   SolicitudRepository solicitudRepository) {
        this.anteproyectoRepository = anteproyectoRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    public Anteproyecto enviarAnteproyecto(Long solicitudId, MultipartFile archivo) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Crear directorio si no existe
        Path dirPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", e);
        }

        // Nombre único para evitar colisiones
        String nombreArchivo = "solicitud_" + solicitudId + "_" + UUID.randomUUID() + ".pdf";
        Path rutaArchivo = dirPath.resolve(nombreArchivo);

        try {
            Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo PDF", e);
        }

        // Si ya existe un anteproyecto para esta solicitud, actualízalo
        Anteproyecto anteproyecto = anteproyectoRepository
                .findBySolicitudId(solicitudId)
                .orElse(new Anteproyecto());

        anteproyecto.setArchivoPdf(nombreArchivo);
        anteproyecto.setFechaEnvio(LocalDate.now());
        anteproyecto.setEstado("ENVIADO");
        anteproyecto.setSolicitud(solicitud);

        return anteproyectoRepository.save(anteproyecto);
    }

    @Override
    public Anteproyecto aprobarAnteproyecto(Long anteproyectoId, String observaciones) {
        Anteproyecto anteproyecto = anteproyectoRepository.findById(anteproyectoId)
                .orElseThrow(() -> new RuntimeException("Anteproyecto no encontrado"));
        anteproyecto.setEstado("APROBADO");
        anteproyecto.setObservaciones(observaciones);
        return anteproyectoRepository.save(anteproyecto);
    }

    @Override
    public Anteproyecto rechazarAnteproyecto(Long anteproyectoId, String observaciones) {
        Anteproyecto anteproyecto = anteproyectoRepository.findById(anteproyectoId)
                .orElseThrow(() -> new RuntimeException("Anteproyecto no encontrado"));
        anteproyecto.setEstado("RECHAZADO");
        anteproyecto.setObservaciones(observaciones);
        return anteproyectoRepository.save(anteproyecto);
    }

    @Override
    public Optional<Anteproyecto> buscarPorSolicitud(Long solicitudId) {
        return anteproyectoRepository.findBySolicitudId(solicitudId);
    }
}
