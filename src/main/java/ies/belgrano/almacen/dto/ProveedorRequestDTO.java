package ies.belgrano.almacen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProveedorRequestDTO {

    @NotBlank(message = "El CUIT es obligatorio.")
    @Pattern(regexp = "^\\d{2}-?\\d{8}-?\\d{1}$", message = "El formato de CUIT no es válido (ej: 20-12345678-9 o 20123456789).")
    private String cuit;

    @NotBlank(message = "La razón social es obligatoria.")
    @Size(max = 150, message = "La razón social no puede superar los 150 caracteres.")
    private String razonSocial;

    @Size(max = 100, message = "El contacto no puede superar los 100 caracteres.")
    private String contacto;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 50, message = "El teléfono no puede superar los 50 caracteres.")
    private String telefono;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres.")
    private String email;

    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres.")
    private String direccion;

    private Boolean activo;

    public ProveedorRequestDTO() {
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
