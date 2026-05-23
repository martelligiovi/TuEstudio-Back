package com.tuestudio.catalog.infrastructure.persistence;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Profile("dev")
class CatalogSeeder {

    private final UniversityJpaRepository universities;
    private final CareerJpaRepository careers;
    private final SubjectJpaRepository subjects;

    CatalogSeeder(UniversityJpaRepository universities, CareerJpaRepository careers,
                  SubjectJpaRepository subjects) {
        this.universities = universities;
        this.careers = careers;
        this.subjects = subjects;
    }

    @EventListener(ApplicationReadyEvent.class)
    void seed() {
        if (universities.count() > 0) return;

        universities.saveAll(List.of(
                new UniversityJpaEntity("uba", "UBA", "🎓"),
                new UniversityJpaEntity("utn", "UTN", "🔧"),
                new UniversityJpaEntity("unlam", "UNLAM", "📚"),
                new UniversityJpaEntity("unc", "UNC", "🏛️"),
                new UniversityJpaEntity("unq", "UNQ", "💡")
        ));

        careers.saveAll(List.of(
                new CareerJpaEntity("ingenieria-informatica", "Ingeniería Informática", "utn"),
                new CareerJpaEntity("licenciatura-sistemas", "Lic. en Sistemas", "uba"),
                new CareerJpaEntity("ingenieria-civil", "Ingeniería Civil", "uba"),
                new CareerJpaEntity("medicina", "Medicina", "uba"),
                new CareerJpaEntity("ingenieria-electronica", "Ingeniería Electrónica", "utn"),
                new CareerJpaEntity("ciencias-economicas", "Ciencias Económicas", "uba"),
                new CareerJpaEntity("exactas", "Cs. Exactas y Naturales", "uba")
        ));

        subjects.saveAll(List.of(
                new SubjectJpaEntity("analisis-matematico", "Análisis Matemático", "📐"),
                new SubjectJpaEntity("algebra", "Álgebra Lineal", "🔢"),
                new SubjectJpaEntity("fisica-i", "Física I", "⚡"),
                new SubjectJpaEntity("fisica-ii", "Física II", "🔭"),
                new SubjectJpaEntity("quimica-general", "Química General", "🧪"),
                new SubjectJpaEntity("programacion-i", "Programación I", "💻"),
                new SubjectJpaEntity("bases-datos", "Bases de Datos", "🗄️"),
                new SubjectJpaEntity("java", "Java", "☕"),
                new SubjectJpaEntity("estadistica", "Estadística", "📊"),
                new SubjectJpaEntity("termodinamica", "Termodinámica", "🌡️")
        ));
    }
}
