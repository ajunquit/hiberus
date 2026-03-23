# Prompts utilizados

Fecha de consolidación: 2026-03-23

## Objetivo

Registrar los prompts o instrucciones de trabajo más relevantes utilizados para construir la solución y justificar las decisiones tomadas durante la implementación.

## Prompts principales

### 1. Análisis del enunciado

Prompt:

> Analiza al detalle el ZIP de la prueba técnica y extrae requisitos funcionales, técnicos y entregables.

Resultado:

- Identificación de endpoints requeridos
- Detección de la brecha entre el WSDL legado y el endpoint REST `GET /payment-orders/{id}`
- Lista de entregables obligatorios: OpenAPI, arquitectura hexagonal, tests, calidad, Docker y documentación de IA

### 2. Definición inicial de arquitectura

Prompt:

> Propón un plan de implementación incremental para cumplir la prueba técnica con Spring Boot 3, Java 17 y enfoque contract-first.

Resultado:

- Secuencia de trabajo por fases
- Priorización de `openapi.yaml`, generación de código, arquitectura hexagonal, pruebas y calidad

### 3. Contrato OpenAPI

Prompt:

> Diseña primero el contrato OpenAPI 3.0 para Payment Initiation / PaymentOrder, incluyendo validaciones, errores y los tres endpoints exigidos.

Resultado:

- Archivo `openapi/payment-initiation-api.yaml`
- Esquemas de request, recurso, estado y `application/problem+json`

### 4. Implementación hexagonal

Prompt:

> Implementa la estructura hexagonal mínima con dominio, casos de uso, puertos y adaptadores, sin acoplar la lógica de negocio a Spring.

Resultado:

- Modelo de dominio `PaymentOrder`
- Servicio de aplicación
- Repositorio en memoria
- Controlador REST apoyado en interfaces generadas

### 5. Estrategia de pruebas

Prompt:

> Añade tests unitarios y de integración que cubran dominio, mapeos, errores y comportamiento end-to-end del API.

Resultado:

- Tests de dominio
- Tests de servicio y adaptadores
- `PaymentOrdersApiIntegrationTest`

### 6. Calidad de build

Prompt:

> Configura JaCoCo, Checkstyle y SpotBugs, excluyendo código generado, y deja `mvn verify` en verde con cobertura mínima del 80%.

Resultado:

- Plugins de calidad en Maven
- Reglas y exclusiones en `config/`
- `mvn verify` exitoso

### 7. Entrega ejecutable

Prompt:

> Prepara Dockerfile multi-stage, docker-compose y documentación operativa coherente con el puerto 8075.

Resultado:

- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore`

## Observaciones

- Los prompts se refinaron iterativamente según iban apareciendo necesidades de validación, compatibilidad con OpenAPI Generator y requisitos de calidad.
- La IA se utilizó como asistente de análisis, diseño y generación incremental, no como sustituto de la verificación técnica.
