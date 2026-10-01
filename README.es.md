# Alquiler de Energía Tron vía API
## SDK Java por TronZap.com

[English](README.md) | **[Español](README.es.md)** | [Português](README.pt-br.md) | [Русский](README.ru.md)

[![Maven Central](https://img.shields.io/maven-central/v/com.tronzap/tronzap-java.svg)](https://central.sonatype.com/artifact/com.tronzap/tronzap-java)
[![CI](https://github.com/tron-energy-market/tronzap-sdk-java/actions/workflows/ci.yml/badge.svg)](https://github.com/tron-energy-market/tronzap-sdk-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

SDK oficial en Java para la API de TronZap.
Este SDK permite integrar fácilmente los servicios de TronZap para alquilar energía TRON.

TronZap.com permite [comprar energía TRON](https://tronzap.com/), reduciendo significativamente las comisiones en transferencias de USDT (TRC20).

👉 [Regístrate para obtener una clave API](https://tronzap.com) para comenzar a usar la API de TronZap e integrarla a través del SDK.

- Sitio web: https://tronzap.com
- Referencia de la API: https://docs.tronzap.com/
- Maven Central: https://central.sonatype.com/artifact/com.tronzap/tronzap-java

## Instalación

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

## Requisitos

- Java 17 o superior
- Una dependencia en tiempo de ejecución, Jackson Databind, de uso interno. Ningún tipo de Jackson aparece en la API pública del SDK.

## Inicio rápido

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
                .apiToken("su_api_token")
                .apiSecret("su_api_secret")
                .build();

        try {
            Balance balance = client.getBalance();
            System.out.println("balance: " + balance.balance() + " (deposit to " + balance.address() + ")");

            // Estima cuánta energía necesita una transferencia de USDT y compra exactamente esa cantidad.
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

Un recorrido ejecutable por todas las operaciones está en
[`BasicUsage.java`](src/test/java/com/tronzap/sdk/example/BasicUsage.java):

```bash
export TRONZAP_API_TOKEN=su_api_token
export TRONZAP_API_SECRET=su_api_secret
export TRONZAP_BASE_URL=api.tronzap.com   # opcional
./mvnw -q test-compile exec:java -Dexec.mainClass=com.tronzap.sdk.example.BasicUsage -Dexec.classpathScope=test
```

Por defecto solo lee y no gasta nada. Con `TRONZAP_ALLOW_PURCHASES=1` también
ejecuta los endpoints que crean transacciones y verificaciones AML, que debitan el
saldo de la cuenta. Consulta el comentario al inicio del archivo para ver las
demás variables opcionales.

## Configuración

El builder recibe las dos credenciales de tu panel: el token de API se envía como
bearer token y el secreto de API firma el cuerpo de cada solicitud. Todo lo demás
es opcional:

```java
TronzapClient client = TronzapClient.builder()
        .apiToken(apiToken)
        .apiSecret(apiSecret)
        .baseUrl("api.tronzap.com")              // por defecto TronzapClient.DEFAULT_BASE_URL
        .timeout(Duration.ofSeconds(10))         // toda la solicitud; por defecto 30 segundos
        .connectTimeout(Duration.ofSeconds(5))   // por defecto 10 segundos
        .userAgent("my-app/1.0")
        .build();
```

`baseUrl` acepta un dominio o una URL completa: si falta el esquema se usa
`https` y se elimina la barra final, así que `"api.tronzap.com"`,
`"api.tronzap.com/"` y `"https://api.tronzap.com"` son equivalentes. Indica un
esquema explícito para evitarlo, por ejemplo `"http://localhost:8080"` con un mock
local.

Para usar un proxy, tu propio executor o un `SSLContext` personalizado, pasa tu
propio `java.net.http.HttpClient`. Se usa tal cual y nunca se cierra. Configura el
connect timeout en ese cliente, porque `connectTimeout` solo configura el cliente
que crea el builder:

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

Un `TronzapClient` es inmutable, seguro para uso concurrente y no tiene estado
global, así que crea uno por cada juego de credenciales y compártelo. `timeout`
limita toda la solicitud, incluido un cuerpo de respuesta que llega lentamente.

## Métodos disponibles

| Método | Endpoint | Descripción |
|---|---|---|
| `getServices()` | `/v1/services` | Servicios disponibles y precios |
| `getBalance()` | `/v1/balance` | Saldo actual de la cuenta |
| `getAddressInfo(address)` | `/v1/address-info` | Recursos (energía, ancho de banda) y saldos (TRX, USDT) de una dirección |
| `estimateEnergy(request)` | `/v1/estimate-energy` | Energía que necesita una transferencia y su coste |
| `calculate(request)` | `/v1/calculate` | Precio de una compra sin crear la transacción |
| `createEnergyTransaction(request)` | `/v1/transaction/new` | Comprar energía |
| `createBandwidthTransaction(request)` | `/v1/transaction/new` | Comprar ancho de banda |
| `createResourceBundleTransaction(request)` | `/v1/transaction/new` | Comprar energía y ancho de banda en una sola transacción |
| `createAddressActivationTransaction(request)` | `/v1/transaction/new` | Activar una dirección TRON |
| `checkTransaction(request)` | `/v1/transaction/check` | Estado de una transacción, por id o id externo |
| `getDirectRechargeInfo()` | `/v1/direct-recharge-info` | Dirección y tarifas de recarga directa |
| `getAmlServices()` | `/v1/aml-checks` | Servicios AML y precios |
| `createAmlCheck(request)` | `/v1/aml-checks/new` | Iniciar una verificación AML |
| `checkAmlStatus(id)` | `/v1/aml-checks/check` | Estado y resultado de una verificación AML |
| `getAmlHistory()` / `getAmlHistory(request)` | `/v1/aml-checks/history` | Historial paginado de verificaciones AML |

Los parámetros son records inmutables en `com.tronzap.sdk.request`. Cada uno tiene
una fábrica `of(...)` con los valores obligatorios, y los que tienen varios valores
opcionales también tienen un `builder(...)`. Una solicitud se valida al crearse,
así que una solicitud inválida nunca se envía. Los valores por defecto coinciden
con la API: `duration` es 1 hora y el historial AML empieza en la página 1 con 10
elementos.

Los resultados son records inmutables en `com.tronzap.sdk.response`. Las
colecciones nunca son `null`, y los valores que la API puede omitir son
`Optional`.

### Comprar recursos

```java
// Energía, con activación opcional de la dirección en la misma llamada.
Transaction tx = client.createEnergyTransaction(
        EnergyTransactionRequest.builder("TRecipientAddress", 65000)
                .duration(1)          // horas; 1 o 24
                .externalId("order-42")
                .activateAddress(true)
                .build());

// Ancho de banda.
tx = client.createBandwidthTransaction(
        BandwidthTransactionRequest.of("TRecipientAddress", 345, "bandwidth-1"));

// Energía y ancho de banda juntos en una sola transacción.
tx = client.createResourceBundleTransaction(
        ResourceBundleTransactionRequest.builder("TRecipientAddress", 65000, 345)
                .externalId("bundle-1")
                .build());

// Solo la activación.
tx = client.createAddressActivationTransaction(
        AddressActivationRequest.of("TRecipientAddress", "activation-1"));
```

El precio de la energía es por unidad y el del ancho de banda por 1000 unidades:
en `getServices()`, `EnergyRate.price()` × 65000 es el coste de 65000 de energía,
mientras que 345 de ancho de banda con un `BandwidthRate.price()` de 1 cuestan
0.345.

Actualmente la API informa un paquete de recursos con `service()` igual a
`Service.ENERGY`, no `Service.RESOURCE_BUNDLE`. Consulta `params().amounts()` para
saber qué recursos contiene una transacción.

### Seguir una transacción

Una transacción pasa por `NEW` → `PENDING` → `SUCCESS` o `FAILED`:

```java
Transaction tx;
do {
    Thread.sleep(2000);
    tx = client.checkTransaction(CheckTransactionRequest.byExternalId("order-42"));
} while (tx.status() == TransactionStatus.NEW || tx.status() == TransactionStatus.PENDING);

System.out.println("finished as " + tx.status() + ", hash " + tx.hash().orElse("none"));
```

### Verificación AML

```java
AmlCheck check = client.createAmlCheck(AmlCheckRequest.forAddress("TRX", "TAddressToScreen"));
// o AmlCheckRequest.forHash("BTC", "bc1RecipientAddress", "TX_HASH", AmlDirection.WITHDRAWAL)

AmlCheck result = client.checkAmlStatus(check.id());
if (result.status() == AmlStatus.COMPLETED) {
    System.out.println(result.riskLevel() + " " + result.riskScore() + " " + result.riskFactors());
}
```

`riskScore()` está vacío hasta que termina la verificación. Una verificación
completada puede tener una puntuación de 0, que no es lo mismo que no tener
puntuación todavía.

## Gestión de errores

Todo fallo de una llamada a la API es una `TronzapException` no comprobada
(unchecked). Captura una subclase para tratar un tipo concreto de fallo:

```
TronzapException
├── ApiException                  — la API respondió con un código distinto de cero
├── HttpException                 — respuesta no 2xx sin payload de la API
│   ├── RateLimitException        — HTTP 429
│   ├── UnauthorizedException     — HTTP 401 o 403
│   └── ServerException           — HTTP 5xx
├── InvalidResponseException      — respuesta 2xx que el SDK no pudo leer
└── NetworkException              — no llegó ninguna respuesta
    ├── ConnectionException       — fallo de DNS, conexión rechazada
    ├── RequestTimeoutException   — la solicitud superó su timeout
    ├── SslException              — fallo del handshake TLS o del certificado
    └── RequestInterruptedException — el hilo que llamaba fue interrumpido
```

`ApiException`, `HttpException` e `InvalidResponseException` incluyen el estado
HTTP (`getStatusCode()`) y el cuerpo de la respuesta (`getResponseBody()`).
`ApiException` incluye además el código de error de la API, la clave del error y el
ID de la solicitud. Los argumentos inválidos lanzan `IllegalArgumentException`
antes de enviar nada.

```java
try {
    client.createEnergyTransaction(EnergyTransactionRequest.of("TRecipientAddress", 65000));
} catch (ApiException e) {
    // Fallo a nivel de aplicación: el código indica exactamente qué salió mal.
    switch (e.getErrorCode()) {
        case INVALID_TRON_ADDRESS ->
                // La clave puede precisarlo, p. ej. "invalid_tron_address.from_address"
                System.err.println("bad address: " + e.getErrorKey().orElse(""));
        case INSUFFICIENT_FUNDS -> System.err.println("top up the account");
        case ADDRESS_NOT_ACTIVATED -> System.err.println("activate the address first");
        default -> System.err.printf("api error %d: %s (request %s)%n",
                e.getCode(), e.getMessage(), e.getRequestId().orElse("-"));
    }
} catch (RateLimitException e) {
    // Espera y reintenta.
} catch (UnauthorizedException e) {
    // Token o firma incorrectos.
} catch (RequestTimeoutException | ServerException e) {
    // Transitorio; se puede reintentar.
} catch (NetworkException e) {
    // Inalcanzable.
}
```

`getRequestId()` es el identificador que la API asigna a cada solicitud.
Indícalo al contactar con soporte.

Un error de la API tiene prioridad sobre el estado HTTP: la API informa de
algunos fallos con estado 2xx y de otros con 4xx o 5xx, así que un payload legible
con un código distinto de cero siempre se informa como `ApiException`, nunca como
`HttpException`.

### Códigos de error de la API

| Código | Constante | Descripción |
|------|----------|-------------|
| 1 | `AUTH_ERROR` | Error de autenticación: token de API o firma inválidos |
| 2 | `INVALID_SERVICE_OR_PARAMS` | Servicio o parámetros inválidos |
| 5 | `WALLET_NOT_FOUND` | Billetera interna no encontrada. Contacta con soporte. |
| 6 | `INSUFFICIENT_FUNDS` | Fondos insuficientes |
| 10 | `INVALID_TRON_ADDRESS` | Dirección TRON inválida |
| 11 | `INVALID_ENERGY_AMOUNT` | Cantidad de energía inválida |
| 12 | `INVALID_DURATION` | Duración inválida |
| 20 | `TRANSACTION_NOT_FOUND` | Transacción/suscripción no encontrada |
| 21 | `CANNOT_STOP_SUBSCRIPTION` | No se puede detener la suscripción |
| 24 | `ADDRESS_NOT_ACTIVATED` | Dirección no activada |
| 25 | `ADDRESS_ALREADY_ACTIVATED` | Dirección ya activada |
| 30 | `AML_CHECK_NOT_FOUND` | Verificación AML no encontrada |
| 35 | `SERVICE_NOT_AVAILABLE` | Servicio no disponible |
| 50 | `INVALID_BANDWIDTH_AMOUNT` | Cantidad de ancho de banda inválida |
| 500 | `INTERNAL_SERVER_ERROR` | Error interno del servidor: contacta con soporte |

Las constantes son valores del enum `ApiErrorCode`. Un código que esta versión del
SDK no conoce se informa como `ApiErrorCode.UNKNOWN`, y el número sigue disponible
en `getCode()`.

## Campos decimales y de fecha

Los importes y precios son `BigDecimal` y conservan la escala que envió la API, así
que compáralos con `compareTo` en lugar de `equals`. La API codifica el dinero como
número JSON en algunas respuestas y como cadena JSON en otras; ambas formas se leen
igual.

Las fechas son valores `Timestamp`: `value()` es el `OffsetDateTime` interpretado y
`raw()` es el texto tal como lo envió la API. Se aceptan los distintos formatos que
usa la API, y las horas sin desplazamiento se leen como UTC. Una fecha no
reconocida deja `value()` vacío en lugar de hacer fallar toda la respuesta.

Los valores que la API pueda añadir en el futuro, como un nuevo estado de
transacción, se informan como la constante `UNKNOWN` del enum correspondiente en
lugar de fallar.

## Pruebas

```bash
./mvnw verify
```

Ejecuta las pruebas unitarias contra un servidor HTTP local, comprueba el Javadoc y
la cobertura de pruebas, y construye los JAR principal, de fuentes y de Javadoc.

## Licencia

Licencia MIT (MIT). Consulta el [archivo de licencia](LICENSE) para más información.

## Soporte

Para soporte, contacta con [support@tronzap.com](mailto:support@tronzap.com).
