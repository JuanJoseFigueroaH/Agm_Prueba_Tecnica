package com.agm.clientes.infrastructure.persistence;

import com.agm.clientes.domain.model.Cliente;
import com.agm.clientes.domain.port.out.ClienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ClienteRepositoryImpl implements ClienteRepositoryPort {

    private final ClienteR2dbcRepository r2dbcRepository;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Cliente> save(Cliente cliente) {
        return r2dbcRepository.save(cliente);
    }

    @Override
    public Mono<Cliente> findById(UUID id) {
        return r2dbcRepository.findById(id);
    }

    @Override
    public Flux<Cliente> findAll(Boolean activo, Boolean includeDeleted, String query, 
                                  int page, int size, String sortBy, String sortDirection) {
        StringBuilder sql = new StringBuilder("SELECT * FROM clientes WHERE 1=1");

        if (!includeDeleted) {
            sql.append(" AND deleted_at IS NULL");
        }

        if (activo != null) {
            sql.append(" AND activo = :activo");
        }

        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(nombre) LIKE :query OR LOWER(email) LIKE :query)");
        }

        sql.append(" ORDER BY ").append(sortBy).append(" ").append(sortDirection);
        sql.append(" LIMIT :limit OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());

        if (activo != null) {
            spec = spec.bind("activo", activo);
        }

        if (query != null && !query.isBlank()) {
            spec = spec.bind("query", "%" + query.toLowerCase() + "%");
        }

        return spec.bind("limit", size)
                .bind("offset", page * size)
                .map((row, metadata) -> Cliente.builder()
                        .id(row.get("id", UUID.class))
                        .nombre(row.get("nombre", String.class))
                        .email(row.get("email", String.class))
                        .telefono(row.get("telefono", String.class))
                        .activo(row.get("activo", Boolean.class))
                        .deletedAt(row.get("deleted_at", java.time.LocalDateTime.class))
                        .createdAt(row.get("created_at", java.time.LocalDateTime.class))
                        .updatedAt(row.get("updated_at", java.time.LocalDateTime.class))
                        .version(row.get("version", Long.class))
                        .build())
                .all();
    }

    @Override
    public Mono<Boolean> existsByEmailIgnoreCase(String email) {
        return r2dbcRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Mono<Boolean> existsByEmailIgnoreCaseAndIdNot(String email, UUID id) {
        return r2dbcRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return r2dbcRepository.deleteById(id);
    }

    @Override
    public Mono<Long> count(Boolean activo, Boolean includeDeleted, String query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM clientes WHERE 1=1");

        if (!includeDeleted) {
            sql.append(" AND deleted_at IS NULL");
        }

        if (activo != null) {
            sql.append(" AND activo = :activo");
        }

        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(nombre) LIKE :query OR LOWER(email) LIKE :query)");
        }

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());

        if (activo != null) {
            spec = spec.bind("activo", activo);
        }

        if (query != null && !query.isBlank()) {
            spec = spec.bind("query", "%" + query.toLowerCase() + "%");
        }

        return spec.map(row -> row.get(0, Long.class))
                .one();
    }
}
