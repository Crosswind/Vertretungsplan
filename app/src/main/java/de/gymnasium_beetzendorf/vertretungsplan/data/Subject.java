package de.gymnasium_beetzendorf.vertretungsplan.data;

public class Subject {

    private String course;
    private int period;
    private String subject;
    private String teacher;
    private String room;
    private String info;



    // constructors

    public Subject() {
        this.info = null;
    }

    // getters

    // probably useless - future release will show
    public static String convertSubjectsToLong(String shortSubject) {
        String longSubject = "";
        switch (shortSubject) {
            case "Mat":
                longSubject = "Mathe";
                break;
            case "EvR":
                longSubject = "Religion";
                break;
            case "Geo":
                longSubject = "Geographie";
                break;
            case "Deu":
                longSubject = "Deutsch";
                break;
            case "Che":
                longSubject = "Chemie";
                break;
            case "Ges":
                longSubject = "Geschichte";
                break;
            case "Kun":
                longSubject = "Kunst";
                break;
            case "---":
                longSubject = "kein Fach";
                break;
            case "Psy":
                longSubject = "Psychologie";
                break;
            case "Eng":
                longSubject = "Englisch";
                break;
            case "Frz":
                longSubject = "Französisch";
                break;
            case "Rus":
                longSubject = "Russisch";
                break;
            case "Spo":
                longSubject = "Sport";
                break;
            case "Bio":
                longSubject = "Biologie";
                break;
            case "Phi":
                longSubject = "Philosophie";
                break;
            case "Lat":
                longSubject = "Latein";
                break;
            case "Eth":
                longSubject = "Ethik";
                break;
            default:
                longSubject = shortSubject;
                break;
        }
        return longSubject;
    }

    // not yet implemented
    public static String convertTeachersToLong(String shortTeacher) {
        return shortTeacher;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
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


    // setters

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

}
