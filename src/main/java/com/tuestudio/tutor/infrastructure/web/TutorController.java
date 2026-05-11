package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.application.usecase.*;
import com.tuestudio.tutor.domain.TutorId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tutors")
public class TutorController {

    private final SearchTutorsUseCase searchTutors;
    private final GetTutorUseCase getTutor;
    private final RequestContactUseCase requestContact;

    public TutorController(SearchTutorsUseCase searchTutors, GetTutorUseCase getTutor,
                           RequestContactUseCase requestContact) {
        this.searchTutors = searchTutors;
        this.getTutor = getTutor;
        this.requestContact = requestContact;
    }

    @GetMapping
    public List<TutorSummaryResponse> search(
            @RequestParam(required = false) String universidad,
            @RequestParam(required = false) String materia,
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        var criteria = new SearchCriteria(universidad, materia, carrera, minPrice, maxPrice);
        return searchTutors.search(criteria).stream().map(TutorSummaryResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TutorProfileResponse getById(@PathVariable UUID id) {
        return TutorProfileResponse.from(getTutor.getById(TutorId.of(id)));
    }

    @PostMapping("/{id}/contact")
    public ResponseEntity<Void> contact(@PathVariable UUID id,
                                        @Valid @RequestBody ContactRequestBody body) {
        requestContact.request(new ContactRequestCommand(TutorId.of(id), body.nombre(), body.telefono(),
                body.universidad(), body.carrera(), body.materia()));
        return ResponseEntity.ok().build();
    }
}
