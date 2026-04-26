package edu.fusion.common.server;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.service.CollegeGateway;
import edu.fusion.common.service.RemoteCollegeGateway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollegeXmlHttpServerTest {

    private CollegeXmlHttpServer server;
    private RemoteCollegeGateway remote;
    private StubGateway stub;
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        stub = new StubGateway();
        port = findFreePort();
        server = new CollegeXmlHttpServer(stub, port);
        server.start();
        Thread.sleep(100);
        remote = new RemoteCollegeGateway(stub.getCollegeCode(),
                "http://localhost:" + port + "/api/xml", 3000, 3000);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void listAllCoursesShouldReturnAllCourses() {
        List<Course> courses = remote.listAllCourses();
        assertEquals(2, courses.size());
        assertEquals("A101", courses.get(0).getId());
        assertEquals("A102", courses.get(1).getId());
    }

    @Test
    void listSharedCoursesShouldReturnOnlyShared() {
        List<Course> courses = remote.listSharedCourses();
        assertEquals(1, courses.size());
        assertEquals("A101", courses.get(0).getId());
        assertTrue(courses.get(0).isShared());
    }

    @Test
    void listStudentCoursesShouldReturnSelectedCourses() {
        stub.selections.add("A001:A101");
        List<Course> courses = remote.listStudentCourses("A001");
        assertEquals(1, courses.size());
        assertEquals("A101", courses.get(0).getId());
    }

    @Test
    void selectCourseShouldSucceed() {
        assertTrue(remote.selectCourse("A001", "A102"));
        assertTrue(stub.selections.contains("A001:A102"));
    }

    @Test
    void selectDuplicateCourseShouldFail() {
        assertTrue(remote.selectCourse("A001", "A101"));
        assertFalse(remote.selectCourse("A001", "A101"));
    }

    @Test
    void dropCourseShouldSucceed() {
        remote.selectCourse("A001", "A102");
        assertTrue(remote.dropCourse("A001", "A102"));
        assertFalse(stub.selections.contains("A001:A102"));
    }

    @Test
    void countMethodsShouldReturnCorrectValues() {
        assertEquals(10, remote.countStudents());
        assertEquals(2, remote.countCourses());
        assertEquals(0, remote.countSelections());
        assertEquals(1, remote.countSharedCourses());
    }

    @Test
    void topCoursesShouldReturnHeatData() {
        List<CourseHeat> heats = remote.topCourses(10);
        assertEquals(2, heats.size());
        assertEquals("A101", heats.get(0).getCourseId());
        assertEquals("A", heats.get(0).getCollege());
    }

    @Test
    void healthEndpointShouldReturnOk() throws Exception {
        java.net.URL url = new java.net.URL("http://localhost:" + port + "/api/health");
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        String body = new String(readAllBytes(conn.getInputStream()), java.nio.charset.StandardCharsets.UTF_8).trim();
        conn.disconnect();
        assertEquals(200, responseCode);
        assertEquals("OK", body);
    }

    private byte[] readAllBytes(java.io.InputStream is) throws Exception {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int n;
        while ((n = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }

    private int findFreePort() throws Exception {
        java.net.ServerSocket socket = new java.net.ServerSocket(0);
        int freePort = socket.getLocalPort();
        socket.close();
        return freePort;
    }

    private static final class StubGateway implements CollegeGateway {

        private final List<String> selections = new ArrayList<>();

        @Override
        public String getCollegeCode() {
            return "A";
        }

        @Override
        public boolean authenticateStudent(String username, String password) {
            return "A001".equals(username) && "123456".equals(password);
        }

        @Override
        public boolean authenticateAdmin(String username, String password) {
            return "adminA".equals(username) && "admin123".equals(password);
        }

        @Override
        public List<Course> listAllCourses() {
            return Arrays.asList(
                    new Course("A101", "Math", 4, "T1", "R1", "A", true),
                    new Course("A102", "Physics", 3, "T2", "R2", "A", false)
            );
        }

        @Override
        public List<Course> listSharedCourses() {
            return Collections.singletonList(
                    new Course("A101", "Math", 4, "T1", "R1", "A", true)
            );
        }

        @Override
        public List<Course> listStudentCourses(String studentId) {
            List<Course> result = new ArrayList<>();
            for (String sel : selections) {
                String[] parts = sel.split(":");
                if (parts[0].equals(studentId)) {
                    for (Course c : listAllCourses()) {
                        if (c.getId().equals(parts[1])) {
                            result.add(c);
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public boolean selectCourse(String studentId, String courseId) {
            String key = studentId + ":" + courseId;
            if (selections.contains(key)) {
                return false;
            }
            selections.add(key);
            return true;
        }

        @Override
        public boolean dropCourse(String studentId, String courseId) {
            return selections.remove(studentId + ":" + courseId);
        }

        @Override
        public int countStudents() {
            return 10;
        }

        @Override
        public int countCourses() {
            return 2;
        }

        @Override
        public int countSelections() {
            return selections.size();
        }

        @Override
        public int countSharedCourses() {
            return 1;
        }

        @Override
        public List<CourseHeat> topCourses(int topN) {
            CourseHeat h1 = new CourseHeat();
            h1.setCourseId("A101");
            h1.setCourseName("Math");
            h1.setCollege("A");
            h1.setSelectedCount(5);
            CourseHeat h2 = new CourseHeat();
            h2.setCourseId("A102");
            h2.setCourseName("Physics");
            h2.setCollege("A");
            h2.setSelectedCount(3);
            return Arrays.asList(h1, h2);
        }
    }
}
