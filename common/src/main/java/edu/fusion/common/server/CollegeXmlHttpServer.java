package edu.fusion.common.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.model.Selection;
import edu.fusion.common.model.Student;
import edu.fusion.common.service.CollegeGateway;
import edu.fusion.common.util.AuditLogger;
import edu.fusion.common.util.Dom4jXmlService;
import edu.fusion.common.util.ErrorLogger;

public class CollegeXmlHttpServer {

    private final CollegeGateway gateway;
    private final int port;
    private HttpServer server;

    public CollegeXmlHttpServer(CollegeGateway gateway, int port) {
        this.gateway = gateway;
        this.port = port;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/xml", new CollegeXmlHandler());
            server.createContext("/api/health", exchange -> {
                byte[] resp = "OK".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, resp.length);
                exchange.getResponseBody().write(resp);
                exchange.getResponseBody().close();
            });
            server.setExecutor(null);
            server.start();
            System.out.println("[" + gateway.getCollegeCode()
                    + "] College XML HTTP server started at http://localhost:" + port + "/api/xml");
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Failed to start college HTTP server for " + gateway.getCollegeCode(), ex);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPort() {
        return port;
    }

    private final class CollegeXmlHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeError(exchange, 405, "Only POST is supported");
                return;
            }
            String requestXml = readBody(exchange.getRequestBody());
            String responseXml;
            try {
                Document req = Dom4jXmlService.parse(requestXml);
                responseXml = handleRequest(req);
            } catch (Exception ex) {
                ErrorLogger.log("college.http.handle", ex);
                responseXml = buildErrorXml("Request processing error: " + ex.getMessage());
            }
            writeXml(exchange, 200, responseXml);
        }
    }

    private String handleRequest(Document req) {
        Element root = req.getRootElement();
        String type = Dom4jXmlService.childText(root, "type");

        switch (type) {
            case "listAllCourses":
                List<Course> allCourses = gateway.listAllCourses();
                AuditLogger.log("listAllCourses", gateway.getCollegeCode(), "college=" + gateway.getCollegeCode(), true, "List all courses");
                return buildCourseListXml(allCourses);
            case "listSharedCourses":
                List<Course> sharedCourses = gateway.listSharedCourses();
                AuditLogger.log("listSharedCourses", gateway.getCollegeCode(), "college=" + gateway.getCollegeCode(), true, "List shared courses");
                return buildCourseListXml(sharedCourses);
            case "listStudentCourses": {
                String studentId = Dom4jXmlService.childText(root, "studentId");
                List<Course> courses = gateway.listStudentCourses(studentId);
                AuditLogger.log("listStudentCourses", studentId, "college=" + gateway.getCollegeCode(), true, "List student courses");
                return buildCourseListXml(courses);
            }
            case "selectCourse": {
                String sid = Dom4jXmlService.childText(root, "studentId");
                String cid = Dom4jXmlService.childText(root, "courseId");
                boolean ok = gateway.selectCourse(sid, cid);
                AuditLogger.log("selectCourse", sid, cid, ok, ok ? "Course selected" : "Select failed");
                return buildSimpleResultXml(ok, ok ? "Course selected" : "Select failed");
            }
            case "dropCourse": {
                String sid = Dom4jXmlService.childText(root, "studentId");
                String cid = Dom4jXmlService.childText(root, "courseId");
                boolean ok = gateway.dropCourse(sid, cid);
                AuditLogger.log("dropCourse", sid, cid, ok, ok ? "Course dropped" : "Drop failed");
                return buildSimpleResultXml(ok, ok ? "Course dropped" : "Drop failed");
            }
            case "authenticateStudent": {
                String username = Dom4jXmlService.childText(root, "username");
                String password = Dom4jXmlService.childText(root, "password");
                boolean ok = gateway.authenticateStudent(username, password);
                AuditLogger.log("authenticateStudent", username, "college=" + gateway.getCollegeCode(), ok, ok ? "Auth OK" : "Auth failed");
                return buildSimpleResultXml(ok, ok ? "Auth OK" : "Auth failed");
            }
            case "authenticateAdmin": {
                String username = Dom4jXmlService.childText(root, "username");
                String password = Dom4jXmlService.childText(root, "password");
                boolean ok = gateway.authenticateAdmin(username, password);
                AuditLogger.log("authenticateAdmin", username, "college=" + gateway.getCollegeCode(), ok, ok ? "Auth OK" : "Auth failed");
                return buildSimpleResultXml(ok, ok ? "Auth OK" : "Auth failed");
            }
            case "countStudents":
                return buildCountXml(gateway.countStudents());
            case "countCourses":
                return buildCountXml(gateway.countCourses());
            case "countSelections":
                return buildCountXml(gateway.countSelections());
            case "countSharedCourses":
                return buildCountXml(gateway.countSharedCourses());
            case "topCourses": {
                int topN = 10;
                String topStr = Dom4jXmlService.childText(root, "topN");
                if (!topStr.isEmpty()) {
                    topN = Integer.parseInt(topStr);
                }
                return buildTopCoursesXml(gateway.topCourses(topN));
            }
            case "listAllStudents": {
                List<Student> allStudents = gateway.listAllStudents();
                AuditLogger.log("listAllStudents", gateway.getCollegeCode(), "college=" + gateway.getCollegeCode(), true, "List all students");
                return buildStudentListXml(allStudents);
            }
            case "listAllSelections": {
                List<Selection> allSelections = gateway.listAllSelections();
                AuditLogger.log("listAllSelections", gateway.getCollegeCode(), "college=" + gateway.getCollegeCode(), true, "List all selections");
                return buildSelectionListXml(allSelections);
            }
            // ===== Admin CRUD =====
            case "adminAddStudent": {
                Student s = new Student();
                s.setId(Dom4jXmlService.childText(root, "id"));
                s.setName(Dom4jXmlService.childText(root, "name"));
                s.setSex(Dom4jXmlService.childText(root, "sex"));
                s.setMajor(Dom4jXmlService.childText(root, "major"));
                s.setCollege(gateway.getCollegeCode());
                boolean ok = gateway.addStudent(s);
                AuditLogger.log("adminAddStudent", gateway.getCollegeCode(), s.getId(), ok, ok ? "Added" : "Failed");
                return buildSimpleResultXml(ok, ok ? "Student added" : "Add failed (duplicate id?)");
            }
            case "adminUpdateStudent": {
                Student s = new Student();
                s.setId(Dom4jXmlService.childText(root, "id"));
                s.setName(Dom4jXmlService.childText(root, "name"));
                s.setSex(Dom4jXmlService.childText(root, "sex"));
                s.setMajor(Dom4jXmlService.childText(root, "major"));
                s.setCollege(gateway.getCollegeCode());
                boolean ok = gateway.updateStudent(s);
                AuditLogger.log("adminUpdateStudent", gateway.getCollegeCode(), s.getId(), ok, ok ? "Updated" : "Failed");
                return buildSimpleResultXml(ok, ok ? "Student updated" : "Update failed");
            }
            case "adminDeleteStudent": {
                String sid = Dom4jXmlService.childText(root, "id");
                boolean ok = gateway.deleteStudent(sid);
                AuditLogger.log("adminDeleteStudent", gateway.getCollegeCode(), sid, ok, ok ? "Deleted" : "Failed");
                return buildSimpleResultXml(ok, ok ? "Student deleted" : "Delete failed (has selections?)");
            }
            case "adminAddCourse": {
                Course c = new Course();
                c.setId(Dom4jXmlService.childText(root, "id"));
                c.setName(Dom4jXmlService.childText(root, "name"));
                c.setCredit(Integer.parseInt(Dom4jXmlService.childText(root, "credit")));
                c.setTeacher(Dom4jXmlService.childText(root, "teacher"));
                c.setLocation(Dom4jXmlService.childText(root, "location"));
                c.setCollege(gateway.getCollegeCode());
                c.setShared("true".equalsIgnoreCase(Dom4jXmlService.childText(root, "shared")));
                boolean ok = gateway.addCourse(c);
                AuditLogger.log("adminAddCourse", gateway.getCollegeCode(), c.getId(), ok, ok ? "Added" : "Failed");
                return buildSimpleResultXml(ok, ok ? "Course added" : "Add failed");
            }
            case "adminUpdateCourse": {
                Course c = new Course();
                c.setId(Dom4jXmlService.childText(root, "id"));
                c.setName(Dom4jXmlService.childText(root, "name"));
                c.setCredit(Integer.parseInt(Dom4jXmlService.childText(root, "credit")));
                c.setTeacher(Dom4jXmlService.childText(root, "teacher"));
                c.setLocation(Dom4jXmlService.childText(root, "location"));
                c.setCollege(gateway.getCollegeCode());
                c.setShared("true".equalsIgnoreCase(Dom4jXmlService.childText(root, "shared")));
                boolean ok = gateway.updateCourse(c);
                AuditLogger.log("adminUpdateCourse", gateway.getCollegeCode(), c.getId(), ok, ok ? "Updated" : "Failed");
                return buildSimpleResultXml(ok, ok ? "Course updated" : "Update failed");
            }
            case "adminDeleteCourse": {
                String cid = Dom4jXmlService.childText(root, "id");
                boolean ok = gateway.deleteCourse(cid);
                AuditLogger.log("adminDeleteCourse", gateway.getCollegeCode(), cid, ok, ok ? "Deleted" : "Failed");
                return buildSimpleResultXml(ok, ok ? "Course deleted" : "Delete failed (has selections?)");
            }
            case "adminUpdateScore": {
                String sid = Dom4jXmlService.childText(root, "studentId");
                String cid = Dom4jXmlService.childText(root, "courseId");
                int score = Integer.parseInt(Dom4jXmlService.childText(root, "score"));
                boolean ok = gateway.updateScore(sid, cid, score);
                AuditLogger.log("adminUpdateScore", gateway.getCollegeCode(), sid + "/" + cid, ok, "score=" + score);
                return buildSimpleResultXml(ok, ok ? "Score updated" : "Update failed");
            }
            case "adminAuditLog": {
                int limit = 50;
                String limitStr = Dom4jXmlService.childText(root, "limit");
                if (!limitStr.isEmpty()) limit = Integer.parseInt(limitStr);
                String logs = gateway.getAuditLogs(limit);
                return buildAuditLogXml(logs);
            }
            default:
                return buildErrorXml("Unsupported request type: " + type);
        }
    }

    private String buildCourseListXml(List<Course> courses) {
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "true");
        Dom4jXmlService.addTextElement(root, "college", gateway.getCollegeCode());
        Element coursesEl = root.addElement("courses");
        for (Course c : courses) {
            Element courseEl = coursesEl.addElement("course");
            Dom4jXmlService.addTextElement(courseEl, "id", c.getId());
            Dom4jXmlService.addTextElement(courseEl, "name", c.getName());
            Dom4jXmlService.addTextElement(courseEl, "credit", String.valueOf(c.getCredit()));
            Dom4jXmlService.addTextElement(courseEl, "teacher", c.getTeacher());
            Dom4jXmlService.addTextElement(courseEl, "location", c.getLocation());
            Dom4jXmlService.addTextElement(courseEl, "college", c.getCollege());
            Dom4jXmlService.addTextElement(courseEl, "shared", String.valueOf(c.isShared()));
        }
        return Dom4jXmlService.toCompactString(doc);
    }

    private String buildSimpleResultXml(boolean success, String message) {
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", String.valueOf(success));
        Dom4jXmlService.addTextElement(root, "message", message);
        return Dom4jXmlService.toCompactString(doc);
    }

    private String buildCountXml(int count) {
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "true");
        Dom4jXmlService.addTextElement(root, "count", String.valueOf(count));
        return Dom4jXmlService.toCompactString(doc);
    }

    private String buildTopCoursesXml(List<CourseHeat> heats) {
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "true");
        Dom4jXmlService.addTextElement(root, "college", gateway.getCollegeCode());
        Element coursesEl = root.addElement("courses");
        for (CourseHeat h : heats) {
            Element c = coursesEl.addElement("course");
            Dom4jXmlService.addTextElement(c, "id", h.getCourseId());
            Dom4jXmlService.addTextElement(c, "name", h.getCourseName());
            Dom4jXmlService.addTextElement(c, "college", h.getCollege());
            Dom4jXmlService.addTextElement(c, "selectedCount", String.valueOf(h.getSelectedCount()));
        }
        return Dom4jXmlService.toCompactString(doc);
    }

    private String buildStudentListXml(List<Student> students) {
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "true");
        Dom4jXmlService.addTextElement(root, "college", gateway.getCollegeCode());
        Element studentsEl = root.addElement("students");
        for (Student s : students) {
            Element studentEl = studentsEl.addElement("student");
            Dom4jXmlService.addTextElement(studentEl, "id", s.getId());
            Dom4jXmlService.addTextElement(studentEl, "name", s.getName());
            Dom4jXmlService.addTextElement(studentEl, "sex", s.getSex());
            Dom4jXmlService.addTextElement(studentEl, "major", s.getMajor());
            Dom4jXmlService.addTextElement(studentEl, "college", s.getCollege());
        }
        return Dom4jXmlService.toCompactString(doc);
    }

    private String buildSelectionListXml(List<Selection> selections) {
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "true");
        Dom4jXmlService.addTextElement(root, "college", gateway.getCollegeCode());
        Element selectionsEl = root.addElement("selections");
        for (Selection s : selections) {
            Element selectionEl = selectionsEl.addElement("selection");
            Dom4jXmlService.addTextElement(selectionEl, "studentId", s.getStudentId());
            Dom4jXmlService.addTextElement(selectionEl, "courseId", s.getCourseId());
            Dom4jXmlService.addTextElement(selectionEl, "college", s.getOwnerCollege());
            Dom4jXmlService.addTextElement(selectionEl, "score", s.getScore() == null ? "" : String.valueOf(s.getScore()));
        }
        return Dom4jXmlService.toCompactString(doc);
    }

    private String buildAuditLogXml(String logs) {
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "true");
        Dom4jXmlService.addTextElement(root, "logs", logs);
        return Dom4jXmlService.toCompactString(doc);
    }

    private String buildErrorXml(String message) {
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "false");
        Dom4jXmlService.addTextElement(root, "message", message);
        return Dom4jXmlService.toCompactString(doc);
    }

    private String readBody(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[8192];
        StringBuilder builder = new StringBuilder();
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            builder.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private void writeXml(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=UTF-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        try {
            os.write(bytes);
        } finally {
            os.close();
        }
    }

    private void writeError(HttpExchange exchange, int statusCode, String msg) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        try {
            os.write(bytes);
        } finally {
            os.close();
        }
    }
}
