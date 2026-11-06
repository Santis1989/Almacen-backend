package ies.belgrano.almacen.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;

public class DetalleVentaRequest {

    @NotNull
    @Positive
    private Integer idProducto;

    @NotNull
    @Positive
    @Digits(
            integer = 9,
            fraction = 3,
            message = "La cantidad admite hasta 9 dígitos enteros y 3 decimales."
    )
    private BigDecimal cantidad;
    public DetalleVentaRequest() {
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
}