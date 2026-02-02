package com.agm.clientes.application.usecase;

import com.agm.clientes.application.dto.request.ClienteCreateRequest;
import com.agm.clientes.application.dto.response.ClienteResponse;
import com.agm.clientes.application.mapper.ClienteMapper;
import com.agm.clientes.domain.exception.DuplicateEmailException;
import com.agm.clientes.domain.model.Cliente;
import com.agm.clientes.domain.port.out.CachePort;
import com.agm.clientes.domain.port.out.ClienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final CachePort cachePort;
    private final ClienteMapper clienteMapper;

    public Mono<ClienteResponse> execute(ClienteCreateRequest request) {
        log.info("Iniciando creación de cliente con email: {}", request.getEmail());

        return clienteRepository.existsByEmailIgnoreCase(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Email duplicado detectado: {}", request.getEmail());
                        return Mono.error(new DuplicateEmailException(request.getEmail()));
                    }

                    Cliente cliente = clienteMapper.toEntity(request);
                    cliente.setId(UUID.randomUUID());
                    cliente.setCreatedAt(LocalDateTime.now());
                    cliente.setUpdatedAt(LocalDateTime.now());

                    return clienteRepository.save(cliente)
                            .doOnSuccess(saved -> log.info("Cliente creado exitosamente con ID: {}", saved.getId()))
                            .flatMap(saved -> cachePort.set("cliente:" + saved.getId(), saved, Duration.ofMinutes(5))
                                    .then(cachePort.deleteByPattern("clientes:list:*"))
                                    .thenReturn(saved))
                            .map(clienteMapper::toResponse);
                });
    }
}
