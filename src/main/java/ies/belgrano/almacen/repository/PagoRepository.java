package ies.belgrano.almacen.repository;

import ies.belgrano.almacen.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByIdFiadoOrderByFechaAsc(Integer idFiado);

}