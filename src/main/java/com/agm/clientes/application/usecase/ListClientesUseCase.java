package com.agm.clientes.application.usecase;

import com.agm.clientes.application.dto.response.ClienteResponse;
import com.agm.clientes.application.dto.response.PageResponse;
import com.agm.clientes.application.mapper.ClienteMapper;
import com.agm.clientes.domain.model.Cliente;
import com.agm.clientes.domain.port.out.CachePort;
import com.agm.clientes.domain.port.out.ClienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListClientesUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final CachePort cachePort;
    private final ClienteMapper clienteMapper;

    public Mono<PageResponse<ClienteResponse>> execute(Boolean activo, Boolean includeDeleted, String query, 
                                                        int page, int size, String sortBy, String sortDirection) {
        log.info("Listando clientes - activo: {}, includeDeleted: {}, query: {}, page: {}, size: {}", 
                activo, includeDeleted, query, page, size);

        return clienteRepository.findAll(activo, includeDeleted, query, page, size, sortBy, sortDirection)
                .map(clienteMapper::toResponse)
                .collectList()
                .zipWith(clienteRepository.count(activo, includeDeleted, query))
                .map(tuple -> buildPageResponse(tuple.getT1(), tuple.getT2(), page, size))
                .doOnSuccess(result -> log.info("Clientes listados exitosamente: {} elementos", result.getTotalElements()));
    }

    private PageResponse<ClienteResponse> buildPageResponse(List<ClienteResponse> content, Long totalElements, 
                                                             int page, int size) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        return PageResponse.<ClienteResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
}
