package com.wex.purchase.entrypoint.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.wex.purchase.application.exception.InvalidCorrelationIdFormatException;
import com.wex.purchase.application.exception.MissingCorrelationIdException;

class HttpCorrelationIdsTest {

    @Test
    void parseRequestId_returnsUuid() {
        UUID expected = UUID.fromString("11111111-1111-4111-8111-111111111111");

        assertThat(HttpCorrelationIds.parseRequestId("11111111-1111-4111-8111-111111111111")).isEqualTo(expected);
    }

    @Test
    void parseRequestId_trimsWhitespace() {
        UUID expected = UUID.fromString("22222222-2222-4222-8222-222222222222");

        assertThat(HttpCorrelationIds.parseRequestId("  22222222-2222-4222-8222-222222222222  "))
                .isEqualTo(expected);
    }

    @Test
    void parseRequestId_whenBlank_throwsMissingCorrelationIdException() {
        assertThatThrownBy(() -> HttpCorrelationIds.parseRequestId(null))
                .isInstanceOf(MissingCorrelationIdException.class)
                .hasMessageContaining(HttpCorrelationIds.X_REQUEST_ID);

        assertThatThrownBy(() -> HttpCorrelationIds.parseRequestId("   "))
                .isInstanceOf(MissingCorrelationIdException.class);
    }

    @Test
    void parseRequestId_whenInvalidUuid_throwsInvalidCorrelationIdFormatException() {
        assertThatThrownBy(() -> HttpCorrelationIds.parseRequestId("not-a-uuid"))
                .isInstanceOf(InvalidCorrelationIdFormatException.class);
    }
}
