package edu.fusion.common.model;

import java.util.ArrayList;
import java.util.List;

public class GlobalStatistics {

    private int totalStudents;
    private int totalCourses;
    private int totalSelections;
    private int totalSharedCourses;
    private List<CourseHeat> topCourses = new ArrayList<>();

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(int totalCourses) {
        this.totalCourses = totalCourses;
    }

    public int getTotalSelections() {
        return totalSelections;
    }

    public void setTotalSelections(int totalSelections) {
        this.totalSelections = totalSelections;
    }

    public int getTotalSharedCourses() {
        return totalSharedCourses;
    }

    public void setTotalSharedCourses(int totalSharedCourses) {
        this.totalSharedCourses = totalSharedCourses;
    }

    public List<CourseHeat> getTopCourses() {
        return topCourses;
    }

    public void setTopCourses(List<CourseHeat> topCourses) {
        this.topCourses = topCourses;
    }
}
