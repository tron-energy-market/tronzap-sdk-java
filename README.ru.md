# Покупка энергии Tron через API
## Java SDK от TronZap.com

[English](README.md) | [Español](README.es.md) | [Português](README.pt-br.md) | **[Русский](README.ru.md)**

[![Maven Central](https://img.shields.io/maven-central/v/com.tronzap/tronzap-java.svg)](https://central.sonatype.com/artifact/com.tronzap/tronzap-java)
[![CI](https://github.com/tron-energy-market/tronzap-sdk-java/actions/workflows/ci.yml/badge.svg)](https://github.com/tron-energy-market/tronzap-sdk-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Официальный Java SDK для API TronZap.
Этот SDK позволяет легко интегрировать сервисы TronZap для аренды энергии TRON.

TronZap.com позволяет [покупать энергию TRON](https://tronzap.com/), существенно снижая комиссии при переводах USDT (TRC20).

👉 [Зарегистрируйтесь для получения API ключа](https://tronzap.com), чтобы начать использовать TronZap API.

- Сайт: https://tronzap.com
- Справочник API: https://docs.tronzap.com/
- Maven Central: https://central.sonatype.com/artifact/com.tronzap/tronzap-java

## Установка

Maven:

```xml
<dependency>
    <groupId>com.tronzap</groupId>
    <artifactId>tronzap-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("com.tronzap:tronzap-java:1.0.0")
```

## Требования

- Java 17 или новее
- Одна runtime-зависимость, Jackson Databind, используется внутри. Ни один тип Jackson не появляется в публичном API SDK.

## Быстрый старт

```java
import com.tronzap.sdk.TronzapClient;
import com.tronzap.sdk.exception.TronzapException;
import com.tronzap.sdk.request.EnergyTransactionRequest;
import com.tronzap.sdk.request.EstimateEnergyRequest;
import com.tronzap.sdk.response.Balance;
import com.tronzap.sdk.response.EnergyEstimate;
import com.tronzap.sdk.response.Transaction;

public class QuickStart {
    public static void main(String[] args) {
        TronzapClient client = TronzapClient.builder()
                .apiToken("ваш_api_token")
                .apiSecret("ваш_api_secret")
                .build();

        try {
            Balance balance = client.getBalance();
            System.out.println("balance: " + balance.balance() + " (deposit to " + balance.address() + ")");

            // Оцениваем, сколько энергии нужно для перевода USDT, и покупаем ровно столько.
            EnergyEstimate estimate = client.estimateEnergy(
                    EstimateEnergyRequest.of("TSenderAddress", "TRecipientAddress"));

            Transaction tx = client.createEnergyTransaction(
                    EnergyTransactionRequest.builder("TRecipientAddress", estimate.energy())
                            .duration(1)
                            .externalId("order-42")
                            .activateAddress(true)
                            .build());
            System.out.println("transaction " + tx.id() + " costs " + tx.amount() + " and is " + tx.status());
        } catch (TronzapException e) {
            System.err.println("TronZap call failed: " + e.getMessage());
        }
    }
}
```

Запускаемый пример со всеми операциями находится в
[`BasicUsage.java`](src/test/java/com/tronzap/sdk/example/BasicUsage.java):

```bash
export TRONZAP_API_TOKEN=ваш_api_token
export TRONZAP_API_SECRET=ваш_api_secret
export TRONZAP_BASE_URL=api.tronzap.com   # необязательно
./mvnw -q test-compile exec:java -Dexec.mainClass=com.tronzap.sdk.example.BasicUsage -Dexec.classpathScope=test
```

По умолчанию пример только читает данные и ничего не тратит. С
`TRONZAP_ALLOW_PURCHASES=1` он также вызывает endpoints, которые создают транзакции
и AML-проверки и списывают средства с баланса. Остальные необязательные переменные
описаны в комментарии в начале файла.

## Настройка

Builder принимает два ключа из личного кабинета: API-токен передаётся как bearer
token, а API-секрет подписывает тело каждого запроса. Всё остальное необязательно:

```java
TronzapClient client = TronzapClient.builder()
        .apiToken(apiToken)
        .apiSecret(apiSecret)
        .baseUrl("api.tronzap.com")              // по умолчанию TronzapClient.DEFAULT_BASE_URL
        .timeout(Duration.ofSeconds(10))         // весь запрос; по умолчанию 30 секунд
        .connectTimeout(Duration.ofSeconds(5))   // по умолчанию 10 секунд
        .userAgent("my-app/1.0")
        .build();
```

`baseUrl` принимает домен или полный URL: если схема не указана, используется
`https`, а завершающий слэш удаляется, поэтому `"api.tronzap.com"`,
`"api.tronzap.com/"` и `"https://api.tronzap.com"` равнозначны. Чтобы этого
избежать, укажите схему явно, например `"http://localhost:8080"` для локального
мока.

Чтобы использовать прокси, свой executor или собственный `SSLContext`, передайте
свой `java.net.http.HttpClient`. Он используется как есть и никогда не
закрывается. Connect timeout задайте в самом клиенте: `connectTimeout` настраивает
только клиент, который создаёт builder:

```java
HttpClient httpClient = HttpClient.newBuilder()
        .proxy(ProxySelector.of(new InetSocketAddress("proxy.internal", 3128)))
        .connectTimeout(Duration.ofSeconds(5))
        .build();

TronzapClient client = TronzapClient.builder()
        .apiToken(apiToken)
        .apiSecret(apiSecret)
        .httpClient(httpClient)
        .build();
```

`TronzapClient` неизменяем, безопасен для конкурентного использования и не
хранит глобального состояния, поэтому создайте один клиент на набор ключей и
используйте его совместно. `timeout` ограничивает весь запрос, включая медленно
приходящее тело ответа.

## Доступные методы

| Метод | Endpoint | Описание |
|---|---|---|
| `getServices()` | `/v1/services` | Доступные сервисы и цены |
| `getBalance()` | `/v1/balance` | Текущий баланс аккаунта |
| `getAddressInfo(address)` | `/v1/address-info` | Ресурсы (энергия, bandwidth) и балансы (TRX, USDT) адреса |
| `estimateEnergy(request)` | `/v1/estimate-energy` | Сколько энергии нужно для перевода и сколько она стоит |
| `calculate(request)` | `/v1/calculate` | Стоимость покупки без создания транзакции |
| `createEnergyTransaction(request)` | `/v1/transaction/new` | Купить энергию |
| `createBandwidthTransaction(request)` | `/v1/transaction/new` | Купить bandwidth |
| `createResourceBundleTransaction(request)` | `/v1/transaction/new` | Купить энергию и bandwidth одной транзакцией |
| `createAddressActivationTransaction(request)` | `/v1/transaction/new` | Активировать адрес TRON |
| `checkTransaction(request)` | `/v1/transaction/check` | Статус транзакции по id или внешнему id |
| `getDirectRechargeInfo()` | `/v1/direct-recharge-info` | Адрес и тарифы прямого пополнения |
| `getAmlServices()` | `/v1/aml-checks` | AML-сервисы и цены |
| `createAmlCheck(request)` | `/v1/aml-checks/new` | Запустить AML-проверку |
| `checkAmlStatus(id)` | `/v1/aml-checks/check` | Статус и результат AML-проверки |
| `getAmlHistory()` / `getAmlHistory(request)` | `/v1/aml-checks/history` | История AML-проверок с пагинацией |

Параметры — неизменяемые records в `com.tronzap.sdk.request`. У каждого есть
фабрика `of(...)` для обязательных значений, а у тех, где несколько
необязательных значений, есть ещё и `builder(...)`. Запрос проверяет себя при
создании, поэтому невалидный запрос никогда не отправляется. Значения по умолчанию
совпадают с API: `duration` — 1 час, история AML начинается со страницы 1 по 10
элементов.

Результаты — неизменяемые records в `com.tronzap.sdk.response`. Коллекции никогда
не бывают `null`, а значения, которые API может не прислать, — `Optional`.

### Покупка ресурсов

```java
// Энергия, при необходимости с активацией адреса в том же вызове.
Transaction tx = client.createEnergyTransaction(
        EnergyTransactionRequest.builder("TRecipientAddress", 65000)
                .duration(1)          // часы; 1 или 24
                .externalId("order-42")
                .activateAddress(true)
                .build());

// Bandwidth.
tx = client.createBandwidthTransaction(
        BandwidthTransactionRequest.of("TRecipientAddress", 345, "bandwidth-1"));

// Энергия и bandwidth вместе одной транзакцией.
tx = client.createResourceBundleTransaction(
        ResourceBundleTransactionRequest.builder("TRecipientAddress", 65000, 345)
                .externalId("bundle-1")
                .build());

// Только активация.
tx = client.createAddressActivationTransaction(
        AddressActivationRequest.of("TRecipientAddress", "activation-1"));
```

Цена энергии указана за единицу, цена bandwidth — за 1000 единиц: в
`getServices()` `EnergyRate.price()` × 65000 — это стоимость 65000 энергии, а 345
bandwidth при `BandwidthRate.price()`, равном 1, стоят 0.345.

Сейчас API возвращает пакет ресурсов с `service()`, равным `Service.ENERGY`, а не
`Service.RESOURCE_BUNDLE`. Состав покупки смотрите в `params().amounts()`.

### Отслеживание транзакции

Транзакция проходит путь `NEW` → `PENDING` → `SUCCESS` или `FAILED`:

```java
Transaction tx;
do {
    Thread.sleep(2000);
    tx = client.checkTransaction(CheckTransactionRequest.byExternalId("order-42"));
} while (tx.status() == TransactionStatus.NEW || tx.status() == TransactionStatus.PENDING);

System.out.println("finished as " + tx.status() + ", hash " + tx.hash().orElse("none"));
```

### AML-проверка

```java
AmlCheck check = client.createAmlCheck(AmlCheckRequest.forAddress("TRX", "TAddressToScreen"));
// или AmlCheckRequest.forHash("BTC", "bc1RecipientAddress", "TX_HASH", AmlDirection.WITHDRAWAL)

AmlCheck result = client.checkAmlStatus(check.id());
if (result.status() == AmlStatus.COMPLETED) {
    System.out.println(result.riskLevel() + " " + result.riskScore() + " " + result.riskFactors());
}
```

`riskScore()` пуст, пока проверка не завершится. У завершённой проверки score
может быть равен 0, и это не то же самое, что отсутствие результата.

## Обработка ошибок

Любой сбой вызова API — непроверяемое (unchecked) исключение `TronzapException`.
Ловите подкласс, чтобы обработать конкретный вид сбоя:

```
TronzapException
├── ApiException                  — API ответил ненулевым code
├── HttpException                 — ответ не 2xx без payload API
│   ├── RateLimitException        — HTTP 429
│   ├── UnauthorizedException     — HTTP 401 или 403
│   └── ServerException           — HTTP 5xx
├── InvalidResponseException      — ответ 2xx, который SDK не смог прочитать
└── NetworkException              — ответ не пришёл
    ├── ConnectionException       — ошибка DNS, соединение отклонено
    ├── RequestTimeoutException   — запрос превысил timeout
    ├── SslException              — сбой TLS-рукопожатия или сертификата
    └── RequestInterruptedException — вызывающий поток был прерван
```

`ApiException`, `HttpException` и `InvalidResponseException` содержат HTTP-статус
(`getStatusCode()`) и тело ответа (`getResponseBody()`). `ApiException` также
содержит код ошибки API, ключ ошибки и ID запроса. Невалидные аргументы вызывают
`IllegalArgumentException` ещё до отправки.

```java
try {
    client.createEnergyTransaction(EnergyTransactionRequest.of("TRecipientAddress", 65000));
} catch (ApiException e) {
    // Ошибка уровня приложения: код точно говорит, что пошло не так.
    switch (e.getErrorCode()) {
        case INVALID_TRON_ADDRESS ->
                // Ключ может уточнить, например "invalid_tron_address.from_address"
                System.err.println("bad address: " + e.getErrorKey().orElse(""));
        case INSUFFICIENT_FUNDS -> System.err.println("top up the account");
        case ADDRESS_NOT_ACTIVATED -> System.err.println("activate the address first");
        default -> System.err.printf("api error %d: %s (request %s)%n",
                e.getCode(), e.getMessage(), e.getRequestId().orElse("-"));
    }
} catch (RateLimitException e) {
    // Подождите и повторите.
} catch (UnauthorizedException e) {
    // Неверный токен или подпись.
} catch (RequestTimeoutException | ServerException e) {
    // Временный сбой; можно повторить.
} catch (NetworkException e) {
    // Сервер недоступен.
}
```

`getRequestId()` — идентификатор, который API присваивает каждому запросу.
Указывайте его при обращении в поддержку.

Ошибка API важнее HTTP-статуса: часть сбоев API возвращает со статусом 2xx, а
часть — с 4xx или 5xx, поэтому читаемый payload с ненулевым кодом всегда
сообщается как `ApiException`, а не как `HttpException`.

### Коды ошибок API

| Код | Константа | Описание |
|------|----------|-------------|
| 1 | `AUTH_ERROR` | Ошибка аутентификации: неверный API-токен или подпись |
| 2 | `INVALID_SERVICE_OR_PARAMS` | Неверный сервис или параметры |
| 5 | `WALLET_NOT_FOUND` | Внутренний кошелёк не найден. Обратитесь в поддержку. |
| 6 | `INSUFFICIENT_FUNDS` | Недостаточно средств |
| 10 | `INVALID_TRON_ADDRESS` | Неверный адрес TRON |
| 11 | `INVALID_ENERGY_AMOUNT` | Неверное количество энергии |
| 12 | `INVALID_DURATION` | Неверная длительность |
| 20 | `TRANSACTION_NOT_FOUND` | Транзакция/подписка не найдена |
| 21 | `CANNOT_STOP_SUBSCRIPTION` | Невозможно остановить подписку |
| 24 | `ADDRESS_NOT_ACTIVATED` | Адрес не активирован |
| 25 | `ADDRESS_ALREADY_ACTIVATED` | Адрес уже активирован |
| 30 | `AML_CHECK_NOT_FOUND` | AML-проверка не найдена |
| 35 | `SERVICE_NOT_AVAILABLE` | Сервис недоступен |
| 50 | `INVALID_BANDWIDTH_AMOUNT` | Неверное количество bandwidth |
| 500 | `INTERNAL_SERVER_ERROR` | Внутренняя ошибка сервера: обратитесь в поддержку |

Константы — значения enum `ApiErrorCode`. Код, который эта версия SDK не знает,
сообщается как `ApiErrorCode.UNKNOWN`, а число по-прежнему доступно через
`getCode()`.

## Числовые поля и даты

Суммы и цены — `BigDecimal` с тем масштабом, который прислал API, поэтому
сравнивайте их через `compareTo`, а не `equals`. В одних ответах API кодирует
деньги JSON-числом, в других — JSON-строкой; обе формы читаются одинаково.

Даты — значения `Timestamp`: `value()` — разобранный `OffsetDateTime`, `raw()` —
текст ровно в том виде, в каком его прислал API. Поддерживаются все форматы,
которые использует API, а время без смещения читается как UTC. Нераспознанная дата
оставляет `value()` пустым и не ломает весь ответ.

Значения, которые API может добавить в будущем, например новый статус транзакции,
сообщаются как константа `UNKNOWN` соответствующего enum и не приводят к ошибке.

## Тестирование

```bash
./mvnw verify
```

Запускает unit-тесты против локального HTTP-сервера, проверяет Javadoc и покрытие
тестами и собирает основной JAR, JAR с исходниками и JAR с Javadoc.

## Лицензия

Лицензия MIT (MIT). Подробнее в [файле лицензии](LICENSE).

## Поддержка

По вопросам поддержки пишите на [support@tronzap.com](mailto:support@tronzap.com).
