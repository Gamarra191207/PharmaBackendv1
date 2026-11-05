package pe.edu.upeu.PharmaBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductoResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long Id;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private Boolean estado;
    private LocalDateTime fecha_creacion;
    private LocalDateTime fecha_modificacion;
    private Long id_categoria;
}
