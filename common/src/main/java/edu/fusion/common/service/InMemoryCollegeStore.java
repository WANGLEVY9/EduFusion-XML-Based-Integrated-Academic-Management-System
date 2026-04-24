package edu.fusion.common.service;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.model.Selection;
import edu.fusion.common.model.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class InMemoryCollegeStore {

    private final String collegeCode;
    private final List<Student> students;
    private final List<Course> courses;
    private final List<Selection> selections;
    private final Map<String, String> studentPasswords;
    private final Map<String, String> adminPasswords;

    private InMemoryCollegeStore(
            String collegeCode,
            List<Student> students,
            List<Course> courses,
            List<Selection> selections,
            Map<String, String> studentPasswords,
            Map<String, String> adminPasswords
    ) {
        this.collegeCode = collegeCode;
        this.students = students;
        this.courses = courses;
        this.selections = selections;
        this.studentPasswords = studentPasswords;
        this.adminPasswords = adminPasswords;
    }

    public static InMemoryCollegeStore seeded(String collegeCode) {
        List<Student> students = new ArrayList<>();
        List<Course> courses = new ArrayList<>();
        List<Selection> selections = new ArrayList<>();
        Map<String, String> studentPasswords = new HashMap<>();
        Map<String, String> adminPasswords = new HashMap<>();

        for (int i = 1; i <= 50; i++) {
            String sid = collegeCode + String.format("%03d", i);
            Student student = new Student(sid, "Student" + sid, i % 2 == 0 ? "F" : "M", "Major" + (i % 5 + 1), collegeCode);
            students.add(student);
            studentPasswords.put(sid, "123456");
        }

        for (int i = 1; i <= 10; i++) {
            String cid = collegeCode + "C" + String.format("%02d", i);
            Course course = new Course(
                    cid,
                    "Course" + cid,
                    2 + i % 3,
                    "Teacher" + collegeCode + i,
                    collegeCode + "-R" + (100 + i),
                    collegeCode,
                    i <= 6
            );
            courses.add(course);
        }

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            for (int j = 0; j < 5; j++) {
                Course course = courses.get((i + j) % courses.size());
                selections.add(new Selection(student.getId(), course.getId(), collegeCode, null));
            }
        }

        adminPasswords.put(collegeCode + "_ADMIN", "admin123");
        return new InMemoryCollegeStore(collegeCode, students, courses, selections, studentPasswords, adminPasswords);
    }

    public String getCollegeCode() {
        return collegeCode;
    }

    public List<Course> listAllCourses() {
        return new ArrayList<>(courses);
    }

    public List<Course> listSharedCourses() {
        return courses.stream().filter(Course::isShared).collect(Collectors.toList());
    }

    public boolean selectCourse(String studentId, String courseId) {
        boolean exists = selections.stream().anyMatch(s -> s.getStudentId().equals(studentId) && s.getCourseId().equals(courseId));
        boolean courseExists = courses.stream().anyMatch(c -> c.getId().equals(courseId));
        if (exists || !courseExists) {
            return false;
        }
        selections.add(new Selection(studentId, courseId, collegeCode, null));
        return true;
    }

    public boolean dropCourse(String studentId, String courseId) {
        return selections.removeIf(s -> Objects.equals(studentId, s.getStudentId()) && Objects.equals(courseId, s.getCourseId()));
    }

    public int countStudents() {
        return students.size();
    }

    public int countCourses() {
        return courses.size();
    }

    public int countSelections() {
        return selections.size();
    }

    public int countSharedCourses() {
        return (int) courses.stream().filter(Course::isShared).count();
    }

    public List<CourseHeat> topCourses(int topN) {
        Map<String, Integer> heatMap = new HashMap<>();
        for (Selection selection : selections) {
            heatMap.put(selection.getCourseId(), heatMap.getOrDefault(selection.getCourseId(), 0) + 1);
        }

        Map<String, Integer> sorted = heatMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(topN)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        List<CourseHeat> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
            Course course = courses.stream().filter(c -> c.getId().equals(entry.getKey())).findFirst().orElse(null);
            if (course != null) {
                result.add(new CourseHeat(course.getId(), course.getName(), course.getCollege(), entry.getValue()));
            }
        }
        return result;
    }

    public boolean authenticateStudent(String username, String password) {
        return password != null && password.equals(studentPasswords.get(username));
    }

    public boolean authenticateAdmin(String username, String password) {
        return password != null && password.equals(adminPasswords.get(username));
    }
}
