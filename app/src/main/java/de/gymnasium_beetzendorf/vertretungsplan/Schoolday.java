package de.gymnasium_beetzendorf.vertretungsplan;

import java.util.List;

public class Schoolday {

    private long date; // Date of the school day
    private List<String> changes; //classes that are affected to a change
    private List<Subject> subjects; //subjects that changed
    private long last_updated;

    // getters
    public long getDate() {
        return date;
    }

    // setters
    public void setDate(long date) {
        this.date = date;
    }

    public List<String> getChanges() {
        return changes;
    }

    public void setChanges(List<String> changes) {
        this.changes = changes;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public long getLastUpdated() {
        return last_updated;
    }

    public void setLastUpdated(long last_updated) {
        this.last_updated = last_updated;
    }
}
