package com.agm.clientes.application.usecase;

import com.agm.clientes.application.dto.response.ClienteResponse;
import com.agm.clientes.application.mapper.ClienteMapper;
import com.agm.clientes.domain.exception.ClienteDeletedException;
import com.agm.clientes.domain.exception.ClienteNotFoundException;
import com.agm.clientes.domain.port.out.CachePort;
import com.agm.clientes.domain.port.out.ClienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToggleClienteActivoUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final CachePort cachePort;
    private final ClienteMapper clienteMapper;

    public Mono<ClienteResponse> execute(UUID id, boolean value) {
        log.info("Cambiando estado activo de cliente {} a: {}", id, value);

        return clienteRepository.findById(id)
                .switchIfEmpty(Mono.error(new ClienteNotFoundException(id)))
                .flatMap(cliente -> {
                    if (cliente.isDeleted()) {
                        log.warn("Intento de cambiar estado de cliente eliminado: {}", id);
                        return Mono.error(new ClienteDeletedException(id));
                    }

                    if (value) {
                        cliente.activate();
                    } else {
                        cliente.deactivate();
                    }

                    return clienteRepository.save(cliente)
                            .doOnSuccess(saved -> log.info("Estado activo cambiado para cliente: {}", id))
                            .flatMap(saved -> cachePort.delete("cliente:" + id)
                                    .then(cachePort.deleteByPattern("clientes:list:*"))
                                    .thenReturn(saved))
                            .map(clienteMapper::toResponse);
                });
    }
}
