
package ies.belgrano.almacen.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class DetalleVentaRequest {

    // Identificador del producto que se va a vender
    @NotNull(message = "El producto es obligatorio")
    private Integer idProducto;

    // Cantidad del producto que se va a vender
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(
        value = "0.0",
        inclusive = false,
        message = "La cantidad debe ser mayor que cero"
    )
    private BigDecimal cantidad;

    // Constructor vacío
    public DetalleVentaRequest() {
    }

    // Obtener el ID del producto
    public Integer getIdProducto() {
        return idProducto;
    }

    // Asignar el ID del producto
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    // Obtener la cantidad
    public BigDecimal getCantidad() {
        return cantidad;
    }

    // Asignar la cantidad
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
}
