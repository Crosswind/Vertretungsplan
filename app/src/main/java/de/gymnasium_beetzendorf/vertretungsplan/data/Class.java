package de.gymnasium_beetzendorf.vertretungsplan.data;


public class Class {

    private String name;
    private String url;

    public Class(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public Class() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
