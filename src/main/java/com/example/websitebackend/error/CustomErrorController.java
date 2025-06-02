package com.example.websitebackend.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<?> handleError(HttpServletRequest request) {
        var statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        var originalUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        var exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int status = statusObj != null ?
                Integer.parseInt(statusObj.toString()) : HttpStatus.INTERNAL_SERVER_ERROR.value();
        var accept = request.getHeader("Accept");

        if (accept != null && (accept.contains("application/json") || accept.equals("*/*"))) {
            Map<String, Object> body = Map.of(
                    "timestamp", java.time.Instant.now().toString(),
                    "status", status,
                    "error", HttpStatus.valueOf(status).getReasonPhrase(),
                    "message", exception != null ? exception.getLocalizedMessage() : "No message available",
                    "path", originalUri
            );
            return ResponseEntity.status(status).body(body);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/")
                .build();
    }

}
