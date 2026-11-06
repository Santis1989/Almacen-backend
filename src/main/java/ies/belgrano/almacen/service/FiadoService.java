
package ies.belgrano.almacen.service;

import ies.belgrano.almacen.model.Cliente;
import ies.belgrano.almacen.model.Fiado;
import ies.belgrano.almacen.repository.ClienteRepository;
import ies.belgrano.almacen.repository.FiadoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class FiadoService {

    private final FiadoRepository fiadoRepository;
    private final ClienteRepository clienteRepository;

    public FiadoService(
            FiadoRepository fiadoRepository,
            ClienteRepository clienteRepository) {

        this.fiadoRepository = fiadoRepository;
        this.clienteRepository = clienteRepository;
    }

    // Listar todos los fiados
    @Transactional(readOnly = true)
    public List<Fiado> listarTodos() {
        return fiadoRepository.findAll();
    }

    // Buscar fiado por ID
    @Transactional(readOnly = true)
    public Optional<Fiado> buscarPorId(Integer id) {
        return fiadoRepository.findById(id);
    }

    // Crear un nuevo fiado
    @Transactional
    public Fiado guardar(Fiado fiado) {

        validarImportes(fiado);
        validarCliente(fiado);
        actualizarEstado(fiado);

        return fiadoRepository.save(fiado);
    }

    // Actualizar un fiado existente
    @Transactional
    public Fiado actualizar(Integer id, Fiado fiado) {

        validarImportes(fiado);
        validarCliente(fiado);

        Fiado existente = fiadoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Fiado no encontrado"));

        // Asignar el cliente validado
        existente.setCliente(fiado.getCliente());

        // Actualizar fecha si corresponde
        if (fiado.getFecha() != null) {
            existente.setFecha(fiado.getFecha());
        }

        existente.setMontoTotal(fiado.getMontoTotal());
        existente.setSaldo(fiado.getSaldo());

        actualizarEstado(existente);

        return fiadoRepository.save(existente);
    }

    // Validar los importes
    private void validarImportes(Fiado fiado) {

        if (fiado == null) {
            throw new IllegalArgumentException(
                    "Los datos del fiado son obligatorios");
        }

        if (fiado.getMontoTotal() == null ||
                fiado.getMontoTotal().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El monto total debe ser mayor a cero");
        }

        if (fiado.getSaldo() == null ||
                fiado.getSaldo().compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El saldo no puede ser negativo");
        }

        if (fiado.getSaldo().compareTo(fiado.getMontoTotal()) > 0) {

            throw new IllegalArgumentException(
                    "El saldo no puede superar el monto total");
        }
    }

    // Validar que el cliente exista
    private void validarCliente(Fiado fiado) {

        if (fiado.getCliente() == null ||
                fiado.getCliente().getIdCliente() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un cliente");
        }

        Integer idCliente = fiado.getCliente().getIdCliente();

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El cliente indicado no existe"));

        fiado.setCliente(cliente);
    }

    // Eliminar un fiado
    @Transactional
    public void eliminar(Integer id) {
        fiadoRepository.deleteById(id);
    }

    // Calcular el estado del fiado
    private void actualizarEstado(Fiado fiado) {

        if (fiado.getSaldo().compareTo(BigDecimal.ZERO) == 0) {

            fiado.setEstado("CANCELADO");

        } else if (fiado.getSaldo().compareTo(
                fiado.getMontoTotal()) < 0) {

            fiado.setEstado("PARCIAL");

        } else {

            fiado.setEstado("PENDIENTE");
        }
    }
}
