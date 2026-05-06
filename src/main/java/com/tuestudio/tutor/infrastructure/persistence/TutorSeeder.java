package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.domain.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!test")
class TutorSeeder {

    private final TutorJpaRepository tutorRepository;
    private final ContactRequestJpaRepository contactRepository;

    TutorSeeder(TutorJpaRepository tutorRepository, ContactRequestJpaRepository contactRepository) {
        this.tutorRepository = tutorRepository;
        this.contactRepository = contactRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    void seed() {
        if (tutorRepository.count() > 0) return;

        tutorRepository.saveAll(List.of(
                TutorJpaEntity.fromDomain(makeTutor(
                        "María González",
                        "Matemáticas", "UBA", "Buenos Aires", "Virtual y Presencial",
                        4.9, 127,
                        "Lic. en Matemáticas con 8 años de experiencia docente. Especializada en Análisis y Álgebra para ingeniería y exactas.",
                        "https://randomuser.me/api/portraits/women/44.jpg",
                        2800.0,
                        List.of(new Subject("Análisis Matemático", "Cálculo diferencial e integral", "📐"),
                                new Subject("Álgebra Lineal", "Matrices, vectores y transformaciones", "🔢")),
                        new Methodology("Mi método se basa en la comprensión profunda antes de la memorización.",
                                List.of(new MethodologyFeature("Clases personalizadas", true),
                                        new MethodologyFeature("Material propio", true),
                                        new MethodologyFeature("Seguimiento semanal", true))),
                        List.of(new Schedule("Lunes a Viernes", "9:00 - 20:00"),
                                new Schedule("Sábados", "10:00 - 14:00")),
                        "Consultar disponibilidad con 48hs de anticipación",
                        List.of(new Plan("Clase suelta", "1 clase de 90 min", "$2.800", "por clase", null, false),
                                new Plan("Pack mensual", "8 clases al mes", "$18.000", "por mes", "Popular", true),
                                new Plan("Intensivo", "16 clases al mes", "$32.000", "por mes", null, false))
                )),
                TutorJpaEntity.fromDomain(makeTutor(
                        "Lucas Martínez",
                        "Física", "UTN", "Córdoba", "Virtual",
                        4.7, 89,
                        "Ing. en Electrónica. Doy clases de Física I, II y Electromagnetismo. Metodología basada en problemas reales.",
                        "https://randomuser.me/api/portraits/men/32.jpg",
                        2500.0,
                        List.of(new Subject("Física I", "Mecánica clásica y termodinámica", "⚡"),
                                new Subject("Física II", "Ondas, óptica y electromagnetismo", "🔭")),
                        new Methodology("Aprendo mejor resolviendo ejercicios, no leyendo teoría.",
                                List.of(new MethodologyFeature("Ejercicios guiados", true),
                                        new MethodologyFeature("Simulacros de parcial", true),
                                        new MethodologyFeature("Grabación de clases", false))),
                        List.of(new Schedule("Martes y Jueves", "18:00 - 22:00"),
                                new Schedule("Domingos", "10:00 - 16:00")),
                        null,
                        List.of(new Plan("Clase suelta", "1 clase de 90 min", "$2.500", "por clase", null, false),
                                new Plan("Pack 4 clases", "4 clases a coordinar", "$9.000", "por mes", "Recomendado", true))
                )),
                TutorJpaEntity.fromDomain(makeTutor(
                        "Sofía Reyes",
                        "Programación", "UNLAM", "Buenos Aires", "Virtual y Presencial",
                        5.0, 43,
                        "Desarrolladora fullstack con 5 años de experiencia. Doy clases de programación desde cero hasta nivel avanzado.",
                        "https://randomuser.me/api/portraits/women/68.jpg",
                        3200.0,
                        List.of(new Subject("Programación I", "Fundamentos, algoritmos y estructuras", "💻"),
                                new Subject("Bases de Datos", "SQL, diseño relacional, NoSQL", "🗄️"),
                                new Subject("Java", "POO, Spring Boot, APIs REST", "☕")),
                        new Methodology("El código se aprende escribiendo código, no mirando tutoriales.",
                                List.of(new MethodologyFeature("Proyectos reales", true),
                                        new MethodologyFeature("Code review", true),
                                        new MethodologyFeature("Recursos adicionales", true))),
                        List.of(new Schedule("Lunes, Miércoles y Viernes", "19:00 - 23:00")),
                        "Horarios rotativos, consultar por WhatsApp",
                        List.of(new Plan("Clase suelta", "1 clase de 2 hs", "$3.200", "por clase", null, false),
                                new Plan("Mentoría mensual", "8 clases + proyectos", "$22.000", "por mes", "Más elegido", true),
                                new Plan("Bootcamp", "Proyecto completo 1 mes", "$45.000", "único pago", "Intensivo", false))
                )),
                TutorJpaEntity.fromDomain(makeTutor(
                        "Tomás Herrera",
                        "Química", "UBA", "Buenos Aires", "Presencial",
                        4.5, 61,
                        "Lic. en Química. Apoyo universitario en Química General, Orgánica y Analítica para todas las carreras de la UBA.",
                        "https://randomuser.me/api/portraits/men/75.jpg",
                        2200.0,
                        List.of(new Subject("Química General", "Estequiometría, equilibrio y termodinámica", "🧪"),
                                new Subject("Química Orgánica", "Mecanismos de reacción y nomenclatura", "⚗️")),
                        new Methodology("Priorizo el entendimiento del 'por qué' antes de memorizar fórmulas.",
                                List.of(new MethodologyFeature("Material visual", true),
                                        new MethodologyFeature("Resolución en grupo", false),
                                        new MethodologyFeature("Autoevaluaciones", true))),
                        List.of(new Schedule("Miércoles y Viernes", "16:00 - 21:00"),
                                new Schedule("Sábados", "9:00 - 13:00")),
                        null,
                        List.of(new Plan("Clase suelta", "1 clase de 90 min", "$2.200", "por clase", null, false),
                                new Plan("Pack quincenal", "4 clases", "$8.000", "quincenal", "Popular", true))
                ))
        ));
    }

    private Tutor makeTutor(String name, String specialty, String university, String location,
                             String modalidad, double rating, int reviews, String bio, String photoUrl,
                             double hourlyRate, List<Subject> subjects, Methodology methodology,
                             List<Schedule> schedules, String schedulesNote, List<Plan> plans) {
        return new Tutor(
                new TutorId(UUID.randomUUID()), name, specialty, university, location, modalidad,
                rating, reviews, bio, photoUrl, true, hourlyRate,
                subjects, methodology, schedules, schedulesNote, plans
        );
    }
}
