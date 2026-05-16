package com.tuestudio.subject.infrastructure;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SubjectConfig {

    @Bean
    public CreateSubjectUseCase createSubjectUseCase(SubjectRepositoryPort repo) {
        return new CreateSubjectService(repo);
    }

    @Bean
    public RenameSubjectUseCase renameSubjectUseCase(SubjectRepositoryPort repo) {
        return new RenameSubjectService(repo);
    }

    @Bean
    public AddSubjectAliasUseCase addSubjectAliasUseCase(SubjectRepositoryPort repo) {
        return new AddSubjectAliasService(repo);
    }

    @Bean
    public RemoveSubjectAliasUseCase removeSubjectAliasUseCase(SubjectRepositoryPort repo) {
        return new RemoveSubjectAliasService(repo);
    }

    @Bean
    public ListSubjectsUseCase listSubjectsUseCase(SubjectRepositoryPort repo) {
        return new ListSubjectsService(repo);
    }

    @Bean
    public SearchSubjectsByQueryUseCase searchSubjectsByQueryUseCase(SubjectRepositoryPort repo) {
        return new SearchSubjectsByQueryService(repo);
    }

    @Bean
    public ChangeSubjectIconUseCase changeSubjectIconUseCase(SubjectRepositoryPort repo) {
        return new ChangeSubjectIconService(repo);
    }
}
