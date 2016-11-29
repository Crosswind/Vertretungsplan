package de.gymnasium_beetzendorf.vertretungsplan.data1;

import java.util.List;

/**
 * holds header data about the day substitution is available for as well as a list of the substitutions
 */

public class SubstitutionDay {

    // the date this substitution day is valid on
    private long date;
    // differentiate between a and b week (schedule might differ for some classes)
    private String week;
    // the id (defined in School.java) of the school
    private int school;
    // when the plan was updated. will be removed
    private long updated;
    // list of the substitutions that belong to the day
    private List<Substitution> substitutionList;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public int getSchool() {
        return school;
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public List<Substitution> getSubstitutionList() {
        return substitutionList;
    }

    public void setSubstitutionList(List<Substitution> substitutionList) {
        this.substitutionList = substitutionList;
    }
}
