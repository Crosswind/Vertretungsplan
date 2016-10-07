package de.gymnasium_beetzendorf.vertretungsplan.data;

public class Substitution {

    private int school;
    private long valid_on;
    private int day;
    private long updated;

    public Substitution(int school, long valid_on, int day, long updated) {
        this.school = school;
        this.valid_on = valid_on;
        this.day = day;
        this.updated = updated;
    }
}
