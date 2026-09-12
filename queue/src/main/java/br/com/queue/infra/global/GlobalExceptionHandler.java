package br.com.queue.infra.global;

import br.com.queue.dtos.error.ErrorResponse;
import br.com.queue.infra.attendances.AttendanceInvalidStateException;
import br.com.queue.infra.attendances.AttendanceNotFoundException;
import br.com.queue.infra.customer.CustomerAlreadyExistsException;
import br.com.queue.infra.customer.CustomerNotFoundException;
import br.com.queue.infra.customer.CustomerValidationException;
import br.com.queue.infra.department.DepartmentNotFoundException;
import br.com.queue.infra.schedule.ScheduleDeleteException;
import br.com.queue.infra.schedule.ScheduleNotFoundException;
import br.com.queue.infra.serviceManagement.ServiceManagementAlreadyExistsException;
import br.com.queue.infra.serviceManagement.ServiceManagementNotFoundException;
import br.com.queue.infra.ticket.TicketNotFoundException;
import br.com.queue.infra.unit.UnitIsPresentException;
import br.com.queue.infra.unit.UnitNotFoundException;
import br.com.queue.infra.user.UserInactiveException;
import br.com.queue.infra.user.UserNotFoundException;
import br.com.queue.infra.user.UserPasswordInvalidException;
import br.com.queue.infra.user.UserUnitMismatchException;
import br.com.queue.infra.user.UserValidationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================================== EXCEPTIONS USER ==================================================
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.error("Usuário não encontrado: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Usuário não encontrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<ErrorResponse> handleUserValidation(UserValidationException ex) {
        log.error("Erro de validação de usuário: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação de usuário",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<ErrorResponse> handleUserInactive(UserInactiveException ex) {
        log.error("Usuário inativo: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Usuário inativo",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(UserPasswordInvalidException.class)
    public ResponseEntity<ErrorResponse> handleUserPasswordInvalid(UserPasswordInvalidException ex) {
        log.error("Senha inválida: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciais inválidas",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UserUnitMismatchException.class)
    public ResponseEntity<ErrorResponse> handleUserUnitMismatch(UserUnitMismatchException ex) {
        log.error("Usuário não pertence à unidade: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // =========================================== EXCEPTIONS UNIT ==================================================
    @ExceptionHandler(UnitNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnitNotFound(UnitNotFoundException ex) {
        log.error("Unidade não encontrada: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Unidade não encontrada",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UnitIsPresentException.class)
    public ResponseEntity<ErrorResponse> handleUnitIsPresent(UnitIsPresentException ex) {
        log.error("Unidade já possui vínculo: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflito com unidade",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // =========================================== EXCEPTIONS SERVICE MANAGEMENT ====================================
    @ExceptionHandler(ServiceManagementNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleServiceNotFound(ServiceManagementNotFoundException ex) {
        log.error("Serviço não encontrado: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Serviço não encontrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ServiceManagementAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleServiceAlreadyExists(ServiceManagementAlreadyExistsException ex) {
        log.error("Serviço já existe: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Serviço já cadastrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // =========================================== EXCEPTIONS CUSTOMER ==============================================
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex) {
        log.error("Cliente não encontrado: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Cliente não encontrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCustomerAlreadyExists(CustomerAlreadyExistsException ex) {
        log.error("Cliente já existe: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Cliente já cadastrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(CustomerValidationException.class)
    public ResponseEntity<ErrorResponse> handleCustomerValidation(CustomerValidationException ex) {
        log.error("Erro de validação de cliente: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação de cliente",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =========================================== EXCEPTIONS ATTENDANCE ============================================
    @ExceptionHandler(AttendanceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAttendanceNotFound(AttendanceNotFoundException ex) {
        log.error("Atendimento não encontrado: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Atendimento não encontrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AttendanceInvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleAttendanceInvalidState(AttendanceInvalidStateException ex) {
        log.error("Estado de atendimento inválido: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Estado de atendimento inválido",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =========================================== EXCEPTIONS SCHEDULE ==============================================
    @ExceptionHandler(ScheduleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleScheduleNotFound(ScheduleNotFoundException ex) {
        log.error("Agendamento não encontrado: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Agendamento não encontrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ScheduleDeleteException.class)
    public ResponseEntity<ErrorResponse> handleScheduleDelete(ScheduleDeleteException ex) {
        log.error("Erro ao deletar agendamento: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro ao deletar agendamento",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =========================================== EXCEPTIONS TICKET ================================================
    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTicketNotFound(TicketNotFoundException ex) {
        log.error("Ticket não encontrado: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Ticket não encontrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // =========================================== EXCEPTIONS DEPARTMENT ============================================
    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDepartmentNotFound(DepartmentNotFoundException ex) {
        log.error("Departamento não encontrado: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Departamento não encontrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // =========================================== VALIDATION EXCEPTIONS ============================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorMessage = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

        log.error("Erro de validação nos campos: {}", errorMessage);

        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação nos campos",
                errorMessage,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        log.error("Erro de validação de restrição: {}", errors);

        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação de restrição",
                errors,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =========================================== DATA INTEGRITY EXCEPTIONS =======================================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Erro de integridade de dados. Verifique se não há dados duplicados ou referências inválidas.";

        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry")) {
                message = "Já existe um registro com essas informações. Verifique os dados enviados.";
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Não é possível realizar esta operação pois existem registros relacionados.";
            }
        }

        log.error("Erro de integridade de dados: {}", ex.getMessage());

        var response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflito de dados",
                message,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // =========================================== SECURITY EXCEPTIONS =============================================
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.error("Falha na autenticação: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Não autorizado",
                "Falha na autenticação. Verifique suas credenciais.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Acesso negado: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado",
                "Você não tem permissão para acessar este recurso",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // =========================================== REQUEST EXCEPTIONS ==============================================
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        log.error("Parâmetro ausente: {}", ex.getParameterName());
        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro ausente",
                "O parâmetro '" + ex.getParameterName() + "' é obrigatório",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "O parâmetro '%s' deve ser do tipo %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "válido"
        );

        log.error("Tipo de parâmetro inválido: {}", message);

        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Tipo de parâmetro inválido",
                message,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Requisição mal formatada. Verifique o JSON enviado.";

        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Required request body is missing")) {
                message = "O corpo da requisição está vazio ou ausente.";
            } else if (ex.getMessage().contains("JSON parse error")) {
                message = "Erro ao processar JSON. Verifique a sintaxe do objeto enviado.";
            }
        }

        log.error("Erro de formatação da requisição: {}", ex.getMessage());

        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de formatação",
                message,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =========================================== GENERIC EXCEPTIONS ==============================================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Argumento inválido: {}", ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Argumento inválido",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro não tratado: ", ex);

        var response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}