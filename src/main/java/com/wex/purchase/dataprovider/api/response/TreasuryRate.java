package com.wex.purchase.dataprovider.api.response;

public record TreasuryRate(
    String record_date,
    String currency,
    String exchange_rate,
    String effective_date
) {}
