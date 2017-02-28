package de.gymnasium_beetzendorf.vertretungsplan.data;

import java.util.List;

/**
 * holds header information about one schoolday as well as the actual lessons
 */

public class LessonDay {

    private int school;
    private String class_year_letter;
    private long valid;
    private List<Lesson> lessonList;

    public int getSchool() {
        return school;
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public String getClass_year_letter() {
        return class_year_letter;
    }

    public void setClass_year_letter(String class_year_letter) {
        this.class_year_letter = class_year_letter;
    }

    public long getValid() {
        return valid;
    }

    public void setValid(long valid) {
        this.valid = valid;
    }

    public List<Lesson> getLessonList() {
        return lessonList;
    }

    public void setLessonList(List<Lesson> lessonList) {
        this.lessonList = lessonList;
    }
}
