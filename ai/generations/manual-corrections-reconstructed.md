# Correcciones Manuales Reconstruidas

Fecha de consolidacion: 2026-03-23

## Alcance

Este documento no es un log historico literal. Es una reconstruccion razonada de correcciones manuales probables, inferidas a partir del estado final del repositorio y de las decisiones tecnicas registradas.

## Correcciones manuales plausibles

### 1. Refinamiento del contrato OpenAPI

Correccion manual:

- Endurecer validaciones y restricciones clave del contrato (`IBAN`, `currency`, `paymentOrderId`, obligatorios).

Motivo:

- La generacion inicial suele quedarse corta en reglas de formato y semantica.
- La version final debia reflejar invariantes reales del dominio.

### 2. Aterrizaje del dominio hexagonal

Correccion manual:

- Consolidar el dominio con value objects (`AccountNumber`, `MonetaryAmount`) y un agregado `PaymentOrder` con invariantes.

Motivo:

- La prueba exige un dominio limpio y mapeo claro request/response <-> dominio.
- Esto evita reglas de negocio dispersas en controladores y mappers.

### 3. Manejo de errores alineado con una API seria

Correccion manual:

- Introducir `application/problem+json` y un manejo centralizado de errores de dominio y aplicacion.

Motivo:

- Una implementacion centrada solo en endpoints suele dejar respuestas heterogeneas.
- Se necesitaba consistencia HTTP para validaciones y errores de negocio.

### 4. Sustitucion del cliente de pruebas E2E

Correccion manual:

- Reemplazar `TestRestTemplate` por `RestAssured` en la prueba de integracion.

Motivo:

- El PDF pide explicitamente `WebTestClient` o `RestAssured`.
- El cambio mantiene cobertura E2E y alinea la implementacion con el entregable esperado.

### 5. Estabilizacion de fuentes generadas para VS Code

Correccion manual:

- Mover la salida de OpenAPI Generator a una carpeta estable `.generated/openapi`, versionar la configuracion minima de VS Code y ajustar exclusiones de Docker y Checkstyle para ese arbol generado.

Motivo:

- En VS Code era posible ver imports en rojo sobre `com.hiberus.candidateapi.generated.*` aunque Maven compilara correctamente.
- El problema no estaba en el codigo funcional, sino en la resolucion del IDE sobre fuentes generadas dentro de `target/`.
- Dejar una ruta estable fuera de `target/` reduce falsos positivos y hace mas predecible la sincronizacion del proyecto tras abrirlo en frio.

## Validacion humana esperada

El valor de estas correcciones esta en que muestran intervencion humana sobre:

- decisiones de arquitectura
- restricciones del contrato
- coherencia entre dominio y API
- adecuacion del stack de pruebas al PDF
- experiencia real de trabajo en VS Code
