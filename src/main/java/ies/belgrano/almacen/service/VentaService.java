package ies.belgrano.almacen.service;

import ies.belgrano.almacen.model.DetalleVenta;
import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.model.Venta;
import ies.belgrano.almacen.repository.DetalleVentaRepository;
import ies.belgrano.almacen.repository.ProductoRepository;
import ies.belgrano.almacen.repository.VentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;


    public VentaService(
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            ProductoRepository productoRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoRepository = productoRepository;
    }


    // ==================================================
    // LISTAR TODAS LAS VENTAS
    // ==================================================

    public List<Venta> listarTodas() {

        return ventaRepository.findAll();
    }


    // ==================================================
    // BUSCAR VENTA POR ID
    // ==================================================

    public Optional<Venta> buscarPorId(Integer idVenta) {

        return ventaRepository.findById(idVenta);
    }


    // ==================================================
    // LISTAR DETALLES DE UNA VENTA
    // ==================================================

    public List<DetalleVenta> listarDetalles(Integer idVenta) {

        return detalleVentaRepository.findByIdVenta(idVenta);
    }


    // ==================================================
    // REGISTRAR VENTA
    // ==================================================

    @Transactional
    public Venta registrarVenta(
            Venta venta,
            List<DetalleVenta> detalles
    ) {

        // ----------------------------------------------
        // 1. GUARDAR LA VENTA
        // ----------------------------------------------

        Venta ventaGuardada =
                ventaRepository.save(venta);

        Integer idVenta =
                ventaGuardada.getIdVenta();


        // ----------------------------------------------
        // 2. RECORRER LOS DETALLES
        // ----------------------------------------------

        for (DetalleVenta detalle : detalles) {

            // Asignar el ID de la venta
            detalle.setIdVenta(idVenta);


            // ------------------------------------------
            // 3. BUSCAR EL PRODUCTO
            // ------------------------------------------

            Producto producto =
                    productoRepository
                            .findById(detalle.getIdProducto())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Producto no encontrado: "
                                                    + detalle.getIdProducto()
                                    )
                            );


            // ------------------------------------------
            // 4. DESCONTAR STOCK
            // ------------------------------------------

            double cantidadVendida =
                    detalle.getCantidad().doubleValue();

            double nuevoStock =
                    producto.getStock() - cantidadVendida;

            producto.setStock(nuevoStock);

            productoRepository.save(producto);


            // ------------------------------------------
            // 5. CALCULAR SUBTOTAL DEL DETALLE
            // ------------------------------------------

            detalle.setSubtotal(
                    detalle.getCantidad()
                            .multiply(detalle.getPrecio())
            );


            // ------------------------------------------
            // 6. GUARDAR DETALLE
            // ------------------------------------------

            detalleVentaRepository.save(detalle);
        }


        // ----------------------------------------------
        // 7. DEVOLVER LA VENTA GUARDADA
        // ----------------------------------------------

        return ventaGuardada;
    }
}