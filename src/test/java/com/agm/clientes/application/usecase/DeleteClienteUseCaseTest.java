package com.agm.clientes.application.usecase;

import com.agm.clientes.domain.exception.ClienteNotFoundException;
import com.agm.clientes.domain.model.Cliente;
import com.agm.clientes.domain.port.out.CachePort;
import com.agm.clientes.domain.port.out.ClienteRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteClienteUseCaseTest {

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private DeleteClienteUseCase deleteClienteUseCase;

    private UUID clienteId;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        
        cliente = Cliente.builder()
                .id(clienteId)
                .nombre("Juan Perez")
                .email("juan@example.com")
                .telefono("1234567890")
                .activo(true)
                .version(0L)
                .build();
    }

    @Test
    void execute_WhenClienteExists_ShouldDeleteLogically() {
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Mono.just(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(Mono.just(cliente));
        when(cachePort.delete(anyString())).thenReturn(Mono.just(true));
        when(cachePort.deleteByPattern(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(deleteClienteUseCase.execute(clienteId))
                .verifyComplete();

        verify(clienteRepository).findById(clienteId);
        verify(clienteRepository).save(any(Cliente.class));
        verify(cachePort).delete("cliente:" + clienteId);
        verify(cachePort).deleteByPattern("clientes:list:*");
    }

    @Test
    void execute_WhenClienteNotFound_ShouldThrowNotFoundException() {
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(deleteClienteUseCase.execute(clienteId))
                .expectError(ClienteNotFoundException.class)
                .verify();

        verify(clienteRepository).findById(clienteId);
        verify(clienteRepository, never()).save(any());
    }
}
