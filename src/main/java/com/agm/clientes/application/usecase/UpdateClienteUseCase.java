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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final CachePort cachePort;
    private final ClienteMapper clienteMapper;

    public Mono<ClienteResponse> execute(UUID id, ClienteUpdateRequest request) {
        log.info("Actualizando cliente con ID: {}", id);

        return clienteRepository.findById(id)
                .switchIfEmpty(Mono.error(new ClienteNotFoundException(id)))
                .flatMap(existing -> {
                    if (existing.isDeleted()) {
                        log.warn("Intento de actualizar cliente eliminado: {}", id);
                        return Mono.error(new ClienteDeletedException(id));
                    }

                    if (!existing.getVersion().equals(request.getVersion())) {
                        log.warn("Conflicto de versión detectado para cliente: {}", id);
                        return Mono.error(new OptimisticLockException());
                    }

                    if (!existing.getEmail().equalsIgnoreCase(request.getEmail())) {
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
                    Cliente updated = clienteMapper.toEntity(request);
                    updated.setId(existing.getId());
                    updated.setCreatedAt(existing.getCreatedAt());
                    updated.setUpdatedAt(LocalDateTime.now());
                    updated.setDeletedAt(existing.getDeletedAt());

                    return clienteRepository.save(updated)
                            .onErrorMap(OptimisticLockingFailureException.class, 
                                    e -> new OptimisticLockException())
                            .doOnSuccess(saved -> log.info("Cliente actualizado exitosamente: {}", id))
                            .flatMap(saved -> cachePort.delete("cliente:" + id)
                                    .then(cachePort.deleteByPattern("clientes:list:*"))
                                    .thenReturn(saved))
                            .map(clienteMapper::toResponse);
                });
    }
}
