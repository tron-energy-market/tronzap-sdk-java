# Changelog

All notable changes to this project are documented in this file. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.2.0] - 2026-11-20

### Added

- Bulk orders: `bulkCalculate`, `bulkCreate`, `bulkCheck`, `getBulkHistory` and `bulkReport` for
  `/v1/bulk/calculate`, `/v1/bulk/create`, `/v1/bulk/check`, `/v1/bulk/history` and `/v1/bulk/report`, with the
  requests `BulkItem`, `BulkCalculateRequest`, `BulkCreateRequest`, `BulkOrderRequest`, `BulkReportRequest` and
  `BulkHistoryRequest`, the results `BulkCalculation`, `BulkOrder` and `BulkHistory`, and `BulkRejectedRow`,
  `BulkStatus` and `BulkRowStatus`. `bulkCalculate` always orders energy; invalid rows are not checked by the SDK and
  come back in `rejected()` with their row number. `bulkReport` returns the CSV report as a string.
- `verifyWebhookSignature` checks the `X-Signature` header of a bulk order webhook against the raw body.
- `ApiException.getResult()` holds the `result` the API sends with an error, as JSON. Until now it was dropped.
- `BulkPriceChangedException` and `BulkNoValidItemsException`, subclasses of `ApiException` for codes 92 and 94,
  carry the new calculation and the rejected rows.
- `getRetryAfter()` on `ApiException` and `HttpException` holds how long to wait before retrying, from the
  `Retry-After` header (seconds or an HTTP date) or the `retry_after` field, or is empty when the server sent
  neither.
- Error codes `BULK_NOT_FOUND` (90), `BULK_EXPIRED` (91), `BULK_PRICE_CHANGED` (92), `BULK_TOO_MANY_ITEMS` (93),
  `BULK_NO_VALID_ITEMS` (94), `BULK_RESOURCE_NOT_SUPPORTED` (95), `PAYLOAD_TOO_LARGE` (96) and `RATE_LIMITED` (429).
- The example lists the bulk orders, calculates a bulk order for `TRONZAP_ADDRESS`, checks the order in
  `TRONZAP_BULK_ID` and prints its report, and with purchases allowed starts a one-address bulk order.
- This changelog.

### Changed

- Jackson 2.23.0.
- The README and the `RateLimitException` docs say that a rate limit reported by the API, HTTP 429 with a JSON body,
  is an `ApiException` with `RATE_LIMITED`, as it always has been; `RateLimitException` is only an HTTP 429 without
  an API body, such as one from a proxy.

## [1.1.0] - 2026-10-09

### Added

- Subscriptions: `getSubscriptions`, `startSubscription`, `checkSubscription`, `stopSubscription` and
  `getSubscriptionHistory`, with their requests, the results `SubscriptionPlan`, `Subscription` and
  `SubscriptionHistory`, and the `SubscriptionStatus` enum.

### Changed

- A hash AML check without a direction sends `deposit` explicitly. Address checks are unchanged.

### Deprecated

- `EnergyRate.minEnergy()` and `maxEnergy()`, `Calculation.energy()` and `EnergyEstimate.energy()`. Use
  `minAmount()`, `maxAmount()` and `amount()`, which return the same values.

### Fixed

- The docs of `EnergyRate.price()` and `DirectRechargeRate.price()`: they are the price of 1000 units, like
  `BandwidthRate.price()`, not of one unit. The values are unchanged.

## [1.0.0] - 2026-10-01

### Added

- The first release: `getServices`, `getBalance`, `getAddressInfo`, `estimateEnergy`, `calculate`, energy,
  bandwidth, resource bundle and address activation transactions, `checkTransaction`, `getDirectRechargeInfo` and
  AML checks.
- `TronzapClientBuilder` with the API token and secret, base URL, timeouts, `HttpClient` and `User-Agent`.
- The exception hierarchy under `TronzapException`: `ApiException` with `ApiErrorCode`, `HttpException`,
  `InvalidResponseException` and `NetworkException`, with their subclasses.

[Unreleased]: https://github.com/tron-energy-market/tronzap-sdk-java/compare/v1.2.0...HEAD
[1.2.0]: https://github.com/tron-energy-market/tronzap-sdk-java/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/tron-energy-market/tronzap-sdk-java/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/tron-energy-market/tronzap-sdk-java/releases/tag/v1.0.0
