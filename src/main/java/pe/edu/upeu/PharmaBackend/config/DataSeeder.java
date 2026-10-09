package pe.edu.upeu.PharmaBackend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;
import pe.edu.upeu.PharmaBackend.service.service.ProductoService;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoriaService categoriaService;
    private final ProductoService productoService;

    public DataSeeder(CategoriaService categoriaService, ProductoService productoService) {
        this.categoriaService = categoriaService;
        this.productoService = productoService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only seed if empty
        if (((List<?>) categoriaService.readAll()).size() < 4) {
            System.out.println("Seeding categories...");
            
            CategoriaRequestDTO c1 = new CategoriaRequestDTO("Analgésicos y Antipiréticos", "Para dolor y fiebre", true);
            CategoriaRequestDTO c2 = new CategoriaRequestDTO("Antibióticos", "Para infecciones", true);
            CategoriaRequestDTO c3 = new CategoriaRequestDTO("Vitaminas y Suplementos", "Complementos nutricionales", true);
            CategoriaRequestDTO c4 = new CategoriaRequestDTO("Dermatológicos", "Para la piel", false); // INACTIVA

            Long id1 = categoriaService.create(c1).getId();
            Long id2 = categoriaService.create(c2).getId();
            Long id3 = categoriaService.create(c3).getId();
            Long id4 = categoriaService.create(c4).getId();

            System.out.println("Seeding products...");
            
            // 8 productos (al menos 1 dado de baja)
            productoService.create(new ProductoRequestDTO("Paracetamol 500mg", new BigDecimal("1.50"), 100, true, id1));
            productoService.create(new ProductoRequestDTO("Ibuprofeno 400mg", new BigDecimal("2.00"), 80, true, id1));
            
            productoService.create(new ProductoRequestDTO("Amoxicilina 500mg", new BigDecimal("5.00"), 50, true, id2));
            productoService.create(new ProductoRequestDTO("Azitromicina 500mg", new BigDecimal("8.50"), 30, true, id2));
            
            productoService.create(new ProductoRequestDTO("Vitamina C 1000mg", new BigDecimal("15.00"), 40, true, id3));
            productoService.create(new ProductoRequestDTO("Complejo B", new BigDecimal("12.00"), 60, true, id3));
            
            productoService.create(new ProductoRequestDTO("Crema Hidratante", new BigDecimal("25.00"), 20, true, id4));
            productoService.create(new ProductoRequestDTO("Protector Solar SPF 50", new BigDecimal("45.00"), 15, false, id4)); // BAJA LOGICA
            
            System.out.println("Seeding complete!");
        }
    }
}
