package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.domain.MethodologyFeature;
import jakarta.persistence.Embeddable;

@Embeddable
class MethodologyFeatureEmbeddable {
    String label;
    boolean value;

    protected MethodologyFeatureEmbeddable() {}

    static MethodologyFeatureEmbeddable from(MethodologyFeature f) {
        MethodologyFeatureEmbeddable e = new MethodologyFeatureEmbeddable();
        e.label = f.label();
        e.value = f.value();
        return e;
    }

    MethodologyFeature toDomain() { return new MethodologyFeature(label, value); }
}
