package com.agm.clientes.domain.exception;

public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String email) {
        super(String.format("El email %s ya está registrado", email));
    }
}
