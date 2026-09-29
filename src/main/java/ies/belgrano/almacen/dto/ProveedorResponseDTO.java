package ies.belgrano.almacen.dto;

import ies.belgrano.almacen.model.Proveedor;

public class ProveedorResponseDTO {

    private Integer idProveedor;
    private String cuit;
    private String razonSocial;
    private String contacto;
    private String telefono;
    private String email;
    private String direccion;
    private Boolean activo;

    public ProveedorResponseDTO() {
    }

    public ProveedorResponseDTO(Integer idProveedor, String cuit, String razonSocial, String contacto,
                                String telefono, String email, String direccion, Boolean activo) {
        this.idProveedor = idProveedor;
        this.cuit = cuit;
        this.razonSocial = razonSocial;
        this.contacto = contacto;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.activo = activo;
    }

    public static ProveedorResponseDTO fromEntity(Proveedor entity) {
        if (entity == null) {
            return null;
        }
        return new ProveedorResponseDTO(
                entity.getIdProveedor(),
                entity.getCuit(),
                entity.getRazonSocial(),
                entity.getContacto(),
                entity.getTelefono(),
                entity.getEmail(),
                entity.getDireccion(),
                entity.getActivo()
        );
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
