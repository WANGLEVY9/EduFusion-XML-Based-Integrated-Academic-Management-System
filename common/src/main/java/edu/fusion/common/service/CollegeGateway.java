package edu.fusion.common.service;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.model.Selection;
import edu.fusion.common.model.Student;
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

    List<Student> listAllStudents();

    List<Selection> listAllSelections();

    // ===== Admin CRUD: Students =====
    boolean addStudent(Student student);

    boolean updateStudent(Student student);

    boolean deleteStudent(String studentId);

    // ===== Admin CRUD: Courses =====
    boolean addCourse(Course course);

    boolean updateCourse(Course course);

    boolean deleteCourse(String courseId);

    // ===== Admin: Scores =====
    boolean updateScore(String studentId, String courseId, int score);

    // ===== Admin: Audit Logs =====
    String getAuditLogs(int limit);
}
