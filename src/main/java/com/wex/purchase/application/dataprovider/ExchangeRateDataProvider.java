package com.wex.purchase.application.dataprovider;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateDataProvider {

    BigDecimal getLatestRateWithinSixMonths(String currency, String country, LocalDate purchaseDate);
}

