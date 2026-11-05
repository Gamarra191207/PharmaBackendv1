package pe.edu.upeu.PharmaBackend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.CategoriaResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaServiceImpl.class);
    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categorias", allEntries = true)
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
    @CacheEvict(cacheNames = { "categorias", "productos" }, allEntries = true)
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
    @Cacheable(cacheNames = "categorias", key = "'id-' + #id")
    public Optional<CategoriaResponseDTO> read(Long id) {
        return categoriaRepository.findById(id)
                .map(this::convertirResponse);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categorias", allEntries = true)
    public void delete(Long id) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursosNoEncontradosException("Categoria no encontrada con id: " + id)
                );

        categoriaRepository.delete(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categorias", key = "'todas'")
    public Iterable<CategoriaResponseDTO> readAll() {
        log.info("Inicio listar categorias");
        long inicio = System.currentTimeMillis();

        List<CategoriaResponseDTO> resultado = categoriaRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();

        log.info("Fin listar categorias duracionMs={}", System.currentTimeMillis() - inicio);
        return resultado;
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
