package com.agm.clientes.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientePatchRequest {

    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String nombre;

    @Email(message = "El email debe tener un formato válido")
    private String email;

    @Pattern(regexp = "^$|^\\d{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos")
    private String telefono;

    private Boolean activo;

    @NotNull(message = "La versión es requerida para control de concurrencia")
    private Long version;
}
