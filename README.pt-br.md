# Aluguel de Energia Tron via API
## SDK Java por TronZap.com

[English](README.md) | [Español](README.es.md) | **[Português](README.pt-br.md)** | [Русский](README.ru.md)

[![Maven Central](https://img.shields.io/maven-central/v/com.tronzap/tronzap-java.svg)](https://central.sonatype.com/artifact/com.tronzap/tronzap-java)
[![CI](https://github.com/tron-energy-market/tronzap-sdk-java/actions/workflows/ci.yml/badge.svg)](https://github.com/tron-energy-market/tronzap-sdk-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

SDK oficial em Java para a API do TronZap.
Este SDK permite integrar facilmente os serviços TronZap para aluguel de energia TRON.

TronZap.com permite [comprar energia TRON](https://tronzap.com/), reduzindo significativamente as taxas nas transferências de USDT (TRC20).

👉 [Registre-se para obter uma chave API](https://tronzap.com) para começar a usar a API TronZap e integrá-la através do SDK.

- Site: https://tronzap.com
- Referência da API: https://docs.tronzap.com/
- Maven Central: https://central.sonatype.com/artifact/com.tronzap/tronzap-java

## Instalação

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

- Java 17 ou superior
- Uma dependência de runtime, Jackson Databind, usada internamente. Nenhum tipo do Jackson aparece na API pública do SDK.

## Início rápido

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
                .apiToken("seu_api_token")
                .apiSecret("seu_api_secret")
                .build();

        try {
            Balance balance = client.getBalance();
            System.out.println("balance: " + balance.balance() + " (deposit to " + balance.address() + ")");

            // Estima quanta energia uma transferência de USDT precisa e compra exatamente essa quantidade.
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

Um passo a passo executável de todas as operações está em
[`BasicUsage.java`](src/test/java/com/tronzap/sdk/example/BasicUsage.java):

```bash
export TRONZAP_API_TOKEN=seu_api_token
export TRONZAP_API_SECRET=seu_api_secret
export TRONZAP_BASE_URL=api.tronzap.com   # opcional
./mvnw -q test-compile exec:java -Dexec.mainClass=com.tronzap.sdk.example.BasicUsage -Dexec.classpathScope=test
```

Por padrão ele apenas lê e não gasta nada. Com `TRONZAP_ALLOW_PURCHASES=1` ele
também executa os endpoints que criam transações e verificações AML, que debitam o
saldo da conta. Veja o comentário no início do arquivo para as demais variáveis
opcionais.

## Configuração

O builder recebe as duas credenciais do seu painel: o token da API é enviado como
bearer token e o segredo da API assina o corpo de cada requisição. Todo o resto é
opcional:

```java
TronzapClient client = TronzapClient.builder()
        .apiToken(apiToken)
        .apiSecret(apiSecret)
        .baseUrl("api.tronzap.com")              // padrão: TronzapClient.DEFAULT_BASE_URL
        .timeout(Duration.ofSeconds(10))         // requisição inteira; padrão: 30 segundos
        .connectTimeout(Duration.ofSeconds(5))   // padrão: 10 segundos
        .userAgent("my-app/1.0")
        .build();
```

`baseUrl` aceita um domínio ou uma URL completa: sem esquema, usa-se `https`, e a
barra final é removida, então `"api.tronzap.com"`, `"api.tronzap.com/"` e
`"https://api.tronzap.com"` são equivalentes. Informe um esquema explícito para
evitar isso, por exemplo `"http://localhost:8080"` com um mock local.

Para usar um proxy, seu próprio executor ou um `SSLContext` personalizado, passe
seu próprio `java.net.http.HttpClient`. Ele é usado como está e nunca é fechado.
Configure o connect timeout nesse cliente, porque `connectTimeout` só configura o
cliente criado pelo builder:

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

Um `TronzapClient` é imutável, seguro para uso concorrente e não tem estado
global, então crie um por conjunto de credenciais e compartilhe-o. `timeout`
limita a requisição inteira, incluindo um corpo de resposta que chega lentamente.

## Métodos disponíveis

| Método | Endpoint | Descrição |
|---|---|---|
| `getServices()` | `/v1/services` | Serviços disponíveis e preços |
| `getBalance()` | `/v1/balance` | Saldo atual da conta |
| `getAddressInfo(address)` | `/v1/address-info` | Recursos (energia, largura de banda) e saldos (TRX, USDT) de um endereço |
| `estimateEnergy(request)` | `/v1/estimate-energy` | Energia necessária para uma transferência e seu custo |
| `calculate(request)` | `/v1/calculate` | Preço de uma compra sem criar a transação |
| `createEnergyTransaction(request)` | `/v1/transaction/new` | Comprar energia |
| `createBandwidthTransaction(request)` | `/v1/transaction/new` | Comprar largura de banda |
| `createResourceBundleTransaction(request)` | `/v1/transaction/new` | Comprar energia e largura de banda em uma única transação |
| `createAddressActivationTransaction(request)` | `/v1/transaction/new` | Ativar um endereço TRON |
| `checkTransaction(request)` | `/v1/transaction/check` | Status de uma transação, por id ou id externo |
| `getDirectRechargeInfo()` | `/v1/direct-recharge-info` | Endereço e tarifas de recarga direta |
| `getAmlServices()` | `/v1/aml-checks` | Serviços AML e preços |
| `createAmlCheck(request)` | `/v1/aml-checks/new` | Iniciar uma verificação AML |
| `checkAmlStatus(id)` | `/v1/aml-checks/check` | Status e resultado de uma verificação AML |
| `getAmlHistory()` / `getAmlHistory(request)` | `/v1/aml-checks/history` | Histórico paginado de verificações AML |

Os parâmetros são records imutáveis em `com.tronzap.sdk.request`. Cada um tem uma
fábrica `of(...)` com os valores obrigatórios, e os que têm vários valores
opcionais também têm um `builder(...)`. Uma requisição se valida ao ser criada,
então uma requisição inválida nunca é enviada. Os padrões coincidem com a API:
`duration` é 1 hora e o histórico AML começa na página 1 com 10 itens.

Os resultados são records imutáveis em `com.tronzap.sdk.response`. As coleções
nunca são `null`, e os valores que a API pode omitir são `Optional`.

### Comprar recursos

```java
// Energia, com ativação opcional do endereço na mesma chamada.
Transaction tx = client.createEnergyTransaction(
        EnergyTransactionRequest.builder("TRecipientAddress", 65000)
                .duration(1)          // horas; 1 ou 24
                .externalId("order-42")
                .activateAddress(true)
                .build());

// Largura de banda.
tx = client.createBandwidthTransaction(
        BandwidthTransactionRequest.of("TRecipientAddress", 345, "bandwidth-1"));

// Energia e largura de banda juntas em uma única transação.
tx = client.createResourceBundleTransaction(
        ResourceBundleTransactionRequest.builder("TRecipientAddress", 65000, 345)
                .externalId("bundle-1")
                .build());

// Apenas a ativação.
tx = client.createAddressActivationTransaction(
        AddressActivationRequest.of("TRecipientAddress", "activation-1"));
```

O preço da energia é por unidade e o da largura de banda é por 1000 unidades: em
`getServices()`, `EnergyRate.price()` × 65000 é o custo de 65000 de energia,
enquanto 345 de largura de banda com um `BandwidthRate.price()` de 1 custam 0.345.

Atualmente a API informa um pacote de recursos com `service()` igual a
`Service.ENERGY`, e não `Service.RESOURCE_BUNDLE`. Consulte `params().amounts()`
para saber quais recursos uma transação contém.

### Acompanhar uma transação

Uma transação passa por `NEW` → `PENDING` → `SUCCESS` ou `FAILED`:

```java
Transaction tx;
do {
    Thread.sleep(2000);
    tx = client.checkTransaction(CheckTransactionRequest.byExternalId("order-42"));
} while (tx.status() == TransactionStatus.NEW || tx.status() == TransactionStatus.PENDING);

System.out.println("finished as " + tx.status() + ", hash " + tx.hash().orElse("none"));
```

### Verificação AML

```java
AmlCheck check = client.createAmlCheck(AmlCheckRequest.forAddress("TRX", "TAddressToScreen"));
// ou AmlCheckRequest.forHash("BTC", "bc1RecipientAddress", "TX_HASH", AmlDirection.WITHDRAWAL)

AmlCheck result = client.checkAmlStatus(check.id());
if (result.status() == AmlStatus.COMPLETED) {
    System.out.println(result.riskLevel() + " " + result.riskScore() + " " + result.riskFactors());
}
```

`riskScore()` fica vazio até a verificação terminar. Uma verificação concluída pode
ter pontuação 0, o que não é o mesmo que ainda não ter pontuação.

## Tratamento de erros

Toda falha de uma chamada à API é uma `TronzapException` não verificada
(unchecked). Capture uma subclasse para tratar um tipo específico de falha:

```
TronzapException
├── ApiException                  — a API respondeu com um código diferente de zero
├── HttpException                 — resposta não 2xx sem payload da API
│   ├── RateLimitException        — HTTP 429
│   ├── UnauthorizedException     — HTTP 401 ou 403
│   └── ServerException           — HTTP 5xx
├── InvalidResponseException      — resposta 2xx que o SDK não conseguiu ler
└── NetworkException              — nenhuma resposta chegou
    ├── ConnectionException       — falha de DNS, conexão recusada
    ├── RequestTimeoutException   — a requisição excedeu o timeout
    ├── SslException              — falha no handshake TLS ou no certificado
    └── RequestInterruptedException — a thread chamadora foi interrompida
```

`ApiException`, `HttpException` e `InvalidResponseException` trazem o status HTTP
(`getStatusCode()`) e o corpo da resposta (`getResponseBody()`). `ApiException`
também traz o código de erro da API, a chave do erro e o ID da requisição.
Argumentos inválidos lançam `IllegalArgumentException` antes de qualquer envio.

```java
try {
    client.createEnergyTransaction(EnergyTransactionRequest.of("TRecipientAddress", 65000));
} catch (ApiException e) {
    // Falha no nível da aplicação: o código diz exatamente o que deu errado.
    switch (e.getErrorCode()) {
        case INVALID_TRON_ADDRESS ->
                // A chave pode detalhar, p. ex. "invalid_tron_address.from_address"
                System.err.println("bad address: " + e.getErrorKey().orElse(""));
        case INSUFFICIENT_FUNDS -> System.err.println("top up the account");
        case ADDRESS_NOT_ACTIVATED -> System.err.println("activate the address first");
        default -> System.err.printf("api error %d: %s (request %s)%n",
                e.getCode(), e.getMessage(), e.getRequestId().orElse("-"));
    }
} catch (RateLimitException e) {
    // Aguarde e tente novamente.
} catch (UnauthorizedException e) {
    // Token ou assinatura incorretos.
} catch (RequestTimeoutException | ServerException e) {
    // Transitório; pode tentar novamente.
} catch (NetworkException e) {
    // Inacessível.
}
```

`getRequestId()` é o identificador que a API atribui a cada requisição.
Informe-o ao contatar o suporte.

Um erro da API tem prioridade sobre o status HTTP: a API informa algumas falhas
com status 2xx e outras com 4xx ou 5xx, então um payload legível com código
diferente de zero é sempre informado como `ApiException`, nunca como
`HttpException`.

### Códigos de erro da API

| Código | Constante | Descrição |
|------|----------|-------------|
| 1 | `AUTH_ERROR` | Erro de autenticação: token da API ou assinatura inválidos |
| 2 | `INVALID_SERVICE_OR_PARAMS` | Serviço ou parâmetros inválidos |
| 5 | `WALLET_NOT_FOUND` | Carteira interna não encontrada. Contate o suporte. |
| 6 | `INSUFFICIENT_FUNDS` | Saldo insuficiente |
| 10 | `INVALID_TRON_ADDRESS` | Endereço TRON inválido |
| 11 | `INVALID_ENERGY_AMOUNT` | Quantidade de energia inválida |
| 12 | `INVALID_DURATION` | Duração inválida |
| 20 | `TRANSACTION_NOT_FOUND` | Transação/assinatura não encontrada |
| 21 | `CANNOT_STOP_SUBSCRIPTION` | Não é possível interromper a assinatura |
| 24 | `ADDRESS_NOT_ACTIVATED` | Endereço não ativado |
| 25 | `ADDRESS_ALREADY_ACTIVATED` | Endereço já ativado |
| 30 | `AML_CHECK_NOT_FOUND` | Verificação AML não encontrada |
| 35 | `SERVICE_NOT_AVAILABLE` | Serviço indisponível |
| 50 | `INVALID_BANDWIDTH_AMOUNT` | Quantidade de largura de banda inválida |
| 500 | `INTERNAL_SERVER_ERROR` | Erro interno do servidor: contate o suporte |

As constantes são valores do enum `ApiErrorCode`. Um código que esta versão do SDK
não conhece é informado como `ApiErrorCode.UNKNOWN`, e o número continua
disponível em `getCode()`.

## Campos decimais e de data

Valores e preços são `BigDecimal` e mantêm a escala enviada pela API, então
compare-os com `compareTo` em vez de `equals`. A API codifica dinheiro como número
JSON em algumas respostas e como string JSON em outras; as duas formas são lidas da
mesma maneira.

Datas são valores `Timestamp`: `value()` é o `OffsetDateTime` interpretado e
`raw()` é o texto exatamente como a API enviou. Os vários formatos usados pela API
são aceitos, e horários sem fuso são lidos como UTC. Uma data não reconhecida deixa
`value()` vazio em vez de fazer toda a resposta falhar.

Valores que a API venha a adicionar no futuro, como um novo status de transação,
são informados como a constante `UNKNOWN` do enum correspondente em vez de falhar.

## Testes

```bash
./mvnw verify
```

Executa os testes unitários contra um servidor HTTP local, verifica o Javadoc e a
cobertura de testes, e gera os JARs principal, de fontes e de Javadoc.

## Licença

Licença MIT (MIT). Veja o [arquivo de licença](LICENSE) para mais informações.

## Suporte

Para suporte, entre em contato com [support@tronzap.com](mailto:support@tronzap.com).
