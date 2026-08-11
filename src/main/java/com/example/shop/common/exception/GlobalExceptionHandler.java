package com.example.shop.common.exception;
import com.example.shop.order.UnitUnavailableException;
import com.example.shop.order.PaymentFailedException;
import com.example.shop.user.EmailAlreadyUsedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> body(HttpStatus status,String message){
        Map<String,Object> m=new LinkedHashMap<>();

        m.put("timestamp",OffsetDateTime.now().toString());
        m.put("status",status.value());
        m.put("error",status.getReasonPhrase());
        m.put("message",message);

        return m;
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Map<String,Object>> handleEmailUsed(EmailAlreadyUsedException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT,"That email is already registered."));
    }


    @ExceptionHandler(UnitUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleUnitUnavailable(UnitUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentFailed(PaymentFailedException ex) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(body(HttpStatus.PAYMENT_REQUIRED, ex.getMessage()));
    }
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String,Object>> handleBadCredentials(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(HttpStatus.UNAUTHORIZED,"Email or password is incorrect."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex){
        Map<String,Object> resp = body(HttpStatus.BAD_REQUEST,"Some fields are invalid.");
        Map<String,String> fields = new LinkedHashMap<>();

        for(FieldError fe : ex.getBindingResult().getFieldErrors()){
            fields.put(fe.getField(),fe.getDefaultMessage());
        }

        resp.put("fields",fields);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleUnexpected(Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body(HttpStatus.INTERNAL_SERVER_ERROR,"Something went wrong"));
    }
}
