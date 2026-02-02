package com.agm.clientes.infrastructure.rest;

import com.agm.clientes.application.dto.request.ClienteCreateRequest;
import com.agm.clientes.application.dto.request.ClientePatchRequest;
import com.agm.clientes.application.dto.request.ClienteUpdateRequest;
import com.agm.clientes.application.dto.response.ApiResponse;
import com.agm.clientes.application.dto.response.ClienteResponse;
import com.agm.clientes.application.dto.response.PageResponse;
import com.agm.clientes.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "API para gestión de clientes")
public class ClienteController {

    private final CreateClienteUseCase createClienteUseCase;
    private final GetClienteByIdUseCase getClienteByIdUseCase;
    private final ListClientesUseCase listClientesUseCase;
    private final UpdateClienteUseCase updateClienteUseCase;
    private final PatchClienteUseCase patchClienteUseCase;
    private final DeleteClienteUseCase deleteClienteUseCase;
    private final ToggleClienteActivoUseCase toggleClienteActivoUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cliente", description = "Crea un nuevo cliente en el sistema")
    public Mono<ApiResponse<ClienteResponse>> create(@Valid @RequestBody ClienteCreateRequest request) {
        return createClienteUseCase.execute(request)
                .map(response -> ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Cliente creado exitosamente",
                        response
                ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Consulta un cliente específico por su ID")
    public Mono<ApiResponse<ClienteResponse>> getById(
            @Parameter(description = "ID del cliente") @PathVariable UUID id) {
        return getClienteByIdUseCase.execute(id)
                .map(response -> ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Cliente encontrado",
                        response
                ));
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Lista todos los clientes con filtros, paginación y ordenamiento")
    public Mono<ApiResponse<PageResponse<ClienteResponse>>> list(
            @Parameter(description = "Filtrar por estado activo") @RequestParam(required = false) Boolean activo,
            @Parameter(description = "Incluir clientes eliminados") @RequestParam(defaultValue = "false") Boolean includeDeleted,
            @Parameter(description = "Búsqueda por nombre o email") @RequestParam(required = false) String q,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        return listClientesUseCase.execute(activo, includeDeleted, q, page, size, sortBy, sortDirection)
                .map(response -> ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Clientes listados exitosamente",
                        response
                ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza completamente un cliente existente")
    public Mono<ApiResponse<ClienteResponse>> update(
            @Parameter(description = "ID del cliente") @PathVariable UUID id,
            @Valid @RequestBody ClienteUpdateRequest request) {
        return updateClienteUseCase.execute(id, request)
                .map(response -> ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Cliente actualizado exitosamente",
                        response
                ));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente cliente", description = "Actualiza parcialmente un cliente existente")
    public Mono<ApiResponse<ClienteResponse>> patch(
            @Parameter(description = "ID del cliente") @PathVariable UUID id,
            @Valid @RequestBody ClientePatchRequest request) {
        return patchClienteUseCase.execute(id, request)
                .map(response -> ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Cliente actualizado parcialmente",
                        response
                ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar cliente", description = "Realiza un borrado lógico del cliente")
    public Mono<Void> delete(@Parameter(description = "ID del cliente") @PathVariable UUID id) {
        return deleteClienteUseCase.execute(id);
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar/Desactivar cliente", description = "Cambia el estado activo del cliente")
    public Mono<ApiResponse<ClienteResponse>> toggleActivo(
            @Parameter(description = "ID del cliente") @PathVariable UUID id,
            @Parameter(description = "Nuevo estado activo") @RequestParam boolean value) {
        return toggleClienteActivoUseCase.execute(id, value)
                .map(response -> ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Estado del cliente actualizado",
                        response
                ));
    }
}
