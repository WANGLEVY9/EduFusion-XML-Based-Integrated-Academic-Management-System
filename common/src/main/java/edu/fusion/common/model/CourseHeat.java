package edu.fusion.common.model;

public class CourseHeat {

    private String courseId;
    private String courseName;
    private String college;
    private int selectedCount;

    public CourseHeat() {
    }

    public CourseHeat(String courseId, String courseName, String college, int selectedCount) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.college = college;
        this.selectedCount = selectedCount;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(int selectedCount) {
        this.selectedCount = selectedCount;
    }
}
