# Purchase Transactions API

Spring Boot service for storing purchase transactions (USD) and retrieving them with optional currency conversion using the US Treasury **Rates of Exchange** API.

## Tech
- Java **25** (per `pom.xml`)
- Spring Boot **4.x**
- Embedded DB: **H2** (in-memory) + Spring Data JPA
- HTTP client: Spring `RestClient` backed by JDK `HttpClient` using **virtual threads**

## Prerequisites
- **JDK 25** installed
- **Maven** installed

## Setup
Clone/open this folder, then build:

```bash
mvn clean compile
```

## Run

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.


## API
Base path: `v1/purchase`

### Create a transaction
**POST** `v1/purchase/transactions`

Headers:
- `Content-Type: application/json`
- `X-Request-Id: <uuid>` (**required**) — used for idempotency; sending the same value twice returns **409**

Body:
- `description` (string, max 50, not blank)
- `transactionDate` (date string `yyyy-MM-dd`)
- `amountUsd` (number > 0; extra decimals accepted and rounded to cents)

Example:

```bash
curl -i -X POST "http://localhost:8080/v1/purchase/transactions" 
  -H "Content-Type: application/json" 
  -H "X-Request-Id: d74ad249-a8e4-4368-9ea8-1d2145fcd3c7" 
  -d "{\"description\":\"Office supplies\",\"transactionDate\":\"2026-05-05\",\"amountUsd\":12.233}"
```

### Get a transaction
**GET** `v1/purchase/transactions/{transactionId}?currency=Dollar&country=Canada`

Query parameter:
- `currency` (**required**, not blank) — normalized (e.g. `dollar`, `DOLLAR` → `Dollar`) before calling Treasury.
- `country` (**required**, not blank) — normalized (e.g. `canada`, `CANADA` → `Canada`) before calling Treasury.

Example:

```bash
curl -i "http://localhost:8080/v1/purchase/transactions/e11c89b4-2866-44a0-882c-fbfb71ec1871?currency=Real&country=Brazil"
```

Response includes:
- `amountUsd` (original)
- `exchangeRateUsed` (latest rate <= purchase date within last 6 months)
- `convertedAmount` (rounded to 2 decimals)

Common cases:
- **400** validation / missing headers / invalid UUID / missing query params
- **404** transaction not found
- **409** duplicate `X-Request-Id`
- **422** currency conversion unavailable (no rate <= purchase date within last 6 months)
- **503** Treasury service unavailable/timeouts after retries

