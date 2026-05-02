package com.nequi.franchises.infrastructure.adapter.in.web.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nequi.franchises.application.exception.*;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = resolveStatus(ex);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        detail.setTitle(resolveTitle(ex));

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(detail);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof FranchiseNotFoundException)          return HttpStatus.NOT_FOUND;
        if (ex instanceof BranchNotFoundException)             return HttpStatus.NOT_FOUND;
        if (ex instanceof ProductNotFoundException)            return HttpStatus.NOT_FOUND;
        if (ex instanceof FranchiseNameAlreadyExistsException) return HttpStatus.CONFLICT;
        if (ex instanceof InvalidStockException)               return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveTitle(Throwable ex) {
        if (ex instanceof FranchiseNotFoundException)          return "Franchise Not Found";
        if (ex instanceof BranchNotFoundException)             return "Branch Not Found";
        if (ex instanceof ProductNotFoundException)            return "Product Not Found";
        if (ex instanceof FranchiseNameAlreadyExistsException) return "Franchise Name Conflict";
        if (ex instanceof InvalidStockException)               return "Invalid Stock Value";
        return "Internal Server Error";
    }
}
