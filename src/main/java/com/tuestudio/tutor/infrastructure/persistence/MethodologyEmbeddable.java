package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.domain.Methodology;
import com.tuestudio.tutor.domain.MethodologyFeature;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.List;

@Embeddable
class MethodologyEmbeddable {
    String intro;

    @ElementCollection
    @CollectionTable(name = "tutor_methodology_features", joinColumns = @JoinColumn(name = "tutor_id"))
    private List<MethodologyFeatureEmbeddable> features = new ArrayList<>();

    protected MethodologyEmbeddable() {}

    static MethodologyEmbeddable from(Methodology m) {
        MethodologyEmbeddable e = new MethodologyEmbeddable();
        e.intro = m.intro();
        e.features = m.features().stream().map(MethodologyFeatureEmbeddable::from).toList();
        return e;
    }

    Methodology toDomain() {
        List<MethodologyFeature> domainFeatures = features.stream()
                .map(MethodologyFeatureEmbeddable::toDomain).toList();
        return new Methodology(intro, domainFeatures);
    }
}
