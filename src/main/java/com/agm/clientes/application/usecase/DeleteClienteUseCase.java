package com.agm.clientes.application.usecase;

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
public class DeleteClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final CachePort cachePort;

    public Mono<Void> execute(UUID id) {
        log.info("Eliminando lógicamente cliente con ID: {}", id);

        return clienteRepository.findById(id)
                .switchIfEmpty(Mono.error(new ClienteNotFoundException(id)))
                .flatMap(cliente -> {
                    cliente.markAsDeleted();
                    return clienteRepository.save(cliente);
                })
                .doOnSuccess(deleted -> log.info("Cliente eliminado lógicamente: {}", id))
                .flatMap(deleted -> cachePort.delete("cliente:" + id)
                        .then(cachePort.deleteByPattern("clientes:list:*")))
                .then();
    }
}
