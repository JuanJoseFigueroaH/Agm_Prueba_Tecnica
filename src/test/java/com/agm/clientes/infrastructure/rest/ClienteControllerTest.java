package com.agm.clientes.infrastructure.rest;

import com.agm.clientes.application.dto.request.ClienteCreateRequest;
import com.agm.clientes.application.dto.response.ClienteResponse;
import com.agm.clientes.application.usecase.*;
import com.agm.clientes.domain.exception.ClienteNotFoundException;
import com.agm.clientes.domain.exception.DuplicateEmailException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock
    private CreateClienteUseCase createClienteUseCase;

    @Mock
    private GetClienteByIdUseCase getClienteByIdUseCase;

    @Mock
    private ListClientesUseCase listClientesUseCase;

    @Mock
    private UpdateClienteUseCase updateClienteUseCase;

    @Mock
    private PatchClienteUseCase patchClienteUseCase;

    @Mock
    private DeleteClienteUseCase deleteClienteUseCase;

    @Mock
    private ToggleClienteActivoUseCase toggleClienteActivoUseCase;

    @InjectMocks
    private ClienteController clienteController;

    private ClienteCreateRequest createRequest;
    private ClienteResponse clienteResponse;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        
        createRequest = ClienteCreateRequest.builder()
                .nombre("Juan Perez")
                .email("juan@example.com")
                .telefono("1234567890")
                .build();

        clienteResponse = ClienteResponse.builder()
                .id(clienteId)
                .nombre("Juan Perez")
                .email("juan@example.com")
                .telefono("1234567890")
                .activo(true)
                .version(0L)
                .build();
    }

    @Test
    void create_WhenValidRequest_ShouldReturnCreatedResponse() {
        when(createClienteUseCase.execute(any(ClienteCreateRequest.class)))
                .thenReturn(Mono.just(clienteResponse));

        StepVerifier.create(clienteController.create(createRequest))
                .expectNextMatches(response -> 
                        response.getStatus() == 201 && 
                        response.getData().equals(clienteResponse))
                .verifyComplete();

        verify(createClienteUseCase).execute(createRequest);
    }

    @Test
    void getById_WhenClienteExists_ShouldReturnCliente() {
        when(getClienteByIdUseCase.execute(any(UUID.class)))
                .thenReturn(Mono.just(clienteResponse));

        StepVerifier.create(clienteController.getById(clienteId))
                .expectNextMatches(response -> 
                        response.getStatus() == 200 && 
                        response.getData().equals(clienteResponse))
                .verifyComplete();

        verify(getClienteByIdUseCase).execute(clienteId);
    }

    @Test
    void delete_WhenClienteExists_ShouldCompleteSuccessfully() {
        when(deleteClienteUseCase.execute(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(clienteController.delete(clienteId))
                .verifyComplete();

        verify(deleteClienteUseCase).execute(clienteId);
    }
}
