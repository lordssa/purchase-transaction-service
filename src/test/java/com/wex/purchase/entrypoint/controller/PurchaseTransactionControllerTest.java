package com.wex.purchase.entrypoint.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wex.purchase.application.domain.ConvertedPurchaseTransaction;
import com.wex.purchase.application.domain.PurchaseTransaction;
import com.wex.purchase.application.exception.CurrencyConversionUnavailableException;
import com.wex.purchase.application.exception.DuplicateCorrelationException;
import com.wex.purchase.application.exception.ExternalServiceUnavailableException;
import com.wex.purchase.application.exception.PurchaseTransactionNotFoundException;
import com.wex.purchase.application.usecase.CreatePurchaseTransactionUseCase;
import com.wex.purchase.application.usecase.GetPurchaseTransactionConvertedUseCase;
import com.wex.purchase.entrypoint.controller.error.RestExceptionHandler;

@WebMvcTest(controllers = PurchaseTransactionController.class)
@Import(RestExceptionHandler.class)
class PurchaseTransactionControllerTest {

    private static final String REQUEST_ID = "33333333-3333-4333-8333-333333333333";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreatePurchaseTransactionUseCase createPurchaseTransactionUseCase;

    @MockitoBean
    private GetPurchaseTransactionConvertedUseCase getPurchaseTransactionConvertedUseCase;

    @Test
    void create_returns201CreatedWithLocationAndBody() throws Exception {
        UUID id = UUID.fromString("44444444-4444-4444-8444-444444444444");
        PurchaseTransaction saved = new PurchaseTransaction(
                id,
                "Supplies",
                LocalDate.of(2026, 5, 7),
                new BigDecimal("12.35")
        );
        when(createPurchaseTransactionUseCase.execute(any(), any())).thenReturn(saved);

        mockMvc.perform(post("/v1/purchase/transactions")
                        .header(HttpCorrelationIds.X_REQUEST_ID, REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Supplies","transactionDate":"2026-05-07","amountUsd":12.35}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/v1/purchase/transactions/" + id)))
                .andExpect(jsonPath("$.transactionId").value(id.toString()))
                .andExpect(jsonPath("$.description").value("Supplies"))
                .andExpect(jsonPath("$.amountUsd").value(12.35));
    }

    @Test
    void create_whenMissingRequestId_returns400() throws Exception {
        mockMvc.perform(post("/v1/purchase/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Supplies","transactionDate":"2026-05-07","amountUsd":12.35}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_whenInvalidRequestId_returns400() throws Exception {
        mockMvc.perform(post("/v1/purchase/transactions")
                        .header(HttpCorrelationIds.X_REQUEST_ID, "bad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Supplies","transactionDate":"2026-05-07","amountUsd":12.35}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("X-Request-Id"));
    }

    @Test
    void create_whenValidationFails_returns400() throws Exception {
        mockMvc.perform(post("/v1/purchase/transactions")
                        .header(HttpCorrelationIds.X_REQUEST_ID, REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"","transactionDate":"2026-05-07","amountUsd":12.35}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void create_whenDuplicateCorrelation_returns409() throws Exception {
        when(createPurchaseTransactionUseCase.execute(any(), any()))
                .thenThrow(new DuplicateCorrelationException("duplicate"));

        mockMvc.perform(post("/v1/purchase/transactions")
                        .header(HttpCorrelationIds.X_REQUEST_ID, REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Supplies","transactionDate":"2026-05-07","amountUsd":12.35}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("duplicate"));
    }

    @Test
    void create_whenMalformedJson_returns400() throws Exception {
        mockMvc.perform(post("/v1/purchase/transactions")
                        .header(HttpCorrelationIds.X_REQUEST_ID, REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is invalid"));
    }

    @Test
    void get_returns200WithConvertedFields() throws Exception {
        UUID transactionId = UUID.fromString("55555555-5555-4555-8555-555555555555");
        PurchaseTransaction tx = new PurchaseTransaction(
                transactionId,
                "Desk",
                LocalDate.of(2026, 1, 2),
                new BigDecimal("200.00")
        );
        ConvertedPurchaseTransaction converted = new ConvertedPurchaseTransaction(tx, "BRL", new BigDecimal("5.50"));
        when(getPurchaseTransactionConvertedUseCase.execute(eq(transactionId), eq("BRL"), eq("Brazil")))
                .thenReturn(converted);

        mockMvc.perform(get("/v1/purchase/transactions/{transactionId}", transactionId)
                        .param("currency", "BRL")
                        .param("country", "Brazil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$.exchangeRateUsed").value(5.5))
                .andExpect(jsonPath("$.convertedAmount").value(1100.0));
    }

    @Test
    void get_whenNotFound_returns404() throws Exception {
        UUID transactionId = UUID.fromString("66666666-6666-4666-8666-666666666666");
        when(getPurchaseTransactionConvertedUseCase.execute(eq(transactionId), eq("BRL"), eq("Brazil")))
                .thenThrow(new PurchaseTransactionNotFoundException(transactionId));

        mockMvc.perform(get("/v1/purchase/transactions/{transactionId}", transactionId)
                        .param("currency", "BRL")
                        .param("country", "Brazil"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void get_whenConversionUnavailable_returns422() throws Exception {
        UUID transactionId = UUID.fromString("77777777-7777-4777-8777-777777777777");
        when(getPurchaseTransactionConvertedUseCase.execute(eq(transactionId), eq("BRL"), eq("Brazil")))
                .thenThrow(new CurrencyConversionUnavailableException("BRL", LocalDate.of(2026, 5, 7)));

        mockMvc.perform(get("/v1/purchase/transactions/{transactionId}", transactionId)
                        .param("currency", "BRL")
                        .param("country", "Brazil"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void get_whenExternalServiceUnavailable_returns503() throws Exception {
        UUID transactionId = UUID.fromString("88888888-8888-4888-8888-888888888888");
        when(getPurchaseTransactionConvertedUseCase.execute(eq(transactionId), eq("BRL"), eq("Brazil")))
                .thenThrow(new ExternalServiceUnavailableException("Treasury", new RuntimeException("timeout")));

        mockMvc.perform(get("/v1/purchase/transactions/{transactionId}", transactionId)
                        .param("currency", "BRL")
                        .param("country", "Brazil"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void get_whenTransactionIdIsNotUuid_returns400() throws Exception {
        mockMvc.perform(get("/v1/purchase/transactions/not-a-uuid")
                        .param("currency", "BRL")
                        .param("country", "Brazil"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("transactionId"))
                .andExpect(jsonPath("$.errors[0].message").value("must be a valid UUID"));
    }

    @Test
    void get_whenCurrencyMissing_returns400() throws Exception {
        UUID transactionId = UUID.fromString("99999999-9999-4999-8999-999999999999");

        mockMvc.perform(get("/v1/purchase/transactions/{transactionId}", transactionId)
                        .param("country", "Brazil"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("currency"));
    }

    @Test
    void get_whenCurrencyBlank_returns400() throws Exception {
        UUID transactionId = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");

        mockMvc.perform(get("/v1/purchase/transactions/{transactionId}", transactionId)
                        .param("currency", "")
                        .param("country", "Brazil"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
