package com.agm.clientes.application.usecase;

import com.agm.clientes.application.dto.request.ClienteUpdateRequest;
import com.agm.clientes.application.dto.response.ClienteResponse;
import com.agm.clientes.application.mapper.ClienteMapper;
import com.agm.clientes.domain.exception.ClienteDeletedException;
import com.agm.clientes.domain.exception.ClienteNotFoundException;
import com.agm.clientes.domain.exception.DuplicateEmailException;
import com.agm.clientes.domain.exception.OptimisticLockException;
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
class UpdateClienteUseCaseTest {

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @Mock
    private CachePort cachePort;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private UpdateClienteUseCase updateClienteUseCase;

    private UUID clienteId;
    private Cliente existingCliente;
    private ClienteUpdateRequest request;
    private Cliente updatedCliente;
    private ClienteResponse response;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        
        existingCliente = Cliente.builder()
                .id(clienteId)
                .nombre("Juan Perez")
                .email("juan@example.com")
                .telefono("1234567890")
                .activo(true)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .build();

        request = ClienteUpdateRequest.builder()
                .nombre("Juan Perez Updated")
                .email("juan@example.com")
                .telefono("9876543210")
                .activo(true)
                .version(0L)
                .build();

        updatedCliente = Cliente.builder()
                .id(clienteId)
                .nombre("Juan Perez Updated")
                .email("juan@example.com")
                .telefono("9876543210")
                .activo(true)
                .version(1L)
                .build();

        response = ClienteResponse.builder()
                .id(clienteId)
                .nombre("Juan Perez Updated")
                .email("juan@example.com")
                .telefono("9876543210")
                .activo(true)
                .version(1L)
                .build();
    }

    @Test
    void execute_WhenValidUpdate_ShouldUpdateCliente() {
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Mono.just(existingCliente));
        when(clienteMapper.toEntity(any(ClienteUpdateRequest.class))).thenReturn(updatedCliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(Mono.just(updatedCliente));
        when(cachePort.delete(anyString())).thenReturn(Mono.just(true));
        when(cachePort.deleteByPattern(anyString())).thenReturn(Mono.just(true));
        when(clienteMapper.toResponse(any(Cliente.class))).thenReturn(response);

        StepVerifier.create(updateClienteUseCase.execute(clienteId, request))
                .expectNext(response)
                .verifyComplete();

        verify(clienteRepository).findById(clienteId);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void execute_WhenClienteNotFound_ShouldThrowNotFoundException() {
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(updateClienteUseCase.execute(clienteId, request))
                .expectError(ClienteNotFoundException.class)
                .verify();
    }

    @Test
    void execute_WhenClienteIsDeleted_ShouldThrowClienteDeletedException() {
        existingCliente.setDeletedAt(LocalDateTime.now());
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Mono.just(existingCliente));

        StepVerifier.create(updateClienteUseCase.execute(clienteId, request))
                .expectError(ClienteDeletedException.class)
                .verify();
    }

    @Test
    void execute_WhenVersionMismatch_ShouldThrowOptimisticLockException() {
        request.setVersion(5L);
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Mono.just(existingCliente));

        StepVerifier.create(updateClienteUseCase.execute(clienteId, request))
                .expectError(OptimisticLockException.class)
                .verify();
    }

    @Test
    void execute_WhenEmailChangedAndDuplicate_ShouldThrowDuplicateEmailException() {
        request.setEmail("otro@example.com");
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Mono.just(existingCliente));
        when(clienteRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), any(UUID.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(updateClienteUseCase.execute(clienteId, request))
                .expectError(DuplicateEmailException.class)
                .verify();
    }
}
