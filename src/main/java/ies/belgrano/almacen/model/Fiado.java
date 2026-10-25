package ies.belgrano.almacen.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiados")
public class Fiado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fiado")
    private Integer idFiado;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Column(name = "saldo", nullable = false)
    private BigDecimal saldo;

    @Column(name = "estado")
    private String estado;

    public Fiado() {
    }

    public Integer getIdFiado() {
        return idFiado;
    }

    public void setIdFiado(Integer idFiado) {
        this.idFiado = idFiado;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }@PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}