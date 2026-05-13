package com.tuestudio.subject.infrastructure.web;

import com.tuestudio.subject.application.usecase.*;
import com.tuestudio.subject.domain.SubjectId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/subjects")
public class AdminSubjectsController {

    private final CreateSubjectUseCase createSubject;
    private final RenameSubjectUseCase renameSubject;
    private final AddSubjectAliasUseCase addAlias;
    private final RemoveSubjectAliasUseCase removeAlias;
    private final ListSubjectsUseCase listSubjects;

    public AdminSubjectsController(CreateSubjectUseCase createSubject,
                                   RenameSubjectUseCase renameSubject,
                                   AddSubjectAliasUseCase addAlias,
                                   RemoveSubjectAliasUseCase removeAlias,
                                   ListSubjectsUseCase listSubjects) {
        this.createSubject = createSubject;
        this.renameSubject = renameSubject;
        this.addAlias = addAlias;
        this.removeAlias = removeAlias;
        this.listSubjects = listSubjects;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectResponse create(@Valid @RequestBody CreateSubjectRequest body) {
        return SubjectResponse.from(createSubject.create(body.canonicalName()));
    }

    @PatchMapping("/{id}")
    public SubjectResponse rename(@PathVariable UUID id, @Valid @RequestBody RenameSubjectRequest body) {
        return SubjectResponse.from(renameSubject.rename(SubjectId.of(id), body.canonicalName()));
    }

    @PostMapping("/{id}/aliases")
    public SubjectResponse addAlias(@PathVariable UUID id, @Valid @RequestBody AddAliasRequest body) {
        return SubjectResponse.from(addAlias.addAlias(SubjectId.of(id), body.alias()));
    }

    @DeleteMapping("/{id}/aliases/{alias}")
    public SubjectResponse removeAlias(@PathVariable UUID id, @PathVariable String alias) {
        return SubjectResponse.from(removeAlias.removeAlias(SubjectId.of(id), alias));
    }

    @GetMapping
    public List<SubjectResponse> list() {
        return listSubjects.list().stream().map(SubjectResponse::from).toList();
    }
}
