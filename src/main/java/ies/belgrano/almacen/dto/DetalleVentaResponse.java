package ies.belgrano.almacen.dto;

import java.math.BigDecimal;

public class DetalleVentaResponse {

    private final Integer idDetalle;
    private final Integer idProducto;
    private final String nombreProducto;
    private final BigDecimal cantidad;
    private final BigDecimal precio;
    private final BigDecimal subtotal;

    public DetalleVentaResponse(
            Integer idDetalle,
            Integer idProducto,
            String nombreProducto,
            BigDecimal cantidad,
            BigDecimal precio,
            BigDecimal subtotal) {

        this.idDetalle = idDetalle;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precio = precio;
        this.subtotal = subtotal;
    }

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}