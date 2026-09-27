package pe.edu.upeu.PharmaBackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter@Setter
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max=50, message = "EL nombre debe tener entre 2 y 50 caracteres")
    protected String nombre;

    @NotNull(message = "El precio es obligatorio")
    private BigDecimal precio;

    private int stock;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;

    @NotNull(message = "El producto debe pertenecer a una categoria")
    private Long id_categoria;
}
