
package ies.belgrano.almacen.controller;

import ies.belgrano.almacen.dto.PagoRequest;
import ies.belgrano.almacen.model.Pago;
import ies.belgrano.almacen.service.PagoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fiados")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // POST - Registrar un pago de un fiado
    @PostMapping("/{idFiado}/pagos")
    public ResponseEntity<Pago> registrarPago(
            @PathVariable Integer idFiado,
            @RequestBody PagoRequest request) {

        Pago pago = pagoService.registrarPago(
                idFiado,
                request.getImporte()
        );

        return ResponseEntity.status(201).body(pago);
    }

    // GET - Consultar historial de pagos
    @GetMapping("/{idFiado}/pagos")
    public ResponseEntity<List<Pago>> obtenerPagos(
            @PathVariable Integer idFiado) {

        return ResponseEntity.ok(
                pagoService.obtenerPagosPorFiado(idFiado)
        );
    }
}
