package ar.edu.ies.almacen.service;

import ar.edu.ies.almacen.dto.ProveedorRequestDTO;
import ar.edu.ies.almacen.dto.ProveedorResponseDTO;
import ar.edu.ies.almacen.entity.Proveedor;
import ar.edu.ies.almacen.exception.ConflictException;
import ar.edu.ies.almacen.exception.ResourceNotFoundException;
import ar.edu.ies.almacen.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listar(Boolean soloActivos, String busqueda) {
        List<Proveedor> proveedores;

        if (busqueda != null && !busqueda.isBlank()) {
            proveedores = proveedorRepository.findByRazonSocialContainingIgnoreCase(busqueda.trim());
            if (Boolean.TRUE.equals(soloActivos)) {
                proveedores = proveedores.stream().filter(Proveedor::getActivo).toList();
            }
        } else if (Boolean.TRUE.equals(soloActivos)) {
            proveedores = proveedorRepository.findByActivo(true);
        } else {
            proveedores = proveedorRepository.findAll();
        }

        return proveedores.stream()
                .map(ProveedorResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProveedorResponseDTO buscarPorId(Integer id) {
        Proveedor proveedor = obtenerEntidadPorId(id);
        return ProveedorResponseDTO.fromEntity(proveedor);
    }

    @Transactional(readOnly = true)
    public ProveedorResponseDTO buscarPorCuit(String cuit) {
        Proveedor proveedor = proveedorRepository.findByCuit(cuit.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún proveedor con el CUIT: " + cuit));
        return ProveedorResponseDTO.fromEntity(proveedor);
    }

    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO request) {
        String cuitLimpio = request.getCuit().trim();

        if (proveedorRepository.existsByCuit(cuitLimpio)) {
            throw new ConflictException("Ya existe un proveedor registrado con el CUIT: " + cuitLimpio);
        }

        Proveedor nuevoProveedor = new Proveedor();
        nuevoProveedor.setCuit(cuitLimpio);
        nuevoProveedor.setRazonSocial(request.getRazonSocial().trim());
        nuevoProveedor.setContacto(request.getContacto() != null ? request.getContacto().trim() : null);
        nuevoProveedor.setTelefono(request.getTelefono().trim());
        nuevoProveedor.setEmail(request.getEmail().trim());
        nuevoProveedor.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);
        nuevoProveedor.setActivo(request.getActivo() != null ? request.getActivo() : true);

        Proveedor guardado = proveedorRepository.save(nuevoProveedor);
        return ProveedorResponseDTO.fromEntity(guardado);
    }

    @Transactional
    public ProveedorResponseDTO actualizar(Integer id, ProveedorRequestDTO request) {
        Proveedor proveedor = obtenerEntidadPorId(id);
        String cuitLimpio = request.getCuit().trim();

        if (proveedorRepository.existsByCuitAndIdProveedorNot(cuitLimpio, id)) {
            throw new ConflictException("El CUIT " + cuitLimpio + " ya está asignado a otro proveedor registrado.");
        }

        proveedor.setCuit(cuitLimpio);
        proveedor.setRazonSocial(request.getRazonSocial().trim());
        proveedor.setContacto(request.getContacto() != null ? request.getContacto().trim() : null);
        proveedor.setTelefono(request.getTelefono().trim());
        proveedor.setEmail(request.getEmail().trim());
        proveedor.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);
        if (request.getActivo() != null) {
            proveedor.setActivo(request.getActivo());
        }

        Proveedor actualizado = proveedorRepository.save(proveedor);
        return ProveedorResponseDTO.fromEntity(actualizado);
    }

    @Transactional
    public ProveedorResponseDTO cambiarEstado(Integer id, boolean activo) {
        Proveedor proveedor = obtenerEntidadPorId(id);
        proveedor.setActivo(activo);
        Proveedor guardado = proveedorRepository.save(proveedor);
        return ProveedorResponseDTO.fromEntity(guardado);
    }

    @Transactional
    public void eliminarFisico(Integer id) {
        if (!proveedorRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar. Proveedor no encontrado con ID: " + id);
        }
        proveedorRepository.deleteById(id);
    }

    private Proveedor obtenerEntidadPorId(Integer id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));
    }
}
