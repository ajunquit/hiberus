# Mapping OpenAPI a dominio

Fecha de consolidación: 2026-03-23

## Propósito

Documentar cómo se trasladó el contrato REST al modelo de dominio y a la semántica del ejercicio.

## Request de alta

`PaymentOrderInitiationRequest` se transforma a `InitiatePaymentOrderCommand` con el siguiente mapping:

- `externalReference` -> `externalReference`
- `debtorAccount.iban` -> `debtorIban`
- `creditorAccount.iban` -> `creditorIban`
- `instructedAmount.amount` -> `amount`
- `instructedAmount.currency` -> `currency`
- `remittanceInformation` -> `remittanceInformation`
- `requestedExecutionDate` -> `requestedExecutionDate`

## Dominio

El agregado `PaymentOrder` encapsula:

- identificador interno de la orden
- referencia externa
- cuenta deudora
- cuenta acreedora
- importe y moneda
- remesa
- fecha solicitada de ejecución
- estado
- marcas temporales de creación y actualización

## Response REST

`PaymentOrder` REST se construye desde el agregado de dominio conservando:

- `paymentOrderId`
- `externalReference`
- cuentas anidadas
- `instructedAmount`
- `remittanceInformation`
- `requestedExecutionDate`
- `status`
- `createdAt`
- `lastUpdate`

## Vista de estado

`PaymentOrderStatusView` se alimenta desde `PaymentOrderStatusResult` y expone:

- `paymentOrderId`
- `status`
- `lastUpdate`

## Divergencias controladas respecto al legado

- El WSDL legado no ofrece un `retrieve` completo de la orden; por eso el microservicio mantiene su propio estado.
- El alta inicial persiste el estado `ACCEPTED`.
- `SETTLED` y `REJECTED` quedan soportados en el contrato y el dominio para evolución posterior.
