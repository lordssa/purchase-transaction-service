package com.wex.purchase.entrypoint.controller.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.util.UriComponentsBuilder;

import com.wex.purchase.entrypoint.controller.PurchaseTransactionController;
import com.wex.purchase.entrypoint.controller.request.PurchaseTransactionRequest;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void handleValidation_returns400WithFieldErrors() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/purchase/transactions");

        Method method = PurchaseTransactionController.class.getMethod(
                "createPurchaseTransaction",
                PurchaseTransactionRequest.class,
                UriComponentsBuilder.class,
                String.class
        );
        MethodParameter parameter = new MethodParameter(method, 0);

        PurchaseTransactionRequest body = new PurchaseTransactionRequest(
                "",
                LocalDate.of(2026, 5, 7),
                new BigDecimal("10.00")
        );
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(body, "purchaseTransactionRequest");
        bindingResult.rejectValue("description", "NotBlank", "must not be blank");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().errors()).isNotEmpty();
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("description");
    }

    @Test
    void handleGenericException_returns500WithoutLeakingDetails() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/purchase/transactions");

        ResponseEntity<ApiErrorResponse> response = handler.handleGenericException(
                new RuntimeException("internal detail"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).doesNotContain("internal detail");
        assertThat(response.getBody().errors()).isEmpty();
    }
}
