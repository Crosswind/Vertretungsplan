package de.gymnasium_beetzendorf.vertretungsplan.data;

/**
 * holds data about a lesson from the schedule of a student.
 * header information found in LessonDay.class
 */

public class Lesson {

    private int period;
    private int subject;
    private int teacher;
    private String room;
    private String course;
    // string that contains which fields have been changed. format: field1|field2|
    private String change;

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

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }
}
