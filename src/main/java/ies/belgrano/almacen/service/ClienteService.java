package ies.belgrano.almacen.service;

import ies.belgrano.almacen.dto.ClienteRequest;
import ies.belgrano.almacen.dto.ClienteResponse;
import ies.belgrano.almacen.model.Cliente;
import ies.belgrano.almacen.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
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

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado"));

        return convertirAResponse(cliente);
    }

    // BUSCAR CLIENTE POR DNI
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorDni(String dni) {

        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado"));

        return convertirAResponse(cliente);
    }

    // CREAR CLIENTE
    @Transactional
    public ClienteResponse guardar(ClienteRequest request) {

        Cliente cliente = new Cliente();

        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setDni(request.getDni());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());

        Cliente guardado = clienteRepository.save(cliente);

        return convertirAResponse(guardado);
    }

    // ACTUALIZAR CLIENTE
    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {

        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado"));

        existente.setNombre(request.getNombre());
        existente.setApellido(request.getApellido());
        existente.setDni(request.getDni());
        existente.setTelefono(request.getTelefono());
        existente.setDireccion(request.getDireccion());

        Cliente actualizado = clienteRepository.save(existente);

        return convertirAResponse(actualizado);
    }

    // ELIMINAR CLIENTE
    @Transactional
    public void eliminar(Integer id) {

        if (!clienteRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cliente no encontrado");
        }

        clienteRepository.deleteById(id);
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