package com.agm.clientes.application.usecase;

import com.agm.clientes.application.dto.request.ClientePatchRequest;
import com.agm.clientes.application.dto.response.ClienteResponse;
import com.agm.clientes.application.mapper.ClienteMapper;
import com.agm.clientes.domain.exception.ClienteDeletedException;
import com.agm.clientes.domain.exception.ClienteNotFoundException;
import com.agm.clientes.domain.exception.DuplicateEmailException;
import com.agm.clientes.domain.exception.OptimisticLockException;
import com.agm.clientes.domain.port.out.CachePort;
import com.agm.clientes.domain.port.out.ClienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatchClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final CachePort cachePort;
    private final ClienteMapper clienteMapper;

    public Mono<ClienteResponse> execute(UUID id, ClientePatchRequest request) {
        log.info("Aplicando actualización parcial a cliente con ID: {}", id);

        return clienteRepository.findById(id)
                .switchIfEmpty(Mono.error(new ClienteNotFoundException(id)))
                .flatMap(existing -> {
                    if (existing.isDeleted()) {
                        log.warn("Intento de actualizar parcialmente cliente eliminado: {}", id);
                        return Mono.error(new ClienteDeletedException(id));
                    }

                    if (!existing.getVersion().equals(request.getVersion())) {
                        log.warn("Conflicto de versión detectado para cliente: {}", id);
                        return Mono.error(new OptimisticLockException());
                    }

                    if (request.getEmail() != null && !existing.getEmail().equalsIgnoreCase(request.getEmail())) {
                        return clienteRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), id)
                                .flatMap(exists -> {
                                    if (exists) {
                                        log.warn("Email duplicado detectado: {}", request.getEmail());
                                        return Mono.error(new DuplicateEmailException(request.getEmail()));
                                    }
                                    return Mono.just(existing);
                                });
                    }

                    return Mono.just(existing);
                })
                .flatMap(existing -> {
                    clienteMapper.updateEntityFromPatch(request, existing);
                    existing.setUpdatedAt(LocalDateTime.now());

                    return clienteRepository.save(existing)
                            .onErrorMap(OptimisticLockingFailureException.class, 
                                    e -> new OptimisticLockException())
                            .doOnSuccess(saved -> log.info("Cliente actualizado parcialmente: {}", id))
                            .flatMap(saved -> cachePort.delete("cliente:" + id)
                                    .then(cachePort.deleteByPattern("clientes:list:*"))
                                    .thenReturn(saved))
                            .map(clienteMapper::toResponse);
                });
    }
}
