package commerce.eshop.core.web.errorHandler;

import commerce.eshop.core.service.AuditingService;
import commerce.eshop.core.util.enums.AuditingStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalApiErrorHandler {

    // == Fields ==
    private final AuditingService auditingService;

    // == Constructors ==
    public GlobalApiErrorHandler(AuditingService auditingService){
        this.auditingService = auditingService;
    }

    // == Public Methods ==
    /** Pass-through for ResponseStatusException (keeps your reason & status) */
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handle(ResponseStatusException ex, HttpServletRequest req){
        var problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
        problemDetail.setTitle(defaultTitle((HttpStatus) ex.getStatusCode()));
        enrich(problemDetail, req);
        audit(req, AuditingStatus.WARNING, ex.getStatusCode().value() + ":" + ex.getReason());
        return problemDetail;
    }

    /** Validation: @Valid on request bodies (bean validation) -> 422 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handle(MethodArgumentNotValidException ex, HttpServletRequest req){
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED");
        problemDetail.setTitle("Validation failed");
        var errors = ex.getBindingResult().getFieldErrors().stream().map(
                fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage(),
                        "rejected", fe.getRejectedValue())).toList();
        problemDetail.setProperty("errors", errors);
        enrich(problemDetail, req);
        audit(req, AuditingStatus.WARNING, "VALIDATION_FAILED" + summarize(errors));
        return problemDetail;
    }

    /** Validation: @RequestParam/@PathVariable constraints -> 422 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handle(ConstraintViolationException ex, HttpServletRequest req){
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY,"CONSTRAINT_VIOLATION");
        problemDetail.setTitle("Validation failed");
        var errors = ex.getConstraintViolations().stream().map(
                v -> Map.of("field", v.getPropertyPath().toString(), "message", v.getMessage())).toList();
        problemDetail.setProperty("errors", errors);
        enrich(problemDetail, req);
        audit(req, AuditingStatus.WARNING, "CONSTRAINT_VIOLATION");
        return problemDetail;
    }

    /** Bad JSON / wrong shape -> 400 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handle(HttpMessageNotReadableException ex, HttpServletRequest req){
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "MALFORMED_JSON");
        problemDetail.setTitle("Bad request");
        enrich(problemDetail, req);
        audit(req, AuditingStatus.WARNING, "MALFORMED_JSON");
        return problemDetail;
    }

    /** Wrong param type or missing required param -> 400 */
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            TypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest req){
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "BAD_REQUEST");
        problemDetail.setTitle("Bad request");
        enrich(problemDetail, req);
        audit(req, AuditingStatus.WARNING, "BAD_REQUEST" + safeMsg(ex));
        return problemDetail;
    }

    /** Not found (when you throw NoSuchElementException) -> 404 */
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handle(NoSuchElementException ex, HttpServletRequest req){
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Not found");
        enrich(problemDetail, req);
        audit(req, AuditingStatus.WARNING, "NOT_FOUND");
        return problemDetail;
    }

    /** Optimistic locking conflicts -> 409 */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handle(OptimisticLockingFailureException ex, HttpServletRequest req){
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "CONCURRENT_UPDATE");
        problemDetail.setTitle("Conflict");
        enrich(problemDetail, req);
        audit(req, AuditingStatus.WARNING, "CONCURRENT_UPDATE");
        return problemDetail;
    }

    /** DB unique/foreign key violations -> 409 (return a short reason) */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handle(DataIntegrityViolationException ex, HttpServletRequest req){
        String reason = "CONSTRAINT_VIOLATION";
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, reason);
        problemDetail.setTitle("Conflict");
        enrich(problemDetail, req);
        audit(req, AuditingStatus.WARNING, reason);
        return problemDetail;
    }

    /** Fallback -> 500 */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handle(Throwable ex, HttpServletRequest req){
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
        problemDetail.setTitle("Internal Server Error");
        enrich(problemDetail, req);
        audit(req, AuditingStatus.ERROR, "INTERNAL_ERROR");
        return problemDetail;
    }

    // == Private Methods ==
    private void enrich(ProblemDetail pd, HttpServletRequest req) {
        pd.setProperty("timestamp", OffsetDateTime.now());
        pd.setProperty("path", req.getRequestURI());
        // If you have an MDC trace id, add it here
        // pd.setProperty("traceId", MDC.get("traceId"));
    }

    private void audit(HttpServletRequest req, AuditingStatus status, String reason) {
        if (auditingService == null) return;
        UUID cid = extractDemoCustomerId(req); // optional; returns null if absent/bad
        auditingService.log(cid, "GLOBAL_ERROR_HANDLER", status, reason);
    }

    private UUID extractDemoCustomerId(HttpServletRequest req) {
        try {
            String v = req.getHeader("X-Demo-UserId");
            return (v == null || v.isBlank()) ? null : UUID.fromString(v.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String defaultTitle(HttpStatus status) {
        return switch (status.value()) {
            case 400 -> "Bad request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not found";
            case 409 -> "Conflict";
            case 422 -> "Unprocessable entity";
            default -> status.getReasonPhrase();
        };
    }

    private static String summarize(java.util.List<Map<String, Object>> errors) {
        return errors.stream()
                .limit(3)
                .map(m -> m.get("field") + ":" + m.get("message"))
                .collect(Collectors.joining(","));
    }

    private static String safeMsg(Exception ex) {
        var m = ex.getMessage();
        return m == null ? ex.getClass().getSimpleName() : m;
    }
}