package edu.fusion.servera.service;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.service.CollegeGateway;
import edu.fusion.common.service.JdbcCollegeRepository;
import edu.fusion.common.util.JdbcConfigLoader;

import java.util.List;

public class CollegeAGateway implements CollegeGateway {

    private static final JdbcCollegeRepository REPOSITORY = new JdbcCollegeRepository(
            "A",
            JdbcConfigLoader.loadFromClasspath("/db/college-a.properties"),
            "StudentA",
            "sid",
            "password",
            "AdminA",
            "username",
            "password",
            "CourseA",
            "cid",
            "cname",
            "credit",
            "teacher",
            "room",
            "shareFlag",
            "1",
            "SelectA",
            "sid",
            "cid",
            "score");

    @Override
    public String getCollegeCode() {
        return REPOSITORY.getCollegeCode();
    }

    @Override
    public boolean authenticateStudent(String username, String password) {
        return REPOSITORY.authenticateStudent(username, password);
    }

    @Override
    public boolean authenticateAdmin(String username, String password) {
        return REPOSITORY.authenticateAdmin(username, password);
    }

    @Override
    public List<Course> listAllCourses() {
        return REPOSITORY.listAllCourses();
    }

    @Override
    public List<Course> listSharedCourses() {
        return REPOSITORY.listSharedCourses();
    }

    @Override
    public List<Course> listStudentCourses(String studentId) {
        return REPOSITORY.listStudentCourses(studentId);
    }

    @Override
    public boolean selectCourse(String studentId, String courseId) {
        return REPOSITORY.selectCourse(studentId, courseId);
    }

    @Override
    public boolean dropCourse(String studentId, String courseId) {
        return REPOSITORY.dropCourse(studentId, courseId);
    }

    @Override
    public int countStudents() {
        return REPOSITORY.countStudents();
    }

    @Override
    public int countCourses() {
        return REPOSITORY.countCourses();
    }

    @Override
    public int countSelections() {
        return REPOSITORY.countSelections();
    }

    @Override
    public int countSharedCourses() {
        return REPOSITORY.countSharedCourses();
    }

    @Override
    public List<CourseHeat> topCourses(int topN) {
        return REPOSITORY.topCourses(topN);
    }
}
