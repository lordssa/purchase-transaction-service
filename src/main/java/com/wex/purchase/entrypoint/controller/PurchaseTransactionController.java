package com.wex.purchase.entrypoint.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;
import java.util.Optional;

import com.wex.purchase.application.usecase.CreatePurchaseTransactionUseCase;
import com.wex.purchase.application.usecase.GetPurchaseTransactionConvertedUseCase;
import com.wex.purchase.entrypoint.controller.mapper.PurchaseTransactionControllerMapper;
import com.wex.purchase.entrypoint.controller.request.PurchaseTransactionRequest;   
import com.wex.purchase.entrypoint.controller.response.PurchaseTransactionResponse;


@RestController
@Validated
@RequestMapping("v1/purchase")
public class PurchaseTransactionController {
    private final CreatePurchaseTransactionUseCase createPurchaseTransactionUseCase;
    private final GetPurchaseTransactionConvertedUseCase getPurchaseTransactionConvertedUseCase;

    public PurchaseTransactionController(
            CreatePurchaseTransactionUseCase createPurchaseTransactionUseCase,
            GetPurchaseTransactionConvertedUseCase getPurchaseTransactionConvertedUseCase
    ) {
        this.createPurchaseTransactionUseCase = createPurchaseTransactionUseCase;
        this.getPurchaseTransactionConvertedUseCase = getPurchaseTransactionConvertedUseCase;
    }

    @PostMapping("/transactions")
    public ResponseEntity<PurchaseTransactionResponse> createPurchaseTransaction(
            @Valid @RequestBody PurchaseTransactionRequest purchaseTransactionRequest,
            UriComponentsBuilder uriComponentsBuilder,
        @RequestHeader(HttpCorrelationIds.X_REQUEST_ID) String requestIdHeader
    ) {
        UUID requestId = HttpCorrelationIds.parseRequestId(requestIdHeader);
        return Optional.ofNullable(purchaseTransactionRequest)
        .map(PurchaseTransactionControllerMapper::toDomain)
        .map(purchaseTransaction -> createPurchaseTransactionUseCase.execute(requestId, purchaseTransaction))
        .map(PurchaseTransactionControllerMapper::toResponse)
        .map(response -> ResponseEntity
                .created(uriComponentsBuilder
                        .path("/v1/purchase/transactions/{id}")
                        .buildAndExpand(response.transactionId())
                        .toUri()
                )
                .body(response)
        )
        .orElseThrow(() -> new IllegalArgumentException("Purchase transaction request cannot be null"));            
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<PurchaseTransactionResponse> getPurchaseTransaction(
            @PathVariable UUID transactionId,
            @RequestParam(name = "currency", required = true) @NotBlank String currency,
            @RequestParam(name = "country", required = true) @NotBlank String country
    ) {
        final var convertedPurchaseTransaction = getPurchaseTransactionConvertedUseCase.execute(transactionId, currency, country);
        return ResponseEntity.ok(
                PurchaseTransactionControllerMapper.toResponse(convertedPurchaseTransaction)
        );
    }
}
