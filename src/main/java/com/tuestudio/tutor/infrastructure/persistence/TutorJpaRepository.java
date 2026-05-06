package com.tuestudio.tutor.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.UUID;

interface TutorJpaRepository extends JpaRepository<TutorJpaEntity, UUID>, JpaSpecificationExecutor<TutorJpaEntity> {}
