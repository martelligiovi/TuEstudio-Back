package com.tuestudio.shared.web;

import com.tuestudio.auth.application.usecase.DuplicateEmailException;
import com.tuestudio.auth.application.usecase.InvalidCredentialsException;
import com.tuestudio.auth.application.usecase.InvalidRegistrationRoleException;
import com.tuestudio.tutor.domain.ContactRequestNotFoundException;
import com.tuestudio.tutor.domain.TutorNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(InvalidRegistrationRoleException.class)
    public ProblemDetail handleInvalidRegistrationRole(InvalidRegistrationRoleException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(ContactRequestNotFoundException.class)
    public ProblemDetail handleContactRequestNotFound(ContactRequestNotFoundException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(TutorNotFoundException.class)
    public ProblemDetail handleTutorNotFound(TutorNotFoundException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setDetail(ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("Validation error"));
        return problem;
    }
}
