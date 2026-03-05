package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Anteproyecto;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.repositories.AnteproyectoRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnteproyectoServiceImpl implements AnteproyectoService {

    private final AnteproyectoRepository anteproyectoRepository;
    private final SolicitudRepository solicitudRepository;

    @Value("${app.upload.dir:uploads/anteproyectos}")
    private String uploadDir;

    public AnteproyectoServiceImpl(AnteproyectoRepository ar, SolicitudRepository sr) {
        this.anteproyectoRepository = ar;
        this.solicitudRepository = sr;
    }

    @Override
    public Anteproyecto enviarAnteproyecto(Long solicitudId, MultipartFile archivo) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        Path dirPath = Paths.get(uploadDir);
        try { Files.createDirectories(dirPath); } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", e);
        }

        String nombreArchivo = "solicitud_" + solicitudId + "_" + UUID.randomUUID() + ".pdf";
        Path rutaArchivo = dirPath.resolve(nombreArchivo);

        // RF-02: Calcular SHA-256 mientras se guarda el archivo (un solo pass)
        String sha256 = calcularSha256YGuardar(archivo, rutaArchivo);

        Anteproyecto anteproyecto = anteproyectoRepository
                .findBySolicitudId(solicitudId)
                .orElse(new Anteproyecto());

        anteproyecto.setArchivoPdf(nombreArchivo);
        anteproyecto.setFechaEnvio(LocalDate.now());
        anteproyecto.setEstado("ENVIADO");
        anteproyecto.setSolicitud(solicitud);
        anteproyecto.setSha256Hash(sha256);
        anteproyecto.setTamanoBytes(archivo.getSize());

        return anteproyectoRepository.save(anteproyecto);
    }

    /** Guarda el archivo y computa el hash SHA-256 en un solo paso */
    private String calcularSha256YGuardar(MultipartFile archivo, Path destino) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = archivo.getInputStream();
                 DigestInputStream dis = new DigestInputStream(is, digest)) {
                Files.copy(dis, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Error al calcular SHA-256 del archivo", e);
        }
    }

    /** RF-02: Verificar integridad — recalcula el hash del archivo en disco y compara */
    @Override
    public boolean verificarIntegridad(Long solicitudId) {
        Anteproyecto ap = anteproyectoRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new RuntimeException("Anteproyecto no encontrado"));

        if (ap.getSha256Hash() == null) return false;

        Path rutaArchivo = Paths.get(uploadDir).resolve(ap.getArchivoPdf()).normalize();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(rutaArchivo);
            String hashActual = HexFormat.of().formatHex(digest.digest(bytes));
            return hashActual.equals(ap.getSha256Hash());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Anteproyecto aprobarAnteproyecto(Long id, String obs) {
        Anteproyecto ap = anteproyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anteproyecto no encontrado"));
        ap.setEstado("APROBADO"); ap.setObservaciones(obs);
        return anteproyectoRepository.save(ap);
    }

    @Override
    public Anteproyecto rechazarAnteproyecto(Long id, String obs) {
        Anteproyecto ap = anteproyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anteproyecto no encontrado"));
        ap.setEstado("RECHAZADO"); ap.setObservaciones(obs);
        return anteproyectoRepository.save(ap);
    }

    @Override
    public Optional<Anteproyecto> buscarPorSolicitud(Long solicitudId) {
        return anteproyectoRepository.findBySolicitudId(solicitudId);
    }
}
