package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.DetalleVentaRequest;
import ies.belgrano.almacen.dto.DetalleVentaResponse;
import ies.belgrano.almacen.exception.ResourceNotFoundException;
import ies.belgrano.almacen.model.Cliente;
import ies.belgrano.almacen.model.DetalleVenta;
import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.model.Venta;
import ies.belgrano.almacen.repository.ClienteRepository;
import ies.belgrano.almacen.repository.DetalleVentaRepository;
import ies.belgrano.almacen.repository.ProductoRepository;
import ies.belgrano.almacen.repository.VentaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    public VentaService(
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            ProductoRepository productoRepository,
            ClienteRepository clienteRepository) {

        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
    }

    // LISTAR TODAS LAS VENTAS

    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    // BUSCAR VENTA POR ID

    public Optional<Venta> buscarPorId(Integer idVenta) {
        return ventaRepository.findById(idVenta);
    }

    // LISTAR DETALLES DE UNA VENTA

    @Transactional(readOnly = true)
    public List<DetalleVentaResponse> listarDetalles(Integer idVenta) {

        if (!ventaRepository.existsById(idVenta)) {
            throw new ResourceNotFoundException(
                    "Venta no encontrada: " + idVenta
            );
        }

        List<DetalleVenta> detalles =
                detalleVentaRepository.findByVenta_IdVenta(idVenta);

        List<DetalleVentaResponse> respuesta = new ArrayList<>();

        for (DetalleVenta detalle : detalles) {
            Producto producto = detalle.getProducto();

            DetalleVentaResponse dto = new DetalleVentaResponse(
                    detalle.getIdDetalle(),
                    producto.getIdProducto(),
                    producto.getNombre(),
                    detalle.getCantidad(),
                    detalle.getPrecio(),
                    detalle.getSubtotal()
            );

            respuesta.add(dto);
        }

        return respuesta;
    }

    // REGISTRAR VENTA

    @Transactional
    public Venta registrarVenta(
            Venta venta,
            List<DetalleVentaRequest> detalles) {

        if (venta == null) {
            throw new IllegalArgumentException(
                    "Los datos de la venta son obligatorios."
            );
        }

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException(
                    "La venta debe contener al menos un detalle."
            );
        }

        if (venta.getCliente() != null) {
            Integer idCliente = venta.getCliente().getIdCliente();

            if (idCliente == null) {
                throw new IllegalArgumentException(
                        "El identificador del cliente es obligatorio."
                );
            }

            Cliente cliente = clienteRepository.findById(idCliente)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado: " + idCliente
                    ));

            venta.setCliente(cliente);
        }
        if (venta.getIdVenta() != null) {
            throw new IllegalArgumentException(
                    "No debe enviarse un identificador al registrar una venta nueva."
            );
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        for (DetalleVentaRequest entrada : detalles) {

            if (entrada == null
                    || entrada.getIdProducto() == null
                    || entrada.getIdProducto() <= 0
                    || entrada.getCantidad() == null
                    || entrada.getCantidad().signum() <= 0) {

                throw new IllegalArgumentException(
                        "Cada detalle debe tener un producto válido "
                                + "y una cantidad positiva."
                );
            }

            Producto producto = productoRepository
                    .findById(entrada.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado: "
                                    + entrada.getIdProducto()
                    ));

            if (producto.getPrecioVenta() == null
                    || producto.getPrecioVenta().signum() < 0) {

                throw new IllegalArgumentException(
                        "El producto no tiene un precio de venta válido."
                );
            }

            if (producto.getStock() == null
                    || !Double.isFinite(producto.getStock())) {

                throw new IllegalArgumentException(
                        "El producto no tiene un stock válido."
                );
            }

            BigDecimal cantidad;

            try {
                cantidad = entrada.getCantidad()
                        .setScale(3, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException(
                        "La cantidad no puede tener más de 3 decimales significativos."
                );
            }

            if (cantidad.precision() > 12) {
                throw new IllegalArgumentException(
                        "La cantidad admite hasta 9 dígitos enteros."
                );
            }

            double cantidadVendida = cantidad.doubleValue();

            double nuevoStock =
                    producto.getStock() - cantidadVendida;

            if (!Double.isFinite(nuevoStock)) {
                throw new IllegalArgumentException(
                        "La cantidad excede el rango admitido para el stock."
                );
            }

            producto.setStock(nuevoStock);
            productoRepository.save(producto);

            DetalleVenta detalle = new DetalleVenta();

            detalle.setVenta(ventaGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            BigDecimal precio;

            try {
                precio = producto.getPrecioVenta()
                        .setScale(2, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException(
                        "El precio del producto debe admitir como máximo 2 decimales."
                );
            }

            if (precio.precision() > 12) {
                throw new IllegalArgumentException(
                        "El precio del producto excede el rango admitido."
                );
            }

            BigDecimal subtotal = cantidad.multiply(precio)
                    .setScale(2, RoundingMode.HALF_UP);

            if (subtotal.precision() > 12) {
                throw new IllegalArgumentException(
                        "El subtotal del detalle excede el rango admitido."
                );
            }

            detalle.setPrecio(precio);
            detalle.setSubtotal(subtotal);

            detalleVentaRepository.save(detalle);
        }

        return ventaGuardada;
    }
}