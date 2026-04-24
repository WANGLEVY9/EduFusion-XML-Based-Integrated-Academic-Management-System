package edu.fusion.common.service;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import java.util.List;

public interface CollegeGateway {

    String getCollegeCode();

    boolean authenticateStudent(String username, String password);

    boolean authenticateAdmin(String username, String password);

    List<Course> listAllCourses();

    List<Course> listSharedCourses();

    List<Course> listStudentCourses(String studentId);

    boolean selectCourse(String studentId, String courseId);

    boolean dropCourse(String studentId, String courseId);

    int countStudents();

    int countCourses();

    int countSelections();

    int countSharedCourses();

    List<CourseHeat> topCourses(int topN);
}
