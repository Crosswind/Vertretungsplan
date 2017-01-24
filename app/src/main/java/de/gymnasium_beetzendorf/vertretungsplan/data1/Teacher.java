package de.gymnasium_beetzendorf.vertretungsplan.data1;

/**
 * holds information about the teacher. right now its just short/long names.
 * this might end up in the database pulled from a server maybe. more information (contact/email/etc) might be added - not right now.
 */

public enum Teacher {
    Huppertz(0, "HUP", "Huppertz", "Martin"),
    AHLFELD(1, "AHL", "Ahlfeld", ""),
    KRÜGER(2, "KRÜ", "Kürger", ""),
    MEHLBERG(3, "MEH", "Mehlberg", "Robert"),
    PALUTKE(4, "PAL", "Palutke", "Hartmut"),
    TUREK(5, "TUR", "Turek", "");


    private int id;
    private String teacher_short;
    private String teacher_long_last;
    private String teacher_long_first;

    Teacher(int id, String teacher_short, String teacher_long_last, String teacher_long_first) {
        this.id = id;
        this.teacher_short = teacher_short;
        this.teacher_long_last = teacher_long_last;
        this.teacher_long_first = teacher_long_first;
    }

    public int getId() {
        return id;
    }

    private String getTeacher_short() {
        return teacher_short;
    }

    public static String getTeacher_shortById(int id) {
        for (Teacher teacher : values()) {
            if (teacher.id == id) {
                return teacher.teacher_short;
            }
        }
        return "";
    }

    public String getTeacher_long_last() {
        return teacher_long_last;
    }

    public static int getTeacherIdByTeacherShort(String teacher_short) {
        for (Teacher teacher : values()) {
            if (teacher.getTeacher_short().equalsIgnoreCase(teacher_short)) {
                return teacher.getId();
            }
        }
        return 0;
    }
}
