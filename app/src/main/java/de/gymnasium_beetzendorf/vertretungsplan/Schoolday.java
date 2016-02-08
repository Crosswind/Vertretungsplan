package de.gymnasium_beetzendorf.vertretungsplan;

import java.util.List;

public class Schoolday {

    private String date; // Date of the school day
    private List<String> changes; //classes that are affected to a change
    private String changesAsString;
    private List<Subject> subjects; //subjects that changed
    private long last_updated;

    // getters
    public String getDate() {
        return date;
    }

    // setters
    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getChanges() {
        return changes;
    }

    public void setChanges(List<String> changes) {
        this.changes = changes;
    }

    // might not be needed and removed in the future
    public String getChangesAsString() {
        for (int n = 0; n < this.getChanges().size(); n++) {
            changesAsString += this.getChanges().get(n) + ",";
        }
        return changesAsString;
    }

    public void setChangesAsString(String changesAsString) {
        this.changesAsString = changesAsString;
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
