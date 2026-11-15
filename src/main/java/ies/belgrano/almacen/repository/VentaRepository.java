
package ies.belgrano.almacen.repository;

import ies.belgrano.almacen.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    // Verificar si un cliente tiene ventas
    boolean existsByCliente_IdCliente(Integer idCliente);

    // Buscar ventas entre dos fechas
    List<Venta> findByFechaGreaterThanEqualAndFechaLessThan(
            LocalDateTime desde,
            LocalDateTime hasta
    );
}
