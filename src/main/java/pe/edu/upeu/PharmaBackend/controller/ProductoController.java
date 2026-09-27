package pe.edu.upeu.PharmaBackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.ProductoResponseDTO;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackend.service.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService){
        this.productoService=productoService;
    }

    @GetMapping
    public Iterable<ProductoResponseDTO> getProductos(){
        return productoService.readAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponseDTO crearProducto(@Valid @RequestBody ProductoRequestDTO dto){
        return productoService.create(dto);
    }


    @GetMapping("/{id}")
    public ProductoResponseDTO getIdProductos(@PathVariable Long id) {
        return productoService.read(id)
                .orElseThrow(() ->
                        new RecursosNoEncontradosException("Producto no encontrada con id: " + id)
                );
    }
    @PutMapping("/{id}")
    public ProductoResponseDTO actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO dto
    ){
        return productoService.update(id,dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable Long id){
        productoService.delete(id);
    }
}
