package ies.belgrano.almacen.dto;

import ies.belgrano.almacen.model.DetalleVenta;
import ies.belgrano.almacen.model.Venta;

import java.util.List;

public class VentaRequest {

    private Venta venta;
    private List<DetalleVenta> detalles;

    public VentaRequest() {
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }
}