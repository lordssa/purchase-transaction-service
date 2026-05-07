package com.wex.purchase.dataprovider.database;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.wex.purchase.application.dataprovider.CorrelationReservationDataProvider;
import com.wex.purchase.application.exception.DuplicateCorrelationException;
import com.wex.purchase.dataprovider.database.entity.CorrelationReservationEntity;

@Component
public class CorrelationReservationDataProviderImpl implements CorrelationReservationDataProvider {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void reserve(UUID correlationId) {
        try {
            entityManager.persist(new CorrelationReservationEntity(correlationId, Instant.now()));
            entityManager.flush();
        } catch (RuntimeException e) {
            if (isDuplicateKey(e)) {
                throw new DuplicateCorrelationException(
                        "A request with this correlation id was already submitted"
                );
            }
            throw e;
        }
    }

   
    private static boolean isDuplicateKey(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof DataIntegrityViolationException) {
                return true;
            }
            if (t instanceof ConstraintViolationException) {
                return true;
            }
            if (t instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
