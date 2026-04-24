package edu.fusion.common.model;

public class Student {

    private String id;
    private String name;
    private String sex;
    private String major;
    private String college;

    public Student() {
    }

    public Student(String id, String name, String sex, String major, String college) {
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.major = major;
        this.college = college;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }
}
