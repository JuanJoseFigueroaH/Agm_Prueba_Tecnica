package com.agm.clientes.application.usecase;

import com.agm.clientes.application.dto.response.ClienteResponse;
import com.agm.clientes.application.mapper.ClienteMapper;
import com.agm.clientes.domain.exception.ClienteDeletedException;
import com.agm.clientes.domain.exception.ClienteNotFoundException;
import com.agm.clientes.domain.model.Cliente;
import com.agm.clientes.domain.port.out.CachePort;
import com.agm.clientes.domain.port.out.ClienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetClienteByIdUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final CachePort cachePort;
    private final ClienteMapper clienteMapper;

    public Mono<ClienteResponse> execute(UUID id) {
        log.info("Consultando cliente con ID: {}", id);

        String cacheKey = "cliente:" + id;

        return cachePort.get(cacheKey, Cliente.class)
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Cliente no encontrado en cache, consultando base de datos");
                    return clienteRepository.findById(id)
                            .switchIfEmpty(Mono.error(new ClienteNotFoundException(id)))
                            .flatMap(cliente -> cachePort.set(cacheKey, cliente, Duration.ofMinutes(5))
                                    .thenReturn(cliente));
                }))
                .flatMap(cliente -> {
                    if (cliente.isDeleted()) {
                        log.warn("Intento de acceso a cliente eliminado: {}", id);
                        return Mono.error(new ClienteDeletedException(id));
                    }
                    log.info("Cliente encontrado: {}", id);
                    return Mono.just(clienteMapper.toResponse(cliente));
                });
    }
}
