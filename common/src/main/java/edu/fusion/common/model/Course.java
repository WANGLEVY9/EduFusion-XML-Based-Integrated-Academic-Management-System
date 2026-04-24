package edu.fusion.common.model;

public class Course {

    private String id;
    private String name;
    private int credit;
    private String teacher;
    private String location;
    private String college;
    private boolean shared;

    public Course() {
    }

    public Course(String id, String name, int credit, String teacher, String location, String college, boolean shared) {
        this.id = id;
        this.name = name;
        this.credit = credit;
        this.teacher = teacher;
        this.location = location;
        this.college = college;
        this.shared = shared;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }
}
