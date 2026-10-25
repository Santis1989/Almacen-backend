
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.ProductoRequestDTO;
import ies.belgrano.almacen.dto.ProductoResponseDTO;
import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import ies.belgrano.almacen.repository.DetalleVentaRepository;
@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            DetalleVentaRepository detalleVentaRepository) {

        this.productoRepository = productoRepository;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    // Consultar todos los productos
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    // Buscar producto por ID
    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    // Crear un producto nuevo
    public ProductoResponseDTO crear(ProductoRequestDTO datos) {

        Producto producto = new Producto();

        copiarDatos(datos, producto);

        Producto guardado = productoRepository.save(producto);

        return convertirAResponseDTO(guardado);
    }

    // Actualizar un producto existente
    public Optional<ProductoResponseDTO> actualizar(
            Integer id, ProductoRequestDTO datos) {

        return productoRepository.findById(id).map(producto -> {

            copiarDatos(datos, producto);

            Producto actualizado = productoRepository.save(producto);

            return convertirAResponseDTO(actualizado);
        });
    }

    // Copiar los campos permitidos
    private void copiarDatos(ProductoRequestDTO datos, Producto producto) {

        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecioCompra(datos.getPrecioCompra());
        producto.setPrecioVenta(datos.getPrecioVenta());
        producto.setStock(datos.getStock());
        producto.setStockMinimo(datos.getStockMinimo());
    }

    // Convertir entidad a DTO de respuesta
    public ProductoResponseDTO convertirAResponseDTO(Producto producto) {

        return new ProductoResponseDTO(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecioCompra(),
                producto.getPrecioVenta(),
                producto.getStock(),
                producto.getStockMinimo()
        );
    }

    // Eliminar producto
    public void eliminar(Integer id) {
        productoRepository.deleteById(id);
    }
    public boolean tieneVentasAsociadas(Integer idProducto) {
        return detalleVentaRepository.existsByProducto_IdProducto(idProducto);
    }
}
