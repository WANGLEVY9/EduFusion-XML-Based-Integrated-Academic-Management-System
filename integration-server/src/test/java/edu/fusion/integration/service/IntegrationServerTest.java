package edu.fusion.integration.service;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.model.GlobalStatistics;
import edu.fusion.common.model.Result;
import edu.fusion.common.service.CollegeGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationServerTest {

    private IntegrationServer server;

    @BeforeEach
    void setUp() {
        GatewayStub gatewayA = new GatewayStub("A", "A101", "A102");
        GatewayStub gatewayB = new GatewayStub("B", "B101", "B102");
        GatewayStub gatewayC = new GatewayStub("C", "C101", "C102");
        server = new IntegrationServer(Arrays.asList(gatewayA, gatewayB, gatewayC));
    }

    @Test
    void queryCoursesShouldReturnCollegeCourses() {
        Result<List<Course>> result = server.queryCourses("A");
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        assertEquals("A", result.getData().get(0).getCollege());
    }

    @Test
    void shareCoursesShouldExcludeSourceCollege() {
        Result<List<Course>> result = server.shareCourses("A");
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        assertEquals("B", result.getData().get(0).getCollege());
        assertEquals("C", result.getData().get(1).getCollege());
    }

    @Test
    void crossSelectAndDropShouldRouteByCoursePrefix() {
        Result<Boolean> selected = server.crossSelect("A001", "B101");
        assertTrue(selected.isSuccess());

        Result<List<Course>> myCourses = server.myCourses("B", "A001");
        assertTrue(myCourses.isSuccess());
        assertEquals(1, myCourses.getData().size());
        assertEquals("B101", myCourses.getData().get(0).getId());

        Result<Boolean> dropped = server.dropCourse("A001", "B101");
        assertTrue(dropped.isSuccess());

        Result<List<Course>> afterDrop = server.myCourses("B", "A001");
        assertTrue(afterDrop.isSuccess());
        assertEquals(0, afterDrop.getData().size());
    }

    @Test
    void crossSelectShouldFailForUnknownCollegePrefix() {
        Result<Boolean> result = server.crossSelect("A001", "X999");
        assertFalse(result.isSuccess());
    }

    @Test
    void statisticsShouldAggregateCounts() {
        Result<GlobalStatistics> result = server.statistics();
        assertTrue(result.isSuccess());
        assertEquals(30, result.getData().getTotalStudents());
        assertEquals(6, result.getData().getTotalCourses());
        assertEquals(0, result.getData().getTotalSelections());
        assertEquals(3, result.getData().getTotalSharedCourses());
    }

    @Test
    void processRequestXmlShouldValidateAndRoute() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<request><type>queryCourses</type><college>A</college></request>";
        Result<org.dom4j.Document> result = server.processRequestXml(xml);
        assertTrue(result.isSuccess());
        String responseXml = edu.fusion.common.util.Dom4jXmlService.toCompactString(result.getData());
        assertTrue(responseXml.contains("<success>true</success>"));
        assertTrue(responseXml.contains("<course>"));
    }

    @Test
    void processRequestXmlShouldFailForUnknownType() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<request><type>unknownType</type></request>";
        Result<org.dom4j.Document> result = server.processRequestXml(xml);
        assertFalse(result.isSuccess());
    }

    private static final class GatewayStub implements CollegeGateway {

        private final String collegeCode;
        private final List<Course> allCourses = new ArrayList<>();
        private final List<Course> sharedCourses = new ArrayList<>();
        private final List<SelectionRecord> selections = new ArrayList<>();

        private GatewayStub(String collegeCode, String sharedCourseId, String normalCourseId) {
            this.collegeCode = collegeCode;
            allCourses.add(createCourse(sharedCourseId, true));
            allCourses.add(createCourse(normalCourseId, false));
            sharedCourses.add(createCourse(sharedCourseId, true));
        }

        @Override
        public String getCollegeCode() {
            return collegeCode;
        }

        @Override
        public boolean authenticateStudent(String username, String password) {
            return true;
        }

        @Override
        public boolean authenticateAdmin(String username, String password) {
            return true;
        }

        @Override
        public List<Course> listAllCourses() {
            return new ArrayList<>(allCourses);
        }

        @Override
        public List<Course> listSharedCourses() {
            return new ArrayList<>(sharedCourses);
        }

        @Override
        public List<Course> listStudentCourses(String studentId) {
            if (studentId == null || studentId.trim().isEmpty()) {
                return Collections.emptyList();
            }
            List<Course> result = new ArrayList<>();
            for (SelectionRecord selection : selections) {
                if (studentId.equals(selection.studentId)) {
                    for (Course course : allCourses) {
                        if (course.getId().equals(selection.courseId)) {
                            result.add(copyCourse(course));
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public boolean selectCourse(String studentId, String courseId) {
            if (!existsCourse(courseId)) {
                return false;
            }
            for (SelectionRecord selection : selections) {
                if (selection.studentId.equals(studentId) && selection.courseId.equals(courseId)) {
                    return false;
                }
            }
            selections.add(new SelectionRecord(studentId, courseId));
            return true;
        }

        @Override
        public boolean dropCourse(String studentId, String courseId) {
            for (int i = 0; i < selections.size(); i++) {
                SelectionRecord selection = selections.get(i);
                if (selection.studentId.equals(studentId) && selection.courseId.equals(courseId)) {
                    selections.remove(i);
                    return true;
                }
            }
            return false;
        }

        @Override
        public int countStudents() {
            return 10;
        }

        @Override
        public int countCourses() {
            return allCourses.size();
        }

        @Override
        public int countSelections() {
            return selections.size();
        }

        @Override
        public int countSharedCourses() {
            return sharedCourses.size();
        }

        @Override
        public List<CourseHeat> topCourses(int topN) {
            List<CourseHeat> result = new ArrayList<>();
            for (Course course : allCourses) {
                CourseHeat heat = new CourseHeat();
                heat.setCourseId(course.getId());
                heat.setCourseName(course.getName());
                heat.setCollege(collegeCode);
                heat.setSelectedCount(0);
                result.add(heat);
            }
            return result.subList(0, Math.min(topN, result.size()));
        }

        private boolean existsCourse(String courseId) {
            for (Course course : allCourses) {
                if (course.getId().equals(courseId)) {
                    return true;
                }
            }
            return false;
        }

        private Course createCourse(String id, boolean shared) {
            Course course = new Course();
            course.setId(id);
            course.setName(collegeCode + "-" + id);
            course.setCredit(3);
            course.setTeacher("Teacher-" + collegeCode);
            course.setLocation(collegeCode + "-Room");
            course.setCollege(collegeCode);
            course.setShared(shared);
            return course;
        }

        private Course copyCourse(Course source) {
            Course copy = new Course();
            copy.setId(source.getId());
            copy.setName(source.getName());
            copy.setCredit(source.getCredit());
            copy.setTeacher(source.getTeacher());
            copy.setLocation(source.getLocation());
            copy.setCollege(source.getCollege());
            copy.setShared(source.isShared());
            return copy;
        }
    }

    private static final class SelectionRecord {

        private final String studentId;
        private final String courseId;

        private SelectionRecord(String studentId, String courseId) {
            this.studentId = studentId;
            this.courseId = courseId;
        }
    }
}
