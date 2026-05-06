package com.tuestudio.tutor.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

interface ContactRequestJpaRepository extends JpaRepository<ContactRequestJpaEntity, UUID> {}
