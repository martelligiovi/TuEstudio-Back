package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.domain.Schedule;
import jakarta.persistence.Embeddable;

@Embeddable
class ScheduleEmbeddable {
    String days;
    String hours;

    protected ScheduleEmbeddable() {}

    static ScheduleEmbeddable from(Schedule s) {
        ScheduleEmbeddable e = new ScheduleEmbeddable();
        e.days = s.days();
        e.hours = s.hours();
        return e;
    }

    Schedule toDomain() { return new Schedule(days, hours); }
}
