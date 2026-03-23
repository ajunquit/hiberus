# Decisiones Iniciales

Fecha: 2026-03-22

## 1. Puerto objetivo

Decisión: mantener el servicio en `8075`.

Motivo:
- El repositorio ya quedó configurado con `server.port=8075` por una solicitud explícita.
- Cambiar otra vez a `8080` no aporta valor técnico y solo introduce ruido.
- Los artefactos externos del ejercicio que apuntan a `8080` se ajustarán después para reflejar la configuración real del proyecto.

Impacto:
- `openapi.yaml`, `README`, `docker-compose.yml` y la colección Postman deberán usar `8075`.

## 2. Estrategia de persistencia inicial

Decisión: empezar con un adaptador de persistencia en memoria detrás de un puerto de repositorio.

Motivo:
- La prueba no exige una base de datos como requisito mandatorio.
- El valor principal del ejercicio está en el contrato OpenAPI, la arquitectura hexagonal, el modelado de dominio y la calidad del build.
- Un repositorio en memoria permite implementar `POST`, `GET` y `GET status` sin bloquear el avance por infraestructura adicional.
- La persistencia quedará abstraída por puerto para poder cambiarla luego por JPA, JDBC o R2DBC sin tocar el dominio.

Alcance de la primera iteración:
- Repositorio `PaymentOrderRepository`.
- Implementación en memoria con identificador generado y almacenamiento por `paymentOrderId`.
- Timestamps controlados con `Clock` inyectable para hacer la lógica testeable.

Consecuencia:
- Los datos no sobrevivirán a un reinicio de la aplicación en esta primera versión.
- Esta limitación se documentará en `README.md` como trade-off consciente.

## 3. Mapping funcional WSDL -> REST BIAN

### Operaciones

Legado SOAP:
- `SubmitPaymentOrder`
- `GetPaymentOrderStatus`

REST objetivo:
- `POST /payment-initiation/payment-orders`
- `GET /payment-initiation/payment-orders/{id}`
- `GET /payment-initiation/payment-orders/{id}/status`

Decisión:
- `POST` mapeará el alta de la orden.
- `GET /status` mapeará la consulta de estado legado.
- `GET /payment-orders/{id}` será una capacidad nueva del microservicio REST y se soportará con almacenamiento propio del agregado `PaymentOrder`, ya que el WSDL no expone esa operación.

### Mapping de campos

| WSDL legado | REST/BIAN propuesto |
| --- | --- |
| `externalId` | `externalReference` |
| `debtorIban` | `debtorAccount.iban` |
| `creditorIban` | `creditorAccount.iban` |
| `amount` + `currency` | `instructedAmount.amount` + `instructedAmount.currency` |
| `remittanceInfo` | `remittanceInformation` |
| `requestedExecutionDate` | `requestedExecutionDate` |
| `paymentOrderId` | `paymentOrderId` |
| `status` | `status` |
| `lastUpdate` | `lastUpdate` |

### Modelo de dominio inicial

Decisión: modelar un agregado `PaymentOrder` con, como mínimo, estos atributos:
- `paymentOrderId`
- `externalReference`
- `debtorIban`
- `creditorIban`
- `amount`
- `currency`
- `remittanceInformation`
- `requestedExecutionDate`
- `status`
- `createdAt`
- `lastUpdate`

### Estados iniciales

Decisión: definir un ciclo mínimo de estados compatible con el enunciado y las muestras:
- `ACCEPTED`
- `SETTLED`
- `REJECTED`

Regla inicial:
- Al crear una orden, el estado persistido será `ACCEPTED`.
- `SETTLED` quedará soportado en el modelo y el contrato para consultas de estado y evolución posterior.
- `REJECTED` se reservará para fallos de negocio explícitos si se introducen en iteraciones posteriores; los errores de validación HTTP no se modelarán como órdenes rechazadas.

## 4. Alineación BIAN para la siguiente fase

Decisión:
- El contrato OpenAPI se alineará al Service Domain `Payment Initiation` y al behavior qualifier `PaymentOrder`, usando nombres de recursos y payloads consistentes con esa semántica.
- El controlador REST implementará interfaces generadas desde OpenAPI; no se expondrán controladores manuales desacoplados del contrato.

## 5. Supuestos abiertos

Estos puntos quedan fijados como supuestos para no bloquear el paso 2:
- No se consumirá el SOAP legado en tiempo de ejecución; el WSDL se usa solo para análisis.
- La primera versión no incluirá base de datos externa.
- La primera versión priorizará claridad del dominio y cobertura de pruebas sobre features opcionales como WebFlux o R2DBC.
