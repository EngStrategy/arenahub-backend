package com.engstrategy.alugai_api.exceptions;

import com.engstrategy.alugai_api.exceptions.response.ErrorResponse;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_CREDENTIALS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("USER_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_TOKEN", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UniqueConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleUniqueConstraintViolation(UniqueConstraintViolationException ex) {
        ErrorResponse error = new ErrorResponse("UNIQUE_CONSTRAINT_VIOLATION", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EmailUnconfirmedException.class)
    public ResponseEntity<ErrorResponse> handleEmailUnconfirmedException(EmailUnconfirmedException ex) {
        ErrorResponse error = new ErrorResponse("EMAIL_UNCONFIRMED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidConfirmationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidConfirmationCodeException(InvalidConfirmationCodeException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_CONFIRMATION_CODE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResendCodeLimitException.class)
    public ResponseEntity<ErrorResponse> handleResendCodeLimitException(ResendCodeLimitException ex) {
        ErrorResponse error = new ErrorResponse("RESEND_CODE_LIMIT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ExpiredConfirmationCodeException.class)
    public ResponseEntity<ErrorResponse> handleExpiredConfirmationCodeException(ExpiredConfirmationCodeException ex) {
        ErrorResponse error = new ErrorResponse("EXPIRED_CONFIRMATION_CODE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AlreadyConfirmedEmailException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyConfirmedEmailException(AlreadyConfirmedEmailException ex) {
        ErrorResponse error = new ErrorResponse("ALREADY_CONFIRMED_EMAIL", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidCooldownResendConfirmationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCooldownResendConfirmationCodeException(InvalidCooldownResendConfirmationCodeException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_COOLDOWN_RESEND_CONFIRMATION_CODE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DuplicateHorarioFuncionamentoException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateHorarioFuncionamento(DuplicateHorarioFuncionamentoException ex) {
        ErrorResponse error = new ErrorResponse("DUPLICATE_HORARIO_FUNCIONAMENTO", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidIntervaloHorarioException.class)
    public ResponseEntity<ErrorResponse> handleInvalidIntervaloHorario(InvalidIntervaloHorarioException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_INTERVALO_HORARIO", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse("ACCESS_DENIED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_ARGUMENT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("ENTITY_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnavailableDateTimeException.class)
    public ResponseEntity<ErrorResponse> handleUnavailableDateTime(UnavailableDateTimeException ex) {
        ErrorResponse error = new ErrorResponse("UNAVAILABLE_DATE_TIME", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ErrorResponse> handleMessagingException(MessagingException ex) {
        ErrorResponse error = new ErrorResponse("Messaging Exception", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(DateTimeParseException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_DATETIME", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IntervaloComAgendamentosException.class)
    public ResponseEntity<ErrorResponse> handleIntervaloComAgendamentosException(IntervaloComAgendamentosException ex) {
        ErrorResponse error = new ErrorResponse("INTERVALO_COM_AGENDA", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IntervaloNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleIntervaloNaoEncontradoException(IntervaloNaoEncontradoException ex) {
        ErrorResponse error = new ErrorResponse("INTERVALO_NAO_ENCONTRADO", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_STATE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Tratamento genérico para validações do @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ExternalAccountExistsException.class)
    public ResponseEntity<ErrorResponse> handleExternalAccountExists(ExternalAccountExistsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "EXTERNAL_ACCOUNT_EXISTS",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SubscriptionInactiveException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionInactive(SubscriptionInactiveException ex) {
        ErrorResponse error = new ErrorResponse("SUBSCRIPTION_INACTIVE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(LimiteDeQuadrasExcedidoException.class)
    public ResponseEntity<ErrorResponse> handleLimiteDeQuadrasExcedido(LimiteDeQuadrasExcedidoException ex) {
        ErrorResponse error = new ErrorResponse("LIMIT_EXCEEDED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
