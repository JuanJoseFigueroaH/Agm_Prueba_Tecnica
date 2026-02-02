package com.agm.clientes.domain.exception;

import java.util.UUID;

public class ClienteNotFoundException extends RuntimeException {
    
    public ClienteNotFoundException(UUID id) {
        super(String.format("Cliente con ID %s no encontrado", id));
    }
}
