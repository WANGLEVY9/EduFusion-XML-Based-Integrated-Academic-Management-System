package edu.fusion.common.model;

public class Selection {

    private String studentId;
    private String courseId;
    private String ownerCollege;
    private Integer score;

    public Selection() {
    }

    public Selection(String studentId, String courseId, String ownerCollege, Integer score) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.ownerCollege = ownerCollege;
        this.score = score;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getOwnerCollege() {
        return ownerCollege;
    }

    public void setOwnerCollege(String ownerCollege) {
        this.ownerCollege = ownerCollege;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
