package ec.edu.uteq.presustentaciones.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jurados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jurado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    /** Roles posibles: PRESIDENTE, VOCAL_1, VOCAL_2 */
    @Column(name = "rol", nullable = false, length = 30)
    private String rol;

    @Column(name = "confirmado", nullable = false)
    @Builder.Default
    private boolean confirmado = false;

    @Column(name = "asignado_en", nullable = false, updatable = false)
    private LocalDateTime asignadoEn;

    @PrePersist
    protected void onCreate() {
        asignadoEn = LocalDateTime.now();
    }
}
