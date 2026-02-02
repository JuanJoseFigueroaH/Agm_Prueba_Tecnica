package com.agm.clientes.domain.exception;

import java.util.UUID;

public class ClienteDeletedException extends RuntimeException {
    
    public ClienteDeletedException(UUID id) {
        super(String.format("No se puede operar sobre el cliente %s porque está eliminado", id));
    }
}
