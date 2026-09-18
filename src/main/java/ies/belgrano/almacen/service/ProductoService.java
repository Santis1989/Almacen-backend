package ies.belgrano.almacen.service;

import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Listar todos los productos
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    // Buscar producto por ID
    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    // Guardar un producto nuevo o actualizar uno existente
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    // Eliminar producto
    public void eliminar(Integer id) {
        productoRepository.deleteById(id);
    }
}