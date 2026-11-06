
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.model.Fiado;
import ies.belgrano.almacen.model.Pago;
import ies.belgrano.almacen.repository.FiadoRepository;
import ies.belgrano.almacen.repository.PagoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final FiadoRepository fiadoRepository;

    public PagoService(PagoRepository pagoRepository,
                       FiadoRepository fiadoRepository) {
        this.pagoRepository = pagoRepository;
        this.fiadoRepository = fiadoRepository;
    }

    // Registrar un pago de un fiado
    @Transactional
    public Pago registrarPago(Integer idFiado, BigDecimal importe) {

        // 1. Buscar el fiado
        Fiado fiado = fiadoRepository.findById(idFiado)
                .orElseThrow(() ->
                        new IllegalArgumentException("El fiado no existe"));

        // 2. Validar el importe
        if (importe == null ||
                importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El importe del pago debe ser mayor a cero");
        }

        // 3. Verificar que no supere la deuda
        if (importe.compareTo(fiado.getSaldo()) > 0) {
            throw new IllegalArgumentException(
                    "El pago no puede superar el saldo pendiente");
        }

        // 4. Descontar el pago del saldo
        BigDecimal nuevoSaldo = fiado.getSaldo().subtract(importe);
        fiado.setSaldo(nuevoSaldo);

        // 5. Actualizar el estado
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            fiado.setEstado("CANCELADO");
        } else {
            fiado.setEstado("PARCIAL");
        }

        // 6. Guardar el fiado actualizado
        fiadoRepository.save(fiado);

        // 7. Registrar el pago relacionado con el fiado
        Pago pago = new Pago();
        pago.setFiado(fiado);
        pago.setImporte(importe);

        return pagoRepository.save(pago);
    }

    // Obtener el historial de pagos de un fiado
    @Transactional(readOnly = true)
    public List<Pago> obtenerPagosPorFiado(Integer idFiado) {
        return pagoRepository.findByFiado_IdFiadoOrderByFechaAsc(idFiado);
    }
}
