package com.agm.clientes.infrastructure.persistence;

import com.agm.clientes.domain.model.Cliente;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ClienteR2dbcRepository extends R2dbcRepository<Cliente, UUID> {

    Mono<Boolean> existsByEmailIgnoreCase(String email);

    Mono<Boolean> existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
}
