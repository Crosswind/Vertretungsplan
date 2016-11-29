package de.gymnasium_beetzendorf.vertretungsplan.data1;

/**
 * holds information about a single substitution
 * header information can be found in SubstitutionDay.class
 */

public class Substitution {

    private String classYearLetter;
    private String classCourse;
    private int period;
    private int subject;
    private int teacher;
    private String room;
    private String info;

    private String changes;


    public String getClassYearLetter() {
        return classYearLetter;
    }

    public void setClassYearLetter(String classYearLetter) {
        this.classYearLetter = classYearLetter;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getSubject() {
        return subject;
    }

    public void setSubject(int subject) {
        this.subject = subject;
    }

    public int getTeacher() {
        return teacher;
    }

    public void setTeacher(int teacher) {
        this.teacher = teacher;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getClassCourse() {
        return classCourse;
    }

    public void setClassCourse(String classCourse) {
        this.classCourse = classCourse;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }
}
