
package ies.belgrano.almacen.dto;

import java.math.BigDecimal;

public class ProductoResponseDTO {

    private Integer idProducto;
    private String nombre;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Double stock;
    private Integer stockMinimo;

    public ProductoResponseDTO() {
    }

    public ProductoResponseDTO(
            Integer idProducto,
            String nombre,
            String descripcion,
            BigDecimal precioCompra,
            BigDecimal precioVenta,
            Double stock,
            Integer stockMinimo) {

        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Double getStock() {
        return stock;
    }

    public void setStock(Double stock) {
        this.stock = stock;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}
