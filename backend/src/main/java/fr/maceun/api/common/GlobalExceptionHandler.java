package fr.maceun.api.common;

import java.util.LinkedHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import fr.maceun.api.booking.BookingForwardException;
import fr.maceun.api.release.source.LinkException;
import fr.maceun.api.storage.InvalidImageException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> onValidation(MethodArgumentNotValidException ex) {
        var fields = new LinkedHashMap<String, String>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiError("VALIDATION", "Certains champs sont invalides.", fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> onUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ApiError.of("BAD_REQUEST", "Requête invalide."));
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> onNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(LinkException.class)
    ResponseEntity<ApiError> onLink(LinkException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError("INVALID_LINK", ex.getMessage(), java.util.Map.of("url", ex.getMessage())));
    }

    @ExceptionHandler(InvalidImageException.class)
    ResponseEntity<ApiError> onInvalidImage(InvalidImageException ex) {
        return ResponseEntity.badRequest().body(ApiError.of("INVALID_IMAGE", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> onTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(ApiError.of("TOO_LARGE", "Image trop lourde (10 Mo maximum)."));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> onBadCredentials(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of("BAD_CREDENTIALS", "Identifiant ou mot de passe incorrect."));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    ResponseEntity<ApiError> onRateLimit(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiError.of("RATE_LIMIT", "Trop de tentatives, réessayez dans quelques minutes."));
    }

    @ExceptionHandler(BookingForwardException.class)
    ResponseEntity<ApiError> onForward(BookingForwardException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiError.of("UPSTREAM", "L'envoi a échoué, réessayez ou appelez directement."));
    }
}
