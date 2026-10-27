package com.debarag.stockpulse.common;

import com.debarag.stockpulse.stock.DuplicateStockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.debarag.stockpulse.stock.StockNotFoundException;
import com.debarag.stockpulse.user.DuplicateWatchlistException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Your existing exception handler methods remain here

    @ExceptionHandler(DuplicateStockException.class)
    public ProblemDetail handleDuplicateStock(
            DuplicateStockException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Duplicate stock");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(
            MethodArgumentNotValidException exception) {

        String details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                details
        );

        problem.setTitle("Validation failed");
        return problem;
    }


    @ExceptionHandler(StockNotFoundException.class)
    public ProblemDetail handleStockNotFound(
            StockNotFoundException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problem.setTitle("Stock not found");
        return problem;
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Invalid request parameter");
        return problem;
    }

    @ExceptionHandler(DuplicateWatchlistException.class)
    public ProblemDetail handleDuplicateWatchlist(
            DuplicateWatchlistException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Duplicate watchlist entry");
        return problem;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(
            ResponseStatusException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                exception.getStatusCode(),
                exception.getReason() != null
                        ? exception.getReason()
                        : "Request failed"
        );

        problem.setTitle("Request failed");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception) {
        logger.error("Unexpected application error", exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );

        problem.setTitle("Internal server error");
        return problem;
    }

}