package com.tuestudio.auth.application.port;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

class TutorProvisioningPortContractTest {

    @Test
    void interface_isInAuthApplicationPortPackage() {
        assertThat(TutorProvisioningPort.class.getPackageName())
                .isEqualTo("com.tuestudio.auth.application.port");
    }

    @Test
    void interface_hasNoTutorModuleImports() throws NoSuchMethodException {
        // All parameter types on declared methods must belong to auth or JDK packages
        for (Method method : TutorProvisioningPort.class.getDeclaredMethods()) {
            for (Class<?> paramType : method.getParameterTypes()) {
                String packageName = paramType.getPackageName();
                assertThat(packageName)
                        .as("Method param type %s must not come from tutor module", paramType.getName())
                        .doesNotStartWith("com.tuestudio.tutor");
            }
        }
    }
}
