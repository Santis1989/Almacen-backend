
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.ReporteVentasResponse;
import ies.belgrano.almacen.model.Venta;
import ies.belgrano.almacen.repository.VentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReporteService {

    private final VentaRepository ventaRepository;

    public ReporteService(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Transactional(readOnly = true)
    public ReporteVentasResponse reporteVentas(
            LocalDate desde,
            LocalDate hasta) {

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha desde no puede ser posterior a la fecha hasta."
            );
        }

        List<Venta> ventas =
                ventaRepository.findByFechaGreaterThanEqualAndFechaLessThan(
                        desde.atStartOfDay(),
                        hasta.plusDays(1).atStartOfDay()
                );

        long cantidadVentas = ventas.size();

        BigDecimal totalVendido = ventas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedioVenta = cantidadVentas == 0
                ? BigDecimal.ZERO.setScale(2)
                : totalVendido.divide(
                BigDecimal.valueOf(cantidadVentas),
                2,
                RoundingMode.HALF_UP
        );

        return new ReporteVentasResponse(
                cantidadVentas,
                totalVendido,
                promedioVenta
        );
    }
}
