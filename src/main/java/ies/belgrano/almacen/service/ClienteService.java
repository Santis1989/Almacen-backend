package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.ClienteRequest;
import ies.belgrano.almacen.dto.ClienteResponse;
import ies.belgrano.almacen.exception.ConflictException;
import ies.belgrano.almacen.exception.ResourceNotFoundException;
import ies.belgrano.almacen.model.Cliente;
import ies.belgrano.almacen.repository.ClienteRepository;
import ies.belgrano.almacen.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;

    public ClienteService(ClienteRepository clienteRepository, VentaRepository ventaRepository) {
        this.clienteRepository = clienteRepository;
        this.ventaRepository = ventaRepository;
    }

    // LISTAR TODOS LOS CLIENTES
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // BUSCAR CLIENTE POR ID
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Integer id) {
        Cliente cliente = obtenerEntidadPorId(id);
        return convertirAResponse(cliente);
    }

    // BUSCAR CLIENTE POR DNI
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorDni(String dni) {
        String dniLimpio = dni != null ? dni.trim() : "";
        Cliente cliente = clienteRepository.findByDni(dniLimpio)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con DNI: " + dniLimpio));

        return convertirAResponse(cliente);
    }

    // CREAR CLIENTE CON VALIDACIÓN DE INTEGRIDAD DE DNI
    @Transactional
    public ClienteResponse guardar(ClienteRequest request) {
        String dniLimpio = request.getDni().trim();

        if (clienteRepository.existsByDni(dniLimpio)) {
            throw new ConflictException("Ya existe un cliente registrado con el DNI: " + dniLimpio);
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre().trim());
        cliente.setApellido(request.getApellido().trim());
        cliente.setDni(dniLimpio);
        cliente.setTelefono(request.getTelefono().trim());
        cliente.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);

        Cliente guardado = clienteRepository.save(cliente);
        return convertirAResponse(guardado);
    }

    // ACTUALIZAR CLIENTE CON VALIDACIÓN DE INTEGRIDAD DE DNI
    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente existente = obtenerEntidadPorId(id);
        String dniLimpio = request.getDni().trim();

        if (clienteRepository.existsByDniAndIdClienteNot(dniLimpio, id)) {
            throw new ConflictException("El DNI " + dniLimpio + " ya está asignado a otro cliente registrado.");
        }

        existente.setNombre(request.getNombre().trim());
        existente.setApellido(request.getApellido().trim());
        existente.setDni(dniLimpio);
        existente.setTelefono(request.getTelefono().trim());
        existente.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);

        Cliente actualizado = clienteRepository.save(existente);
        return convertirAResponse(actualizado);
    }

    // ELIMINAR CLIENTE CON VALIDACIÓN DE INTEGRIDAD REFERENCIAL
    @Transactional
    public void eliminar(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar. Cliente no encontrado con ID: " + id);
        }

        if (ventaRepository.existsByCliente_IdCliente(id)) {
            throw new ConflictException("No es posible eliminar el cliente con ID " + id +
                    " porque registra ventas asociadas. Para preservar la integridad histórica comercial, no debe ser eliminado.");
        }

        clienteRepository.deleteById(id);
    }

    // MÉTODO AUXILIAR PARA OBTENER ENTIDAD O LANZAR 404
    private Cliente obtenerEntidadPorId(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
    }

    // CONVERTIR CLIENTE A CLIENTE RESPONSE
    private ClienteResponse convertirAResponse(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setIdCliente(cliente.getIdCliente());
        response.setNombre(cliente.getNombre());
        response.setApellido(cliente.getApellido());
        response.setDni(cliente.getDni());
        response.setTelefono(cliente.getTelefono());
        response.setDireccion(cliente.getDireccion());
        return response;
    }
}