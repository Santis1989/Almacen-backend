
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.DetalleVentaRequest;
import ies.belgrano.almacen.dto.DetalleVentaResponse;
import ies.belgrano.almacen.dto.VentaRequest;
import ies.belgrano.almacen.dto.VentaResponse;

import ies.belgrano.almacen.exception.ConflictException;
import ies.belgrano.almacen.exception.ResourceNotFoundException;

import ies.belgrano.almacen.model.Cliente;
import ies.belgrano.almacen.model.DetalleVenta;
import ies.belgrano.almacen.model.Producto;
import ies.belgrano.almacen.model.Venta;

import ies.belgrano.almacen.repository.ClienteRepository;
import ies.belgrano.almacen.repository.DetalleVentaRepository;
import ies.belgrano.almacen.repository.ProductoRepository;
import ies.belgrano.almacen.repository.VentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        @Transactional(readOnly = true)
        public List<VentaResponse> listarTodas() {

                List<VentaResponse> respuestas = new ArrayList<>();

                for (Venta venta : ventaRepository.findAll()) {
                        respuestas.add(convertirAResponse(venta));
                }

                return respuestas;
        }

        @Transactional(readOnly = true)
        public Optional<VentaResponse> buscarPorId(Integer idVenta) {

                return ventaRepository.findById(idVenta)
                                .map(this::convertirAResponse);
        }

        @Transactional(readOnly = true)
        public List<DetalleVentaResponse> listarDetalles(Integer idVenta) {

                if (!ventaRepository.existsById(idVenta)) {
                        throw new ResourceNotFoundException(
                                        "Venta no encontrada: " + idVenta);
                }

                List<DetalleVenta> detalles = detalleVentaRepository.findByVenta_IdVenta(idVenta);

                List<DetalleVentaResponse> respuestas = new ArrayList<>();

                for (DetalleVenta detalle : detalles) {
                        respuestas.add(convertirDetalleAResponse(detalle));
                }

                return respuestas;
        }

        @Transactional
        public VentaResponse registrarVenta(VentaRequest request) {

                if (request == null
                                || request.getDetalles() == null
                                || request.getDetalles().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "La venta debe tener al menos un detalle");
                }

                Venta venta = new Venta();

                // Cliente opcional
                if (request.getIdCliente() != null) {

                        Cliente cliente = clienteRepository
                                        .findById(request.getIdCliente())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Cliente no encontrado: "
                                                                        + request.getIdCliente()));

                        venta.setCliente(cliente);
                }

                List<DetalleVenta> detalles = new ArrayList<>();

                BigDecimal subtotalVenta = BigDecimal.ZERO;

                for (DetalleVentaRequest detalleRequest : request.getDetalles()) {

                        // Validar los datos del detalle
                        if (detalleRequest == null
                                        || detalleRequest.getIdProducto() == null
                                        || detalleRequest.getCantidad() == null
                                        || detalleRequest.getCantidad()
                                                        .compareTo(BigDecimal.ZERO) <= 0) {

                                throw new IllegalArgumentException(
                                                "El detalle debe tener un producto y una cantidad mayor a cero");
                        }

                        Integer idProducto = detalleRequest.getIdProducto();

                        // Buscar producto
                        Producto producto = productoRepository
                                        .findById(idProducto)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Producto no encontrado: " + idProducto));

                        BigDecimal cantidad = detalleRequest.getCantidad();

                        // Validar stock
                        if (producto.getStock() == null) {

                                throw new IllegalArgumentException(
                                                "El producto no tiene stock definido: "
                                                                + producto.getNombre());
                        }

                        BigDecimal stockDisponible = BigDecimal.valueOf(producto.getStock());

                        // CAMBIO: devolver 409 Conflict
                        if (stockDisponible.compareTo(cantidad) < 0) {

                                throw new ConflictException(
                                                "Stock insuficiente para el producto: "
                                                                + producto.getNombre());
                        }

                        // Obtener precio desde la base de datos
                        BigDecimal precio = producto.getPrecioVenta();

                        if (precio == null
                                        || precio.compareTo(BigDecimal.ZERO) < 0) {

                                throw new IllegalArgumentException(
                                                "Precio de venta inválido para el producto: "
                                                                + producto.getNombre());
                        }

                        // Calcular subtotal del detalle
                        BigDecimal subtotalDetalle = cantidad
                                        .multiply(precio)
                                        .setScale(2, RoundingMode.HALF_UP);

                        DetalleVenta detalle = new DetalleVenta();

                        detalle.setProducto(producto);
                        detalle.setCantidad(cantidad);
                        detalle.setPrecio(precio);
                        detalle.setSubtotal(subtotalDetalle);

                        detalles.add(detalle);

                        subtotalVenta = subtotalVenta.add(subtotalDetalle);

                        // Actualizar stock
                        BigDecimal nuevoStock = stockDisponible.subtract(cantidad);

                        producto.setStock(nuevoStock.doubleValue());

                        productoRepository.save(producto);
                }

                // Validar porcentaje de descuento
                BigDecimal porcentajeDescuento = request.getPorcentajeDescuento();

                if (porcentajeDescuento == null) {
                        porcentajeDescuento = BigDecimal.ZERO;
                }

                if (porcentajeDescuento.compareTo(BigDecimal.ZERO) < 0
                                || porcentajeDescuento.compareTo(
                                                new BigDecimal("100")) > 0) {

                        throw new IllegalArgumentException(
                                        "El porcentaje de descuento debe estar entre 0 y 100");
                }

                // Calcular descuento
                BigDecimal montoDescuento = subtotalVenta
                                .multiply(porcentajeDescuento)
                                .divide(
                                                new BigDecimal("100"),
                                                2,
                                                RoundingMode.HALF_UP);

                // Calcular total
                BigDecimal total = subtotalVenta
                                .subtract(montoDescuento)
                                .setScale(2, RoundingMode.HALF_UP);

                venta.setSubtotal(
                                subtotalVenta.setScale(2, RoundingMode.HALF_UP));

                venta.setPorcentajeDescuento(
                                porcentajeDescuento.setScale(2, RoundingMode.HALF_UP));

                venta.setMontoDescuento(montoDescuento);
                venta.setTotal(total);

                // Guardar venta
                Venta ventaGuardada = ventaRepository.save(venta);

                // Guardar detalles
                for (DetalleVenta detalle : detalles) {

                        detalle.setVenta(ventaGuardada);

                        detalleVentaRepository.save(detalle);
                }

                return convertirAResponse(ventaGuardada);
        }

        private VentaResponse convertirAResponse(Venta venta) {

                VentaResponse respuesta = new VentaResponse();

                respuesta.setIdVenta(venta.getIdVenta());
                respuesta.setFecha(venta.getFecha());

                if (venta.getCliente() != null) {
                        respuesta.setIdCliente(
                                        venta.getCliente().getIdCliente());
                }

                respuesta.setSubtotal(venta.getSubtotal());

                respuesta.setPorcentajeDescuento(
                                venta.getPorcentajeDescuento());

                respuesta.setMontoDescuento(
                                venta.getMontoDescuento());

                respuesta.setTotal(venta.getTotal());

                List<DetalleVentaResponse> detalles = new ArrayList<>();

                List<DetalleVenta> detallesGuardados = detalleVentaRepository.findByVenta_IdVenta(
                                venta.getIdVenta());

                for (DetalleVenta detalle : detallesGuardados) {
                        detalles.add(convertirDetalleAResponse(detalle));
                }

                respuesta.setDetalles(detalles);

                return respuesta;
        }

        private DetalleVentaResponse convertirDetalleAResponse(
                        DetalleVenta detalle) {

                DetalleVentaResponse respuesta = new DetalleVentaResponse();

                respuesta.setIdDetalle(detalle.getIdDetalle());

                respuesta.setIdProducto(
                                detalle.getProducto().getIdProducto());

                respuesta.setNombreProducto(
                                detalle.getProducto().getNombre());

                respuesta.setCantidad(detalle.getCantidad());
                respuesta.setPrecio(detalle.getPrecio());
                respuesta.setSubtotal(detalle.getSubtotal());

                return respuesta;
        }
}
