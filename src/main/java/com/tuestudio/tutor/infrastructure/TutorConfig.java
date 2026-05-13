package com.tuestudio.tutor.infrastructure;

import com.tuestudio.tutor.application.port.ContactRequestRepositoryPort;
import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TutorConfig {

    @Bean
    public SearchTutorsUseCase searchTutorsUseCase(TutorRepositoryPort tutorRepository,
                                                     SubjectLookupPort subjectLookupPort) {
        return new SearchTutorsService(tutorRepository, subjectLookupPort);
    }

    @Bean
    public GetTutorUseCase getTutorUseCase(TutorRepositoryPort tutorRepository) {
        return new GetTutorService(tutorRepository);
    }

    @Bean
    public CreateTutorProfileUseCase createTutorProfileUseCase(TutorRepositoryPort tutorRepository) {
        return new CreateTutorProfileService(tutorRepository);
    }

    @Bean
    public GetTutorProfileUseCase getTutorProfileUseCase(TutorRepositoryPort tutorRepository) {
        return new GetTutorProfileService(tutorRepository);
    }

    @Bean
    public UpdateTutorProfileUseCase updateTutorProfileUseCase(TutorRepositoryPort tutorRepository) {
        return new UpdateTutorProfileService(tutorRepository);
    }

    @Bean
    public RequestContactUseCase requestContactUseCase(TutorRepositoryPort tutorRepository,
                                                        ContactRequestRepositoryPort contactRepository) {
        return new RequestContactService(tutorRepository, contactRepository);
    }

    @Bean
    public GetTeacherRequestsUseCase getTeacherRequestsUseCase(ContactRequestRepositoryPort contactRepository) {
        return new GetTeacherRequestsService(contactRepository);
    }

    @Bean
    public AttendRequestUseCase attendRequestUseCase(ContactRequestRepositoryPort contactRepository) {
        return new AttendRequestService(contactRepository);
    }
}
