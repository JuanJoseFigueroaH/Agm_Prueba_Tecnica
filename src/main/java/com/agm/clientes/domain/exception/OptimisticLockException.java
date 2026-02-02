package com.agm.clientes.domain.exception;

public class OptimisticLockException extends RuntimeException {
    
    public OptimisticLockException() {
        super("El recurso ha sido modificado por otro proceso. Por favor, recarga y vuelve a intentar");
    }
}
