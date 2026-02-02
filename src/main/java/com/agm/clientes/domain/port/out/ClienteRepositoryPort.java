package com.agm.clientes.domain.port.out;

import com.agm.clientes.domain.model.Cliente;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ClienteRepositoryPort {

    Mono<Cliente> save(Cliente cliente);

    Mono<Cliente> findById(UUID id);

    Flux<Cliente> findAll(Boolean activo, Boolean includeDeleted, String query, int page, int size, String sortBy, String sortDirection);

    Mono<Boolean> existsByEmailIgnoreCase(String email);

    Mono<Boolean> existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    Mono<Void> deleteById(UUID id);

    Mono<Long> count(Boolean activo, Boolean includeDeleted, String query);
}
