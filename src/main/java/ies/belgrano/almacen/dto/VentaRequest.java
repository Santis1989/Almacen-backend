
package ies.belgrano.almacen.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

public class VentaRequest {

    // Cliente asociado a la venta (opcional)
    private Integer idCliente;

    // Porcentaje de descuento (opcional, entre 0 y 100)
    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El descuento no puede superar el 100%")
    private BigDecimal porcentajeDescuento;

    // Productos y cantidades de la venta
    @NotEmpty(message = "La venta debe tener al menos un detalle")
    @Valid
    private List<DetalleVentaRequest> detalles;

    // Constructor vacío
    public VentaRequest() {
    }

    // Obtener el ID del cliente
    public Integer getIdCliente() {
        return idCliente;
    }

    // Asignar el ID del cliente
    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    // Obtener el porcentaje de descuento
    public BigDecimal getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    // Asignar el porcentaje de descuento
    public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    // Obtener los detalles de la venta
    public List<DetalleVentaRequest> getDetalles() {
        return detalles;
    }

    // Asignar los detalles de la venta
    public void setDetalles(List<DetalleVentaRequest> detalles) {
        this.detalles = detalles;
    }
}
