package com.agm.clientes.application.usecase;

import com.agm.clientes.application.dto.response.ClienteResponse;
import com.agm.clientes.application.mapper.ClienteMapper;
import com.agm.clientes.domain.exception.ClienteDeletedException;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetClienteByIdUseCaseTest {

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @Mock
    private CachePort cachePort;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private GetClienteByIdUseCase getClienteByIdUseCase;

    private UUID clienteId;
    private Cliente cliente;
    private ClienteResponse response;

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

        response = ClienteResponse.builder()
                .id(clienteId)
                .nombre("Juan Perez")
                .email("juan@example.com")
                .telefono("1234567890")
                .activo(true)
                .version(0L)
                .build();
    }

    @Test
    void execute_WhenClienteExistsInCache_ShouldReturnFromCache() {
        when(cachePort.get(anyString(), eq(Cliente.class))).thenReturn(Mono.just(cliente));
        when(clienteMapper.toResponse(any(Cliente.class))).thenReturn(response);

        StepVerifier.create(getClienteByIdUseCase.execute(clienteId))
                .expectNext(response)
                .verifyComplete();

        verify(cachePort).get("cliente:" + clienteId, Cliente.class);
        verify(clienteRepository, never()).findById(any());
    }

    @Test
    void execute_WhenClienteNotInCache_ShouldReturnFromDatabase() {
        when(cachePort.get(anyString(), eq(Cliente.class))).thenReturn(Mono.empty());
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Mono.just(cliente));
        when(cachePort.set(anyString(), any(), any())).thenReturn(Mono.just(true));
        when(clienteMapper.toResponse(any(Cliente.class))).thenReturn(response);

        StepVerifier.create(getClienteByIdUseCase.execute(clienteId))
                .expectNext(response)
                .verifyComplete();

        verify(clienteRepository).findById(clienteId);
        verify(cachePort).set(anyString(), any(), any());
    }

    @Test
    void execute_WhenClienteNotFound_ShouldThrowNotFoundException() {
        when(cachePort.get(anyString(), eq(Cliente.class))).thenReturn(Mono.empty());
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(getClienteByIdUseCase.execute(clienteId))
                .expectError(ClienteNotFoundException.class)
                .verify();
    }

    @Test
    void execute_WhenClienteIsDeleted_ShouldThrowClienteDeletedException() {
        cliente.setDeletedAt(LocalDateTime.now());
        
        when(cachePort.get(anyString(), eq(Cliente.class))).thenReturn(Mono.just(cliente));

        StepVerifier.create(getClienteByIdUseCase.execute(clienteId))
                .expectError(ClienteDeletedException.class)
                .verify();
    }
}
