package com.tuestudio.tutor.domain;

import java.util.List;

public final class Tutor {
    private final TutorId id;
    private final String name;
    private final String subjectSpecialty;
    private final String university;
    private final String location;
    private final String modalidad;
    private final double rating;
    private final int reviewsCount;
    private final String bio;
    private final String photoUrl;
    private final boolean active;
    private final double hourlyRate;
    private final List<Subject> subjects;
    private final Methodology methodology;
    private final List<Schedule> schedules;
    private final String schedulesNote;
    private final List<Plan> plans;
    private final String phoneNumber;

    public Tutor(TutorId id, String name, String subjectSpecialty, String university,
                 String location, String modalidad, double rating, int reviewsCount,
                 String bio, String photoUrl, boolean active, double hourlyRate,
                 List<Subject> subjects, Methodology methodology,
                 List<Schedule> schedules, String schedulesNote, List<Plan> plans,
                 String phoneNumber) {
        this.id = id;
        this.name = name;
        this.subjectSpecialty = subjectSpecialty;
        this.university = university;
        this.location = location;
        this.modalidad = modalidad;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.bio = bio;
        this.photoUrl = photoUrl;
        this.active = active;
        this.hourlyRate = hourlyRate;
        this.subjects = subjects;
        this.methodology = methodology;
        this.schedules = schedules;
        this.schedulesNote = schedulesNote;
        this.plans = plans;
        this.phoneNumber = phoneNumber;
    }

    public TutorId id() { return id; }
    public String name() { return name; }
    public String subjectSpecialty() { return subjectSpecialty; }
    public String university() { return university; }
    public String location() { return location; }
    public String modalidad() { return modalidad; }
    public double rating() { return rating; }
    public int reviewsCount() { return reviewsCount; }
    public String bio() { return bio; }
    public String photoUrl() { return photoUrl; }
    public boolean active() { return active; }
    public double hourlyRate() { return hourlyRate; }
    public List<Subject> subjects() { return subjects; }
    public Methodology methodology() { return methodology; }
    public List<Schedule> schedules() { return schedules; }
    public String schedulesNote() { return schedulesNote; }
    public List<Plan> plans() { return plans; }
    public String phoneNumber() { return phoneNumber; }
}
