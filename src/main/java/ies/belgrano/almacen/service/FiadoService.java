
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.model.Fiado;
import ies.belgrano.almacen.repository.FiadoRepository;
import ies.belgrano.almacen.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class FiadoService {

    private final FiadoRepository fiadoRepository;
    private final ClienteRepository clienteRepository;

    // Constructor
    public FiadoService(
            FiadoRepository fiadoRepository,
            ClienteRepository clienteRepository) {

        this.fiadoRepository = fiadoRepository;
        this.clienteRepository = clienteRepository;
    }

    // Listar todos los fiados
    public List<Fiado> listarTodos() {
        return fiadoRepository.findAll();
    }

    // Buscar un fiado por ID
    public Optional<Fiado> buscarPorId(Integer id) {
        return fiadoRepository.findById(id);
    }

    // Crear un nuevo fiado
    public Fiado guardar(Fiado fiado) {

        // Validar los importes
        validarImportes(fiado);

        // Validar que el cliente exista
        validarCliente(fiado);

        // Calcular el estado del fiado
        actualizarEstado(fiado);

        // Guardar en la base de datos
        return fiadoRepository.save(fiado);
    }

    // Modificar un fiado existente
    public Fiado actualizar(Integer id, Fiado fiado) {

        // Validar los importes
        validarImportes(fiado);

        // Validar que el cliente exista
        validarCliente(fiado);

        // Buscar el fiado existente
        Fiado existente = fiadoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Fiado no encontrado"));

        // Actualizar los datos
        existente.setIdCliente(fiado.getIdCliente());

        // Conservar la fecha si no se proporciona una nueva
        if (fiado.getFecha() != null) {
            existente.setFecha(fiado.getFecha());
        }

        existente.setMontoTotal(fiado.getMontoTotal());
        existente.setSaldo(fiado.getSaldo());

        // Recalcular el estado
        actualizarEstado(existente);

        // Guardar los cambios
        return fiadoRepository.save(existente);
    }

    // Validar los importes del fiado
    private void validarImportes(Fiado fiado) {

        if (fiado == null) {
            throw new IllegalArgumentException(
                    "Los datos del fiado son obligatorios"
            );
        }

        if (fiado.getMontoTotal() == null
                || fiado.getMontoTotal().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El monto total debe ser mayor a cero"
            );
        }

        if (fiado.getSaldo() == null
                || fiado.getSaldo().compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El saldo no puede ser negativo"
            );
        }

        if (fiado.getSaldo().compareTo(fiado.getMontoTotal()) > 0) {

            throw new IllegalArgumentException(
                    "El saldo no puede superar el monto total"
            );
        }
    }

    // Validar que el cliente exista
    private void validarCliente(Fiado fiado) {

        if (fiado.getIdCliente() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un cliente"
            );
        }

        if (!clienteRepository.existsById(fiado.getIdCliente())) {
            throw new IllegalArgumentException(
                    "El cliente indicado no existe"
            );
        }
    }

    // Eliminar un fiado
    public void eliminar(Integer id) {
        fiadoRepository.deleteById(id);
    }

    // Calcular el estado según el saldo
    private void actualizarEstado(Fiado fiado) {

        if (fiado.getSaldo().compareTo(BigDecimal.ZERO) == 0) {

            fiado.setEstado("CANCELADO");

        } else if (fiado.getSaldo().compareTo(fiado.getMontoTotal()) < 0) {

            fiado.setEstado("PARCIAL");

        } else {

            fiado.setEstado("PENDIENTE");
        }
    }
}
