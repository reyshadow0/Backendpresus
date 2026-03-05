package ec.edu.uteq.presustentaciones.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registra la nota que un JURADO asigna a UN criterio de la rúbrica
 * para una solicitud específica.
 * Escalas válidas: 100, 67, 33, 0  (% del puntaje máximo del criterio)
 */
@Entity
@Table(name = "evaluaciones_criterio",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"solicitud_id", "jurado_id", "criterio_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvaluacionCriterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Escala aplicada: 100, 67, 33 o 0 */
    @Column(name = "escala", nullable = false)
    private Integer escala;

    /** Nota calculada = criterio.ponderacion * escala / 100  (sobre la base del criterio) */
    @Column(name = "nota_obtenida", nullable = false)
    private Double notaObtenida;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "registrado_en", nullable = false, updatable = false)
    private LocalDateTime registradoEn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitud_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "jurados", "tutor",
                           "evaluacion", "acta", "anteproyecto", "cronograma", "notificaciones"})
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jurado_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "solicitud"})
    private Jurado jurado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "criterio_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "rubrica"})
    private CriterioRubrica criterio;

    @PrePersist
    protected void onCreate() {
        registradoEn = LocalDateTime.now();
    }

    /** Escalas válidas según la rúbrica institucional */
    public static final int[] ESCALAS_VALIDAS = {100, 67, 33, 0};

    public static boolean esEscalaValida(int escala) {
        for (int e : ESCALAS_VALIDAS) if (e == escala) return true;
        return false;
    }
}
