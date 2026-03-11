package ec.edu.uteq.presustentaciones.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "nota_final")
    private Double notaFinal;

    /** Valores posibles: APROBADO, REPROBADO */
    @Column(name = "resultado", length = 20)
    private String resultado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitud_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "creadoPor", "actualizadoPor"})
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rubrica_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Rubrica rubrica;
}
