package com.wex.purchase.application.dataprovider;

import java.util.UUID;


public interface CorrelationReservationDataProvider {
    void reserve(UUID correlationId);
}
