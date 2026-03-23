package com.hiberus.candidateapi.adapter.in.rest.error;

import com.hiberus.candidateapi.application.exception.PaymentOrderNotFoundException;
import com.hiberus.candidateapi.domain.exception.DomainValidationException;
import com.hiberus.candidateapi.generated.model.InvalidParam;
import com.hiberus.candidateapi.generated.model.Problem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);
    private static final MediaType PROBLEM_JSON = MediaType.parseMediaType("application/problem+json");

    private final ApiProblemFactory apiProblemFactory;

    public RestExceptionHandler(ApiProblemFactory apiProblemFactory) {
        this.apiProblemFactory = apiProblemFactory;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        List<InvalidParam> invalidParams = toInvalidParams(exception.getBindingResult());
        Problem problem = apiProblemFactory.validationProblem(
            instanceFrom(request),
            "One or more fields contain invalid values.",
            invalidParams
        );
        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
        HandlerMethodValidationException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        List<InvalidParam> invalidParams = new ArrayList<>();
        for (ParameterValidationResult validationResult : exception.getParameterValidationResults()) {
            String parameterName = resolveParameterName(validationResult);
            if (validationResult instanceof ParameterErrors parameterErrors) {
                invalidParams.addAll(toInvalidParams(parameterErrors, parameterName));
                continue;
            }
            for (MessageSourceResolvable resolvable : validationResult.getResolvableErrors()) {
                invalidParams.add(
                    new InvalidParam()
                        .name(parameterName)
                        .reason(defaultMessage(resolvable, "contains an invalid value"))
                );
            }
        }

        Problem problem = apiProblemFactory.validationProblem(
            instanceFrom(request),
            "One or more request parameters contain invalid values.",
            invalidParams
        );
        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        Problem problem = apiProblemFactory.validationProblem(
            instanceFrom(request),
            "Request body is malformed or uses an unsupported format.",
            List.of()
        );
        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<Problem> handleDomainValidation(
        DomainValidationException exception,
        HttpServletRequest request
    ) {
        Problem problem = apiProblemFactory.validationProblem(
            request.getRequestURI(),
            exception.getMessage(),
            List.of()
        );
        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Problem> handleConstraintViolation(
        ConstraintViolationException exception,
        HttpServletRequest request
    ) {
        List<InvalidParam> invalidParams = exception
            .getConstraintViolations()
            .stream()
            .map(violation ->
                new InvalidParam()
                    .name(violation.getPropertyPath().toString())
                    .reason(violation.getMessage())
            )
            .toList();

        Problem problem = apiProblemFactory.validationProblem(
            request.getRequestURI(),
            "One or more request parameters contain invalid values.",
            invalidParams
        );
        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(PaymentOrderNotFoundException.class)
    public ResponseEntity<Problem> handlePaymentOrderNotFound(
        PaymentOrderNotFoundException exception,
        HttpServletRequest request
    ) {
        Problem problem = apiProblemFactory.notFoundProblem(request.getRequestURI(), exception.getMessage());
        return problemResponse(HttpStatus.NOT_FOUND, problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleUnexpectedException(
        Exception exception,
        HttpServletRequest request
    ) {
        LOGGER.error("Unhandled exception for request {}", request.getRequestURI(), exception);
        Problem problem = apiProblemFactory.internalServerErrorProblem(request.getRequestURI());
        return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, problem);
    }

    private <T> ResponseEntity<T> problemResponse(HttpStatus status, T problem) {
        return ResponseEntity.status(status).contentType(PROBLEM_JSON).body(problem);
    }

    private List<InvalidParam> toInvalidParams(BindingResult bindingResult) {
        List<InvalidParam> invalidParams = new ArrayList<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            invalidParams.add(
                new InvalidParam()
                    .name(fieldError.getField())
                    .reason(defaultMessage(fieldError, "contains an invalid value"))
            );
        }
        for (ObjectError globalError : bindingResult.getGlobalErrors()) {
            invalidParams.add(
                new InvalidParam()
                    .name(bindingResult.getObjectName())
                    .reason(defaultMessage(globalError, "contains an invalid value"))
            );
        }
        return invalidParams;
    }

    private List<InvalidParam> toInvalidParams(ParameterErrors parameterErrors, String prefix) {
        List<InvalidParam> invalidParams = new ArrayList<>();
        for (FieldError fieldError : parameterErrors.getFieldErrors()) {
            invalidParams.add(
                new InvalidParam()
                    .name(prefix + "." + fieldError.getField())
                    .reason(defaultMessage(fieldError, "contains an invalid value"))
            );
        }
        for (ObjectError globalError : parameterErrors.getGlobalErrors()) {
            invalidParams.add(
                new InvalidParam()
                    .name(prefix)
                    .reason(defaultMessage(globalError, "contains an invalid value"))
            );
        }
        return invalidParams;
    }

    private String defaultMessage(MessageSourceResolvable resolvable, String fallback) {
        return resolvable.getDefaultMessage() == null ? fallback : resolvable.getDefaultMessage();
    }

    private String resolveParameterName(ParameterValidationResult validationResult) {
        PathVariable pathVariable = validationResult.getMethodParameter().getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            if (!pathVariable.name().isBlank()) {
                return pathVariable.name();
            }
            if (!pathVariable.value().isBlank()) {
                return pathVariable.value();
            }
        }
        String parameterName = validationResult.getMethodParameter().getParameterName();
        if (parameterName != null && !parameterName.isBlank()) {
            return parameterName;
        }
        return "parameter[%d]".formatted(validationResult.getMethodParameter().getParameterIndex());
    }

    private String instanceFrom(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return "";
    }
}
