package edu.fusion.common.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.service.CollegeGateway;
import edu.fusion.common.util.Dom4jXmlService;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
                return buildCourseListXml(gateway.listAllCourses());
            case "listSharedCourses":
                return buildCourseListXml(gateway.listSharedCourses());
            case "listStudentCourses": {
                String studentId = Dom4jXmlService.childText(root, "studentId");
                return buildCourseListXml(gateway.listStudentCourses(studentId));
            }
            case "selectCourse": {
                String sid = Dom4jXmlService.childText(root, "studentId");
                String cid = Dom4jXmlService.childText(root, "courseId");
                boolean ok = gateway.selectCourse(sid, cid);
                return buildSimpleResultXml(ok, ok ? "Course selected" : "Select failed");
            }
            case "dropCourse": {
                String sid = Dom4jXmlService.childText(root, "studentId");
                String cid = Dom4jXmlService.childText(root, "courseId");
                boolean ok = gateway.dropCourse(sid, cid);
                return buildSimpleResultXml(ok, ok ? "Course dropped" : "Drop failed");
            }
            case "authenticateStudent": {
                String username = Dom4jXmlService.childText(root, "username");
                String password = Dom4jXmlService.childText(root, "password");
                boolean ok = gateway.authenticateStudent(username, password);
                return buildSimpleResultXml(ok, ok ? "Auth OK" : "Auth failed");
            }
            case "authenticateAdmin": {
                String username = Dom4jXmlService.childText(root, "username");
                String password = Dom4jXmlService.childText(root, "password");
                boolean ok = gateway.authenticateAdmin(username, password);
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
