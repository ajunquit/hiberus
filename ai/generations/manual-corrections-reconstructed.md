# Correcciones Manuales Reconstruidas

Fecha de consolidacion: 2026-03-23

## Alcance

Este documento no es un log historico literal. Es una reconstruccion razonada de correcciones manuales probables, inferidas a partir del estado final del repositorio y de las decisiones tecnicas registradas.

## Correcciones manuales plausibles

### 1. Ajuste del mapping entre legado SOAP y recurso REST

Correccion manual:

- Separar `GET /payment-orders/{id}` de `GET /payment-orders/{id}/status`, aunque el WSDL legado solo expone una consulta de estado.

Motivo:

- El contrato REST exigido por la prueba pide recuperar la orden completa.
- Fue necesario decidir que el microservicio mantuviera estado propio para soportar esa capacidad.

### 2. Refinamiento del contrato OpenAPI

Correccion manual:

- Endurecer validaciones de IBAN, currency, `paymentOrderId` y campos obligatorios.

Motivo:

- Un borrador generado suele quedarse corto en restricciones de formato y semantica.
- La version final necesitaba reflejar invariantes de dominio y producir errores consistentes.

### 3. Aterrizaje del dominio hexagonal

Correccion manual:

- Reemplazar una posible estructura inicial demasiado anemica por value objects (`AccountNumber`, `MonetaryAmount`) y un agregado `PaymentOrder` con invariantes.

Motivo:

- La prueba exige un dominio limpio y mapeo claro request/response <-> dominio.
- Esa estructura reduce reglas dispersas en controladores o mappers.

### 4. Manejo de errores alineado con una API seria

Correccion manual:

- Introducir `application/problem+json`, `RestExceptionHandler` y errores de dominio/aplicacion.

Motivo:

- Una generacion inicial centrada solo en endpoints puede dejar respuestas de error heterogeneas.
- Se necesitaba consistencia HTTP y trazabilidad de fallos de validacion y negocio.

### 5. Exclusiones de calidad sobre codigo generado

Correccion manual:

- Excluir `generated/**` y la clase bootstrap de ciertas metricas y analisis.

Motivo:

- JaCoCo, Checkstyle y SpotBugs deben medir el codigo mantenido por el equipo, no el producido automaticamente por OpenAPI Generator.
- Sin este ajuste, las metricas se vuelven ruidosas y menos defendibles.

### 6. Ajuste del wrapper Maven en Windows

Correccion manual:

- Corregir el comportamiento del wrapper de Windows para evitar fallos al resolver rutas locales de Maven.

Motivo:

- El entorno real de ejecucion mostro problemas practicos que no estaban cubiertos por la configuracion inicial.
- Sin esta correccion, la reproducibilidad local quedaba comprometida.

### 7. Sustitucion del cliente de pruebas E2E

Correccion manual:

- Reemplazar `TestRestTemplate` por `RestAssured` en la prueba de integracion.

Motivo:

- El PDF pide explicitamente `WebTestClient` o `RestAssured`.
- El cambio mantiene cobertura E2E y alinea la implementacion con el entregable esperado.

### 8. Alineacion documental del puerto y artefactos base

Correccion manual:

- Actualizar documentacion, Docker y material base para reflejar el puerto real `8075`.

Motivo:

- Parte del material inicial del ejercicio apuntaba a `8080`.
- Era necesario dejar consistente el repositorio final con la configuracion efectiva del servicio.

## Validacion humana esperada

El valor de estas correcciones esta en que muestran intervencion humana sobre:

- decisiones de arquitectura
- restricciones del contrato
- coherencia entre dominio y API
- calidad del build
- adecuacion final a los criterios del PDF
