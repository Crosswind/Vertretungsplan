package de.gymnasium_beetzendorf.vertretungsplan.data;

/**
 * holds information about a single substitution
 * header information can be found in SubstitutionDay.class
 */

public class Substitution {

    private String classYearLetter;
    private String classCourse;
    private int period;
    private String subject;
    private String teacher;
    private String room;
    private String info;

    private String changes;

    public Substitution () {
        this.changes = "";
    }


    public String getClassYearLetter() {
        return classYearLetter;
    }

    public void setClassYearLetter(String classYearLetter) {
        this.classYearLetter = classYearLetter;
    }

    public Substitution(String classYearLetter, String classCourse, int period, String subject, String teacher, String room, String info, String changes) {
        this.classYearLetter = classYearLetter;
        this.classCourse = classCourse;
        this.period = period;
        this.subject = subject;
        this.teacher = teacher;
        this.room = room;
        this.info = info;
        this.changes = changes;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
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

    @Override
    public String toString() {
        return "[" + classYearLetter + ", " + classCourse + ", " + period + ", " + subject + ", " + teacher + ", " + room + ", " + info + ", " + changes + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Substitution)) {
            return false;
        }

        Substitution comp = (Substitution) obj;

        return this.classYearLetter.equals(comp.classYearLetter) &&
                this.classCourse.equals(comp.classCourse) &&
                this.period == comp.period &&
                this.subject.equals(comp.subject) &&
                this.teacher.equals(comp.teacher) &&
                this.room.equals(comp.room) &&
                this.info.equals(comp.info);
    }
}
