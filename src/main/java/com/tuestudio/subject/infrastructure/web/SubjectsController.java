package com.tuestudio.subject.infrastructure.web;

import com.tuestudio.subject.application.usecase.SearchSubjectsByQueryUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectsController {

    private final SearchSubjectsByQueryUseCase searchSubjects;

    public SubjectsController(SearchSubjectsByQueryUseCase searchSubjects) {
        this.searchSubjects = searchSubjects;
    }

    @GetMapping
    public List<PublicSubjectResponse> search(@RequestParam(name = "q", required = false) String q) {
        return searchSubjects.search(q).stream()
                .map(s -> new PublicSubjectResponse(s.id().value(), s.canonicalName()))
                .toList();
    }
}
