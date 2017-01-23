package de.gymnasium_beetzendorf.vertretungsplan.data1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum School {
    Gymnasium_Beetzendorf (0,
            "Gymnasium Beetzendorf",
            "http://gymnasium-beetzendorf.de/vplankl/Vertretungsplan_Klassen.xml",
            "http://gymnasium-beetzendorf.de/stundenkl/default.html",
            "http://gymnasium-beetzendorf.de/stundenkl/");
   /*Jahn_Gymnasium(1,
            "Jahn Gymnasium Salzwedel",
            "",
            "",
            "");*/


    private int id;
    private String name, substitutionUrl, scheduleOverviewUrl, scheduleDirectoryUrl;

    School(int id, String name, String substitutionUrl, String scheduleOverviewUrl, String scheduleDirectoryUrl) {
        this.id = id;
        this.name = name;
        this.substitutionUrl = substitutionUrl;
        this.scheduleOverviewUrl = scheduleOverviewUrl;
        this.scheduleDirectoryUrl = scheduleDirectoryUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSubstitutionUrl() {
        return substitutionUrl;
    }

    public String getScheduleOverviewUrl() {
        return scheduleOverviewUrl;
    }

    public String getScheduleDirectoryUrl() {
        return scheduleDirectoryUrl;
    }

    public static List<School> schoolList() {
        return Arrays.asList(values());
    }

    public static List<String> schoolListNames() {
        List<String> schoolListNames = new ArrayList<>();
        for (School school : values()) {
            try {
                schoolListNames.add(school.getName());
            } catch (NullPointerException e) {
                break;
            }
        }
        return schoolListNames;
    }

    public static School findSchoolByName(String name) throws IllegalAccessException {
        for(School school : schoolList()) {
            if((school.getName()).equalsIgnoreCase(name)) {
                return school;
            }
        }

        throw new IllegalAccessException("No school with this name(" + name + ") found.");
    }

    public static int findSchoolIdByName(String name) {
        for (School school : schoolList()) {
            if (school.getName().equalsIgnoreCase(name)) {
                return school.getId();
            }
        }
        return -1;
    }

    public static School findSchoolById(int id) throws IllegalAccessException {
        for(School school : schoolList()) {
            if (school.getId() == id) {
                return school;
            }
        }

        throw new IllegalAccessException("No school with this id(" + id + ") found.");
    }
}
