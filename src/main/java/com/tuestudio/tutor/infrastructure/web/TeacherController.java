package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.auth.domain.User;
import com.tuestudio.tutor.application.usecase.AttendRequestUseCase;
import com.tuestudio.tutor.application.usecase.GetTeacherRequestsUseCase;
import com.tuestudio.tutor.application.usecase.GetTutorProfileUseCase;
import com.tuestudio.tutor.application.usecase.UpdateTutorProfileUseCase;
import com.tuestudio.tutor.domain.TutorId;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final GetTeacherRequestsUseCase getRequests;
    private final AttendRequestUseCase attendRequest;
    private final GetTutorProfileUseCase getProfile;
    private final UpdateTutorProfileUseCase updateProfile;

    public TeacherController(GetTeacherRequestsUseCase getRequests,
                             AttendRequestUseCase attendRequest,
                             GetTutorProfileUseCase getProfile,
                             UpdateTutorProfileUseCase updateProfile) {
        this.getRequests = getRequests;
        this.attendRequest = attendRequest;
        this.getProfile = getProfile;
        this.updateProfile = updateProfile;
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

    /**
     * GET /api/teacher/profile — returns the profile for the authenticated TEACHER.
     * tutorId == user.id() (same UUID shared between User and Tutor).
     */
    @GetMapping("/profile")
    public TutorProfileResponse getProfile(@AuthenticationPrincipal User user) {
        return TutorProfileResponse.from(getProfile.getById(TutorId.of(user.id())));
    }

    /**
     * PUT /api/teacher/profile — full-replacement update of the TEACHER's profile.
     * Returns HTTP 200 with the updated profile (including recomputed active flag).
     */
    @PutMapping("/profile")
    public TutorProfileResponse putProfile(@AuthenticationPrincipal User user,
                                           @Valid @RequestBody UpdateTutorProfileRequest body) {
        return TutorProfileResponse.from(
                updateProfile.update(body.toCommand(TutorId.of(user.id())))
        );
    }
}
