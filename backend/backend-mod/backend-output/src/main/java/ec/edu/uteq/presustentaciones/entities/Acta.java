package ec.edu.uteq.presustentaciones.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "actas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Acta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDate fechaGeneracion;

    @Column(name = "archivo_pdf")
    private String archivoPdf;

    @Column(name = "firmada", nullable = false)
    @Builder.Default
    private boolean firmada = false;

    @OneToOne
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;
}
