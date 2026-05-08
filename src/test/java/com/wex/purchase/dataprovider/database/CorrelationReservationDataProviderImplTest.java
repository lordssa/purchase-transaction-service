package com.wex.purchase.dataprovider.database;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;

import com.wex.purchase.application.exception.DuplicateCorrelationException;
import com.wex.purchase.dataprovider.database.entity.CorrelationReservationEntity;

@ExtendWith(MockitoExtension.class)
class CorrelationReservationDataProviderImplTest {

    @Mock
    private EntityManager entityManager;

    private CorrelationReservationDataProviderImpl dataProvider;

    @BeforeEach
    void setUp() {
        dataProvider = new CorrelationReservationDataProviderImpl();
        ReflectionTestUtils.setField(dataProvider, "entityManager", entityManager);
    }

    @Test
    void reserve_persistsAndFlushes() {
        UUID id = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

        dataProvider.reserve(id);

        verify(entityManager).persist(any(CorrelationReservationEntity.class));
        verify(entityManager).flush();
    }

    @Test
    void reserve_onDuplicateKey_throwsDuplicateCorrelationException() {
        UUID id = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        var dup = new DataIntegrityViolationException("unique", new SQLException("dup", "23505", 0));
        doThrow(dup).when(entityManager).persist(any());

        assertThatThrownBy(() -> dataProvider.reserve(id))
                .isInstanceOf(DuplicateCorrelationException.class)
                .hasMessageContaining("already submitted");
    }

    @Test
    void reserve_onNonDuplicateRuntime_rethrows() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        doThrow(new IllegalStateException("unexpected")).when(entityManager).persist(any());

        assertThatThrownBy(() -> dataProvider.reserve(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("unexpected");
    }
}
