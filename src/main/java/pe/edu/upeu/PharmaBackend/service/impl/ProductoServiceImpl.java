package pe.edu.upeu.PharmaBackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.ProductoResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.entity.Producto;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.repository.ProductoRepository;
import pe.edu.upeu.PharmaBackend.service.service.ProductoService;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository){
        this.productoRepository= productoRepository;
        this.categoriaRepository=categoriaRepository;
    }

    @Override
    @Transactional
    public ProductoResponseDTO create(ProductoRequestDTO dto){
        String nombre =dto.getNombre().trim();

        if (productoRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException("Ya existe un producto con el nombre" + nombre);
        }

        Categoria categoria=buscarCategoria(dto.getId_categoria());

        if (!categoria.getEstado()) {
            throw new ReglaNegocioException("No se puede vincular el producto a una categoria inactiva");
        }

        Producto producto=new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setEstado(dto.getEstado());
        producto.setCategoria(categoria);

        Producto productocreado=productoRepository.save(producto);

        return convertirResponse(productocreado);
    };

    @Override
    @Transactional
    public ProductoResponseDTO update(Long id, ProductoRequestDTO dto){
        Producto producto=productoRepository.findById(id)
                .orElseThrow(()->
                        new RecursosNoEncontradosException("Producto no encontrado con el id:"+ id));

        String nombre= dto.getNombre().trim();
        if (productoRepository.existsByNombreIgnoreCaseAndIdNot(nombre,id)){
            throw new ReglaNegocioException("Ya existe un producto con el nombre: "+nombre);
        }

        Categoria categoria=buscarCategoria(dto.getId_categoria());

        if (!categoria.getEstado()) {
            throw new ReglaNegocioException("No se puede vincular el producto a una categoria inactiva");
        }
        producto.setNombre(nombre);
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setEstado(dto.getEstado());
        producto.setCategoria(categoria);

        Producto productoActualizado=productoRepository.save(producto);
        return convertirResponse(productoActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoResponseDTO> read(Long id){
        return productoRepository.findById(id)
                .map(this::convertirResponse);
    }

    @Override
    @Transactional
    public void delete (Long id){
        Producto producto=productoRepository.findById(id)
                .orElseThrow(()->new RecursosNoEncontradosException("Producto no encontrado con el id:"+ id));
        if (!producto.isEstado()) {
            throw new ReglaNegocioException("El producto ya se encuentra inactivo");
        }
        producto.setEstado(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Iterable<ProductoResponseDTO> readAll(){
        return productoRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    private Categoria buscarCategoria(Long id_categoria){
        return categoriaRepository.findById(id_categoria)
                .orElseThrow(()->new RecursosNoEncontradosException("Categoria no encontrada con id:"+ id_categoria));

    }

    private ProductoResponseDTO convertirResponse(Producto producto){
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.isEstado(),
                producto.getFecha_creacion(),
                producto.getFecha_modificacion(),
                producto.getCategoria().getId()
                );
    }

}

