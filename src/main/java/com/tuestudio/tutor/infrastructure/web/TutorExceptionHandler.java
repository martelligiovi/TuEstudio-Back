package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.application.usecase.ProfilePhotoValidationException;
import com.tuestudio.tutor.domain.UnknownSubjectIdsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Exception handler for the tutor web layer.
 * Scoped to TeacherController and TutorController only.
 */
@RestControllerAdvice(assignableTypes = {TeacherController.class, TutorController.class})
public class TutorExceptionHandler {

    @ExceptionHandler(ProfilePhotoValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleProfilePhotoValidation(ProfilePhotoValidationException ex) {
        return Map.of(
                "error", ex.code(),
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler(UnknownSubjectIdsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> handleUnknownSubjectIds(UnknownSubjectIdsException ex) {
        return Map.of(
                "error", "unknown_subject_ids",
                "unknown", ex.unknown()
        );
    }
}
