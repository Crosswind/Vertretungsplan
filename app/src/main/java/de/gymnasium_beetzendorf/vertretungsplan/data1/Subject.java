package de.gymnasium_beetzendorf.vertretungsplan.data1;

import java.util.Arrays;
import java.util.List;

/**
 * holds data about the individual subjects
 * this may change in the future and migrate into a database that updates dynamically according to the data from the different xml files
 * right now this works because we only look at one school
 */

public enum Subject {
    Mathe(0, "Mat", "Mathe"),
    Religion(1, "EvR", "Religion"),
    Geographie(2, "Geo", "Geographie"),
    Deutsch(3, "Deu", "Deutsch"),
    Chemie(4, "Che", "Chemie"),
    Geschichte(5, "Ges", "Geschichte"),
    Kunst(6, "Kun", "Kunst"),
    Psychologie(7, "Psy", "Psychologie"),
    Englisch(8, "Eng", "Englisch"),
    Französisch(9, "Frz", "Französisch"),
    Russisch(10, "Rus", "Russisch"),
    Sport(11, "Spo", "Sport"),
    Biologie(12, "Bio", "Biologie"),
    Philosophie(13, "Phi", "Philosophie"),
    Latein(14, "Lat", "Latein"),
    Ethik(15, "Eth", "Ethik"),
    Berufsberatung(16, "BSB", "Berufsberatung"),
    DLI(17, "DLI", "Das Lernen lernen"),
    PCI(18, "PCI", "Einführung mit d. PC"),
    Informatik(19, "Inf", "Informatik"),
    FREISTUNDE(20, "freePeriod", "frei"),
    BERUFSBERATUNG(21, "BSB", "Berufsberatung");


    private int id;
    private String subject_short;
    private String subject_long;

    Subject(int id, String subject_short, String subject_long) {
        this.id = id;
        this.subject_short = subject_short;
        this.subject_long = subject_long;
    }

    public static List<Subject> list() {
        return Arrays.asList(values());
    }

    private int getId() {
        return id;
    }

    private String getSubject_short() {
        return subject_short;
    }

    private String getSubject_long() {
        return subject_long;
    }

    Subject getSubjectById(int id) {
        for (Subject subject : values()) {
            if (subject.getId() == id) {
                return subject;
            }
        }
        return null;
    }

    public static String getSubjectShortById(int id) {
        for (Subject subject : list()) {
            if (subject.getId() == id) {
                return subject.getSubject_short();
            }
        }
        return "";
    }

    static String getSubjectLongById(int id) {
        for (Subject subject : list()) {
            if (subject.getId() == id) {
                return subject.getSubject_long();
            }
        }
        return "";
    }

    public static int getSubjectIdBySubjectShort(String subject_short) {
        for (Subject subject : list()) {
            if (subject.getSubject_short().equalsIgnoreCase(subject_short)) {
                return subject.getId();
            }
        }
        return 0;
    }
}
