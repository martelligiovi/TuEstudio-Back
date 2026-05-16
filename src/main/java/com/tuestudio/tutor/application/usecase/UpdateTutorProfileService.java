package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.AssignedSubjectId;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorNotFoundException;
import com.tuestudio.tutor.domain.UnknownSubjectIdsException;
import jakarta.transaction.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Full-replacement update of a Tutor profile.
 * Reads existing Tutor, validates submitted subject UUIDs against the catalog,
 * replaces all client-editable fields, calls recomputeActive() before save.
 *
 * @Transactional — read-then-save must be atomic.
 */
public class UpdateTutorProfileService implements UpdateTutorProfileUseCase {

    private final TutorRepositoryPort repository;
    private final SubjectLookupPort subjectLookup;

    public UpdateTutorProfileService(TutorRepositoryPort repository, SubjectLookupPort subjectLookup) {
        this.repository = repository;
        this.subjectLookup = subjectLookup;
    }

    @Override
    @Transactional
    public Tutor update(UpdateTutorProfileCommand c) {
        Tutor existing = repository.findById(c.id())
                .orElseThrow(() -> new TutorNotFoundException(c.id()));

        // Validate subject UUIDs: every submitted UUID must exist in the catalog
        Set<UUID> requested = new LinkedHashSet<>(c.assignedSubjectIds());
        Set<UUID> found = subjectLookup.findExistingIds(requested);

        if (found.size() != requested.size()) {
            Set<UUID> unknown = new LinkedHashSet<>(requested);
            unknown.removeAll(found);
            throw new UnknownSubjectIdsException(unknown);
        }

        // Map validated UUIDs to domain VOs, preserving request order
        List<AssignedSubjectId> assignedSubjectIds = c.assignedSubjectIds().stream()
                .map(AssignedSubjectId::of)
                .toList();

        Tutor updated = new Tutor(
                existing.id(),
                c.name(),
                c.subjectSpecialty(),
                c.university(),
                c.location(),
                c.modalidad(),
                existing.rating(),        // server-managed — preserved
                existing.reviewsCount(),  // server-managed — preserved
                c.bio(),
                c.photoUrl(),
                existing.active(),        // overwritten by recomputeActive() below
                c.hourlyRate(),
                assignedSubjectIds,
                c.methodology(),
                c.schedules(),
                c.schedulesNote(),
                c.plans(),
                c.phoneNumber()
        ).recomputeActive();

        repository.save(updated);
        return updated;
    }
}
