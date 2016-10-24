package de.gymnasium_beetzendorf.vertretungsplan.data1;

import java.util.List;

/**
 * holds header data about the day substitution is available for as well as a list of the substitutions
 */

public class SubstitutionDay {

    private long date;
    private int school;
    private long updated;
    private List<Substitution> substitutionList;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
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
