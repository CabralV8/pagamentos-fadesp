package com.fadesp.pagamento.infrastructure.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ==== EXCEÇÕES DE NEGÓCIO / DOMÍNIO ====

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Problem> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Problem> handleConflict(ConflictException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Conflito de dados", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Problem> handleBusiness(BusinessException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Regra de negócio violada", ex.getMessage(), req.getRequestURI());
    }

    // ==== VALIDAÇÃO (Bean Validation) ====

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                HttpServletRequest req) {
        Map<String, List<String>> errorsByField = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        Problem problem = Problem.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Dados inválidos")
                .message("Um ou mais campos estão inválidos.")
                .path(req.getRequestURI())
                .validationErrors(errorsByField)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Problem> handleConstraintViolation(ConstraintViolationException ex,
                                                             HttpServletRequest req) {
        Map<String, List<String>> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.groupingBy(
                        v -> v.getPropertyPath().toString(),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));

        Problem problem = Problem.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Parâmetros inválidos")
                .message("Um ou mais parâmetros da requisição estão inválidos.")
                .path(req.getRequestURI())
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(problem);
    }

    // ==== ERROS DE DESERIALIZAÇÃO / TIPO ====

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Problem> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                HttpServletRequest req) {
        String detail = "Corpo da requisição inválido ou malformado.";

        // Mensagem amigável quando for enum inválido (ex.: StatusPagamentoEnum)
        if (ex.getCause() instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
            Object[] accepted = ife.getTargetType().getEnumConstants();
            detail = "Valor inválido: '" + ife.getValue()
                    + "'. Aceitos: " + List.of(accepted);
        }

        return build(HttpStatus.BAD_REQUEST, "Formato inválido", detail, req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Problem> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                      HttpServletRequest req) {
        String detail = "Parâmetro '" + ex.getName() + "' com tipo inválido. Valor: '" + ex.getValue() + "'.";
        return build(HttpStatus.BAD_REQUEST, "Tipo de parâmetro inválido", detail, req.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Problem> handleMissingParam(MissingServletRequestParameterException ex,
                                                      HttpServletRequest req) {
        String detail = "Parâmetro obrigatório ausente: '" + ex.getParameterName() + "'.";
        return build(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente", detail, req.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Problem> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método não suportado", ex.getMessage(), req.getRequestURI());
    }

    // ==== DADOS / BANCO ====

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Problem> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String detail = "Violação de integridade de dados. " +
                "Verifique unicidade/relacionamentos ou formato dos campos.";
        return build(HttpStatus.CONFLICT, "Integridade de dados", detail, req.getRequestURI());
    }

    // ==== FALLBACK ====

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", req.getRequestURI());
    }

    // ==== Helper ====

    private ResponseEntity<Problem> build(HttpStatus status, String error, String message, String path) {
        Problem body = Problem.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
