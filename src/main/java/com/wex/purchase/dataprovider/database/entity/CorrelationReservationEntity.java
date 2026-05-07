package com.wex.purchase.dataprovider.database.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "correlation_reservation")
public class CorrelationReservationEntity {

    @Id
    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt;

    protected CorrelationReservationEntity() {}

    public CorrelationReservationEntity(UUID correlationId, Instant reservedAt) {
        this.correlationId = correlationId;
        this.reservedAt = reservedAt;
    }

    @PrePersist
    void onCreate() {
        if (reservedAt == null) {
            reservedAt = Instant.now();
        }
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public Instant getReservedAt() {
        return reservedAt;
    }
}
