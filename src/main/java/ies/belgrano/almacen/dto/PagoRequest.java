package ies.belgrano.almacen.dto;

import java.math.BigDecimal;

public class PagoRequest {

    private BigDecimal importe;

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
}