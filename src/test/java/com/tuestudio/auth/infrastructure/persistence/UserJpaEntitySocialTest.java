package com.tuestudio.auth.infrastructure.persistence;

import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserJpaEntitySocialTest {

    @Test
    void fromDomain_socialUser_mapsProviderAndNullPassword() {
        User social = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.STUDENT);

        UserJpaEntity entity = UserJpaEntity.fromDomain(social);

        assertThat(entity.getProvider()).isEqualTo("GOOGLE");
        assertThat(entity.getProviderUserId()).isEqualTo("g-123");
        assertThat(entity.getHashedPassword()).isNull();
    }

    @Test
    void fromDomain_localUser_mapsPasswordAndDefaultProvider() {
        User local = User.create("Ana", "ana@mail.com", new HashedPassword("hashed"), Role.STUDENT);

        UserJpaEntity entity = UserJpaEntity.fromDomain(local);

        assertThat(entity.getProvider()).isEqualTo("LOCAL");
        assertThat(entity.getProviderUserId()).isNull();
        assertThat(entity.getHashedPassword()).isEqualTo("hashed");
    }

    @Test
    void toDomain_socialEntity_returnsSocialUser() {
        User social = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.STUDENT);
        UserJpaEntity entity = UserJpaEntity.fromDomain(social);

        User domain = entity.toDomain();

        assertThat(domain.isSocial()).isTrue();
        assertThat(domain.provider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(domain.providerUserId()).isEqualTo("g-123");
        assertThat(domain.password()).isNull();
    }

    @Test
    void toDomain_localEntity_returnsLocalUser() {
        User local = User.create("Ana", "ana@mail.com", new HashedPassword("hashed"), Role.STUDENT);
        UserJpaEntity entity = UserJpaEntity.fromDomain(local);

        User domain = entity.toDomain();

        assertThat(domain.isSocial()).isFalse();
        assertThat(domain.provider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(domain.password()).isNotNull();
        assertThat(domain.password().value()).isEqualTo("hashed");
    }
}
