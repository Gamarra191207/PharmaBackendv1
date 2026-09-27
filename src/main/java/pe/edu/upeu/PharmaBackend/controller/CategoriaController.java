package pe.edu.upeu.PharmaBackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.CategoriaResponseDTO;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public Iterable<CategoriaResponseDTO> getCategorias() {
        return categoriaService.readAll();
    }

    @GetMapping("/{id}")
    public CategoriaResponseDTO getIdCategorias(@PathVariable Long id) {
        return categoriaService.read(id)
                .orElseThrow(() ->
                        new RecursosNoEncontradosException("Categoria no encontrada con id: " + id)
                );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponseDTO crearCategoria(@Valid @RequestBody CategoriaRequestDTO dto) {
        return categoriaService.create(dto);
    }

    @PutMapping("/{id}")
    public CategoriaResponseDTO actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO dto) {
        return categoriaService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCategoria(@PathVariable Long id) {
        categoriaService.delete(id);
    }
}