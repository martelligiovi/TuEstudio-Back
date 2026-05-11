package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.auth.domain.User;
import com.tuestudio.tutor.application.usecase.AttendRequestUseCase;
import com.tuestudio.tutor.application.usecase.GetTeacherRequestsUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final GetTeacherRequestsUseCase getRequests;
    private final AttendRequestUseCase attendRequest;

    public TeacherController(GetTeacherRequestsUseCase getRequests, AttendRequestUseCase attendRequest) {
        this.getRequests = getRequests;
        this.attendRequest = attendRequest;
    }

    @GetMapping("/requests")
    public List<ContactRequestResponse> getMyRequests(@AuthenticationPrincipal User user) {
        return getRequests.getByTutorId(user.id())
                .stream().map(ContactRequestResponse::from).toList();
    }

    @PatchMapping("/requests/{id}/attend")
    public ContactRequestResponse attend(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ContactRequestResponse.from(attendRequest.attend(id, user.id()));
    }
}
