package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.SubjectSummary;
import com.tuestudio.tutor.application.usecase.*;
import com.tuestudio.tutor.domain.AssignedSubjectId;
import com.tuestudio.tutor.domain.Tutor;
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
    private final SubjectLookupPort subjectLookup;

    public TutorController(SearchTutorsUseCase searchTutors, GetTutorUseCase getTutor,
                           RequestContactUseCase requestContact,
                           SubjectLookupPort subjectLookup) {
        this.searchTutors = searchTutors;
        this.getTutor = getTutor;
        this.requestContact = requestContact;
        this.subjectLookup = subjectLookup;
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
        Tutor tutor = getTutor.getById(TutorId.of(id));
        List<SubjectSummary> summaries = enrichSubjects(tutor);
        return TutorProfileResponse.from(tutor, summaries);
    }

    @PostMapping("/{id}/contact")
    public ResponseEntity<Void> contact(@PathVariable UUID id,
                                        @Valid @RequestBody ContactRequestBody body) {
        requestContact.request(new ContactRequestCommand(TutorId.of(id), body.nombre(), body.telefono(),
                body.universidad(), body.carrera(), body.materia()));
        return ResponseEntity.ok().build();
    }

    private List<SubjectSummary> enrichSubjects(Tutor tutor) {
        if (tutor.assignedSubjectIds() == null || tutor.assignedSubjectIds().isEmpty()) {
            return List.of();
        }
        List<UUID> ids = tutor.assignedSubjectIds().stream()
                .map(AssignedSubjectId::value)
                .toList();
        return subjectLookup.findByIds(ids);
    }
}
