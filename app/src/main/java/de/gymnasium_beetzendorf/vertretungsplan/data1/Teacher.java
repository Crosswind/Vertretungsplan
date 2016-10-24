package de.gymnasium_beetzendorf.vertretungsplan.data1;

/**
 * holds information about the teacher. right now its just short/long names.
 * this might end up in the database pulled from a server maybe. more information (contact/email/etc) might be added - not right now.
 */

public enum Teacher {
    ;

    private int id;
    private String teacher_short;
    private String teacher_long;

    Teacher(int id, String teacher_short, String teacher_long) {
        this.id = id;
        this.teacher_short = teacher_short;
        this.teacher_long = teacher_long;
    }

    public int getId() {
        return id;
    }

    public String getTeacher_short() {
        return teacher_short;
    }

    public String getTeacher_long() {
        return teacher_long;
    }
}
