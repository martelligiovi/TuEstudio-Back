package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.domain.Plan;
import jakarta.persistence.Embeddable;

@Embeddable
class PlanEmbeddable {
    String name;
    String description;
    String price;
    String unit;
    String badge;
    boolean featured;

    protected PlanEmbeddable() {}

    static PlanEmbeddable from(Plan p) {
        PlanEmbeddable e = new PlanEmbeddable();
        e.name = p.name();
        e.description = p.description();
        e.price = p.price();
        e.unit = p.unit();
        e.badge = p.badge();
        e.featured = p.featured();
        return e;
    }

    Plan toDomain() { return new Plan(name, description, price, unit, badge, featured); }
}
