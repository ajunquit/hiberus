# Candidate API

Microservicio REST para `Payment Initiation / PaymentOrder`, construido con Spring Boot 3, Java 17 y enfoque contract-first a partir de OpenAPI.

## Objetivo

El servicio expone una API REST que modela el alta y la consulta de órdenes de pago:

- `POST /payment-initiation/payment-orders`
- `GET /payment-initiation/payment-orders/{paymentOrderId}`
- `GET /payment-initiation/payment-orders/{paymentOrderId}/status`

El contrato fuente está en [openapi/payment-initiation-api.yaml](openapi/payment-initiation-api.yaml).

## Material base del ejercicio

Los artefactos originales y el análisis inicial que sirvieron como punto de partida quedaron versionados en `technical-test-baseline/`.

Ese directorio incluye:

- el PDF original de la prueba
- la colección Postman
- el WSDL legado
- las muestras XML
- la extracción a texto usada durante el análisis inicial

## Stack técnico

- Java 17
- Spring Boot 3.5.12
- Maven
- OpenAPI Generator
- Arquitectura hexagonal
- JaCoCo
- Checkstyle
- SpotBugs
- Docker / Docker Compose

## Arquitectura

La solución está organizada en capas para separar dominio, casos de uso e infraestructura:

- `domain`: reglas de negocio y modelo `PaymentOrder`
- `application`: casos de uso, comandos, resultados y puertos
- `adapter.in`: API REST, mapeo OpenAPI y manejo de errores
- `adapter.out`: persistencia en memoria y generador de identificadores
- `config`: beans técnicos y metadatos de Actuator

## Decisiones principales

- Puerto de ejecución: `8075`
- Persistencia inicial: en memoria
- Contrato REST: generado desde OpenAPI
- Capacidad adicional respecto al WSDL legado: `GET /payment-orders/{id}` se resuelve con almacenamiento propio

La trazabilidad de decisiones asistidas por IA está en [ai/decisions.md](ai/decisions.md).

## Ejecución local

### Requisitos

- JDK 17+
- Maven 3.9+ o wrapper Maven

### Arranque

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación queda disponible en `http://localhost:8075`.

### Verificación completa

```bash
./mvnw verify
```

Esto ejecuta:

- tests unitarios e integración
- generación OpenAPI
- JaCoCo con cobertura mínima del 80%
- Checkstyle
- SpotBugs

## OpenAPI y código generado

Las interfaces y modelos bajo `com.hiberus.candidateapi.generated.*` no están en `src/main/java`. Se generan desde [openapi/payment-initiation-api.yaml](openapi/payment-initiation-api.yaml) durante el build Maven y se escriben en `.generated/openapi/src/main/java`.

Se eligio una carpeta estable fuera de `target/` para que VS Code no pierda esas clases generadas tras limpiezas del proyecto o refrescos del workspace.

Esto implica que un IDE puede marcar imports en rojo si el proyecto se abre antes de generar fuentes por primera vez, aunque el código compile correctamente con Maven.

### Regenerar fuentes

```bash
./mvnw generate-sources
```

En Windows:

```powershell
.\mvnw.cmd generate-sources
```

### Si VS Code marca falsos errores

1. Ejecuta `generate-sources` o `compile`.
2. Espera a que VS Code recargue la configuración Maven de forma automática.
3. Ejecuta `Java: Clean Java Language Server Workspace`.
4. Verifica que exista `.generated/openapi/src/main/java`.

## Ejecución con Docker

### Construir imagen

```bash
docker build -t candidateapi:local .
```

### Ejecutar con Docker Compose

```bash
docker compose up --build
```

El contenedor expone el puerto `8075` y publica `actuator/health`, `actuator/info` y `actuator/metrics`.

## Ejemplos de uso

### Crear una orden

```bash
curl --request POST "http://localhost:8075/payment-initiation/payment-orders" \
  --header "Content-Type: application/json" \
  --data '{
    "externalReference": "EXT-1001",
    "debtorAccount": { "iban": "EC12DEBTOR" },
    "creditorAccount": { "iban": "EC98CREDITOR" },
    "instructedAmount": { "amount": 150.75, "currency": "USD" },
    "remittanceInformation": "Invoice 1001",
    "requestedExecutionDate": "2026-03-30"
  }'
```

### Recuperar una orden

```bash
curl "http://localhost:8075/payment-initiation/payment-orders/PO-0001"
```

### Consultar estado

```bash
curl "http://localhost:8075/payment-initiation/payment-orders/PO-0001/status"
```

## Calidad y pruebas

- `mvn verify` pasa en local
- Cobertura JaCoCo: superior al 80%
- Checkstyle: sin errores
- SpotBugs: sin hallazgos
- Integracion E2E implementada con RestAssured

Los reportes se generan en `target/site/jacoco` y `target/spotbugsXml.xml`.

## Limitaciones conocidas

- La persistencia es en memoria; los datos no sobreviven a un reinicio.
- El WSDL legado se usó como insumo de análisis, no como dependencia de ejecución.
- La construcción efectiva de la imagen Docker requiere un daemon Docker disponible.

## Evidencia de IA

La carpeta [ai/](ai/) documenta:

- decisiones de diseño
- prompts de trabajo y resumen de respuestas
- resúmenes de generación y refinamiento
- mapping entre contrato y dominio
- correcciones manuales reconstruidas e inferidas

Documentos principales:

- [ai/prompts.md](ai/prompts.md)
- [ai/decisions.md](ai/decisions.md)
- [ai/generations/implementation-summary.md](ai/generations/implementation-summary.md)
- [ai/generations/openapi-to-domain-mapping.md](ai/generations/openapi-to-domain-mapping.md)
- [ai/generations/manual-corrections-reconstructed.md](ai/generations/manual-corrections-reconstructed.md)
