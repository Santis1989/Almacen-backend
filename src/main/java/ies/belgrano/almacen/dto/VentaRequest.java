package ies.belgrano.almacen.dto;

import ies.belgrano.almacen.model.Venta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class VentaRequest {

    @NotNull(message = "Los datos de la venta son obligatorios.")
    private Venta venta;

    @NotEmpty(message = "La venta debe contener al menos un detalle.")
    @Valid
    private List<@NotNull DetalleVentaRequest> detalles;

    public VentaRequest() {
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public List<DetalleVentaRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaRequest> detalles) {
        this.detalles = detalles;
    }
}