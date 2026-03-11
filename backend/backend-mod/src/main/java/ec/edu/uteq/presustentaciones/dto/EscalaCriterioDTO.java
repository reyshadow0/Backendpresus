package ec.edu.uteq.presustentaciones.dto;

import lombok.Data;

@Data
public class EscalaCriterioDTO {
    private Long criterioId;
    /** 100, 67, 33 o 0 */
    private Integer escala;
    private String observaciones;
}
