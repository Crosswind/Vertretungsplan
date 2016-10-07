package de.gymnasium_beetzendorf.vertretungsplan.data;

/**
 * Created by David on 09.09.16.
 */
public class Lesson {

    private String type; // defines whether it's reg schedule or substitution
    private String year; // using a string just in case some weird things appear and ints are not enough
    private String classletter; // couldn't use class
    private String course;
    private int school; // number taken from class School
    private long valid_from;
    private long valid_on; // for substitutions that's the date this sub applies to
    private int day; // 1 = Monday, 2 = Tuesday, etc
    private int period; // 1 - 8
    private String subject;
    private String teacher;
    private String room;
    private String info;

    public Lesson() {
    }

    public Lesson(String type, String year, String classletter, int school, long valid_from, int day) {
        this.type = type;
        this.year = year;
        this.classletter = classletter;
        this.school = school;
        this.valid_from = valid_from;
        this.day = day;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public long getValid_on() {
        return valid_on;
    }

    public void setValid_on(long valid_on) {
        this.valid_on = valid_on;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getClassletter() {
        return classletter;
    }

    public void setClassletter(String classletter) {
        this.classletter = classletter;
    }

    public int getSchool() {
        return school;
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public long getValid_from() {
        return valid_from;
    }

    public void setValid_from(long valid_from) {
        this.valid_from = valid_from;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
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

    public boolean isEmpty() {
        return this.type.isEmpty() &&
                this.year.isEmpty() &&
                this.classletter.isEmpty() &&
                this.course.isEmpty() &&
                String.valueOf(this.school).isEmpty() &&
                String.valueOf(this.valid_from).isEmpty() &&
                String.valueOf(this.valid_on).isEmpty() &&
                String.valueOf(this.day).isEmpty() &&
                String.valueOf(this.period).isEmpty() &&
                this.subject.isEmpty() &&
                this.teacher.isEmpty() &&
                this.room.isEmpty() &&
                this.info.isEmpty();
    }
}