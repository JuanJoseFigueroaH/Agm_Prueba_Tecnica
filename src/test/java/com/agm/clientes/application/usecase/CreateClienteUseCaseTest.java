package com.agm.clientes.application.usecase;

import com.agm.clientes.application.dto.request.ClienteCreateRequest;
import com.agm.clientes.application.dto.response.ClienteResponse;
import com.agm.clientes.application.mapper.ClienteMapper;
import com.agm.clientes.domain.exception.DuplicateEmailException;
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

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateClienteUseCaseTest {

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @Mock
    private CachePort cachePort;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private CreateClienteUseCase createClienteUseCase;

    private ClienteCreateRequest request;
    private Cliente cliente;
    private ClienteResponse response;

    @BeforeEach
    void setUp() {
        request = ClienteCreateRequest.builder()
                .nombre("Juan Perez")
                .email("juan@example.com")
                .telefono("1234567890")
                .build();

        cliente = Cliente.builder()
                .id(UUID.randomUUID())
                .nombre("Juan Perez")
                .email("juan@example.com")
                .telefono("1234567890")
                .activo(true)
                .version(0L)
                .build();

        response = ClienteResponse.builder()
                .id(cliente.getId())
                .nombre("Juan Perez")
                .email("juan@example.com")
                .telefono("1234567890")
                .activo(true)
                .version(0L)
                .build();
    }

    @Test
    void execute_WhenEmailNotExists_ShouldCreateCliente() {
        when(clienteRepository.existsByEmailIgnoreCase(anyString())).thenReturn(Mono.just(false));
        when(clienteMapper.toEntity(any(ClienteCreateRequest.class))).thenReturn(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(Mono.just(cliente));
        when(cachePort.set(anyString(), any(), any(Duration.class))).thenReturn(Mono.just(true));
        when(cachePort.deleteByPattern(anyString())).thenReturn(Mono.just(true));
        when(clienteMapper.toResponse(any(Cliente.class))).thenReturn(response);

        StepVerifier.create(createClienteUseCase.execute(request))
                .expectNext(response)
                .verifyComplete();

        verify(clienteRepository).existsByEmailIgnoreCase("juan@example.com");
        verify(clienteRepository).save(any(Cliente.class));
        verify(cachePort).set(anyString(), any(), any(Duration.class));
        verify(cachePort).deleteByPattern("clientes:list:*");
    }

    @Test
    void execute_WhenEmailExists_ShouldThrowDuplicateEmailException() {
        when(clienteRepository.existsByEmailIgnoreCase(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(createClienteUseCase.execute(request))
                .expectError(DuplicateEmailException.class)
                .verify();

        verify(clienteRepository).existsByEmailIgnoreCase("juan@example.com");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }
}
