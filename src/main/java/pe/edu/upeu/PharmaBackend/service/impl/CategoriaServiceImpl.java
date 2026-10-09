package pe.edu.upeu.PharmaBackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.CategoriaResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.repository.ProductoRepository;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public CategoriaResponseDTO create(CategoriaRequestDTO dto) {

        String nombre = dto.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una categoria con el nombre " + nombre);
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setEstado(dto.getEstado());

        Categoria categoriaCreada = categoriaRepository.save(categoria);

        return convertirResponse(categoriaCreada);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO update(Long id, CategoriaRequestDTO dto) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursosNoEncontradosException("Categoria no encontrada con id: " + id)
                );

        String nombre = dto.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe otra categoria con el nombre " + nombre);
        }

        categoria.setNombre(nombre);
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setEstado(dto.getEstado());

        Categoria categoriaActualizada = categoriaRepository.save(categoria);

        return convertirResponse(categoriaActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaResponseDTO> read(Long id) {
        return categoriaRepository.findById(id)
                .map(this::convertirResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursosNoEncontradosException("Categoria no encontrada con id: " + id)
                );

        if (productoRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar la categorÃ­a porque tiene productos registrados");
        }

        categoriaRepository.delete(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<CategoriaResponseDTO> readAll() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    private CategoriaResponseDTO convertirResponse(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getEstado(),
                categoria.getFechaCreacion(),
                categoria.getFechaModificacion()
        );
    }
}

