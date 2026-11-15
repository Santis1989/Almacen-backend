
package ies.belgrano.almacen.dto;

import java.math.BigDecimal;

public record ReporteVentasResponse(
        long cantidadVentas,
        BigDecimal totalVendido,
        BigDecimal promedioVenta
) {
}
