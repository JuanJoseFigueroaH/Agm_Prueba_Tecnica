package com.agm.clientes.infrastructure.cache;

import com.agm.clientes.domain.port.out.CachePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheAdapter implements CachePort {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public <T> Mono<T> get(String key, Class<T> type) {
        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(value -> {
                    try {
                        T result = objectMapper.readValue(value, type);
                        log.debug("Cache hit para key: {}", key);
                        return Mono.just(result);
                    } catch (JsonProcessingException e) {
                        log.error("Error deserializando valor de cache: {}", key, e);
                        return Mono.empty();
                    }
                })
                .doOnError(e -> log.error("Error obteniendo valor de cache: {}", key, e))
                .onErrorResume(e -> Mono.empty());
    }

    @Override
    public <T> Mono<Boolean> set(String key, T value, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            return redisTemplate.opsForValue()
                    .set(key, jsonValue, ttl)
                    .doOnSuccess(result -> log.debug("Valor guardado en cache: {}", key))
                    .doOnError(e -> log.error("Error guardando en cache: {}", key, e))
                    .onErrorReturn(false);
        } catch (JsonProcessingException e) {
            log.error("Error serializando valor para cache: {}", key, e);
            return Mono.just(false);
        }
    }

    @Override
    public Mono<Boolean> delete(String key) {
        return redisTemplate.delete(key)
                .map(count -> count > 0)
                .doOnSuccess(result -> log.debug("Cache eliminado para key: {}", key))
                .doOnError(e -> log.error("Error eliminando cache: {}", key, e))
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> deleteByPattern(String pattern) {
        return redisTemplate.keys(pattern)
                .flatMap(redisTemplate::delete)
                .reduce(0L, Long::sum)
                .map(count -> count > 0)
                .doOnSuccess(result -> log.debug("Cache eliminado para pattern: {}", pattern))
                .doOnError(e -> log.error("Error eliminando cache por pattern: {}", pattern, e))
                .onErrorReturn(false);
    }
}
