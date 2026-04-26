package edu.fusion.common.service;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.util.Dom4jXmlService;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RemoteCollegeGateway implements CollegeGateway {

    private final String collegeCode;
    private final String serviceUrl;
    private final int connectTimeout;
    private final int readTimeout;

    public RemoteCollegeGateway(String collegeCode, String serviceUrl) {
        this(collegeCode, serviceUrl, 5000, 15000);
    }

    public RemoteCollegeGateway(String collegeCode, String serviceUrl,
                                 int connectTimeout, int readTimeout) {
        this.collegeCode = collegeCode;
        this.serviceUrl = serviceUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public String getCollegeCode() {
        return collegeCode;
    }

    @Override
    public boolean authenticateStudent(String username, String password) {
        Document req = buildSimpleRequest("authenticateStudent", "username", username, "password", password);
        return parseBoolResponse(postXml(req));
    }

    @Override
    public boolean authenticateAdmin(String username, String password) {
        Document req = buildSimpleRequest("authenticateAdmin", "username", username, "password", password);
        return parseBoolResponse(postXml(req));
    }

    @Override
    public List<Course> listAllCourses() {
        Document req = buildSimpleRequest("listAllCourses");
        return parseCourseListResponse(postXml(req));
    }

    @Override
    public List<Course> listSharedCourses() {
        Document req = buildSimpleRequest("listSharedCourses");
        return parseCourseListResponse(postXml(req));
    }

    @Override
    public List<Course> listStudentCourses(String studentId) {
        Document req = buildSimpleRequest("listStudentCourses", "studentId", studentId);
        return parseCourseListResponse(postXml(req));
    }

    @Override
    public boolean selectCourse(String studentId, String courseId) {
        Document req = buildSimpleRequest("selectCourse",
                "studentId", studentId, "courseId", courseId);
        return parseBoolResponse(postXml(req));
    }

    @Override
    public boolean dropCourse(String studentId, String courseId) {
        Document req = buildSimpleRequest("dropCourse",
                "studentId", studentId, "courseId", courseId);
        return parseBoolResponse(postXml(req));
    }

    @Override
    public int countStudents() {
        return parseIntResponse(postXml(buildSimpleRequest("countStudents")));
    }

    @Override
    public int countCourses() {
        return parseIntResponse(postXml(buildSimpleRequest("countCourses")));
    }

    @Override
    public int countSelections() {
        return parseIntResponse(postXml(buildSimpleRequest("countSelections")));
    }

    @Override
    public int countSharedCourses() {
        return parseIntResponse(postXml(buildSimpleRequest("countSharedCourses")));
    }

    @Override
    public List<CourseHeat> topCourses(int topN) {
        Document req = buildSimpleRequest("topCourses", "topN", String.valueOf(topN));
        return parseCourseHeatResponse(postXml(req));
    }

    // ========== XML helpers ==========

    private Document buildSimpleRequest(String type, String... keyValues) {
        Document doc = Dom4jXmlService.createDocument("request");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "type", type);
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            Dom4jXmlService.addTextElement(root, keyValues[i], keyValues[i + 1]);
        }
        return doc;
    }

    private String postXml(Document request) {
        HttpURLConnection connection = null;
        try {
            String xml = Dom4jXmlService.toCompactString(request);
            connection = (HttpURLConnection) new URL(serviceUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/xml");
            connection.setDoOutput(true);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            OutputStream os = connection.getOutputStream();
            try {
                os.write(xml.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } finally {
                os.close();
            }

            InputStream is = connection.getResponseCode() >= 400
                    ? connection.getErrorStream() : connection.getInputStream();
            if (is == null) {
                return "";
            }
            return readAll(is);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Failed to call college service " + collegeCode + " at " + serviceUrl, ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean parseBoolResponse(String xml) {
        if (xml == null || xml.isEmpty()) {
            return false;
        }
        Document doc = Dom4jXmlService.parse(xml);
        return "true".equalsIgnoreCase(
                Dom4jXmlService.childText(doc.getRootElement(), "success"));
    }

    private int parseIntResponse(String xml) {
        if (xml == null || xml.isEmpty()) {
            return 0;
        }
        Document doc = Dom4jXmlService.parse(xml);
        String count = Dom4jXmlService.childText(doc.getRootElement(), "count");
        return count.isEmpty() ? 0 : Integer.parseInt(count);
    }

    private List<Course> parseCourseListResponse(String xml) {
        List<Course> courses = new ArrayList<>();
        if (xml == null || xml.isEmpty()) {
            return courses;
        }
        Document doc = Dom4jXmlService.parse(xml);
        Element root = doc.getRootElement();
        Element coursesEl = root.element("courses");
        if (coursesEl == null) {
            return courses;
        }
        @SuppressWarnings("unchecked")
        List<Element> courseElements = coursesEl.elements("course");
        for (Element el : courseElements) {
            Course c = new Course();
            c.setId(textOf(el, "id"));
            c.setName(textOf(el, "name"));
            c.setCredit(intOf(el, "credit"));
            c.setTeacher(textOf(el, "teacher"));
            c.setLocation(textOf(el, "location"));
            c.setCollege(textOf(el, "college"));
            c.setShared("true".equalsIgnoreCase(textOf(el, "shared")));
            courses.add(c);
        }
        return courses;
    }

    private List<CourseHeat> parseCourseHeatResponse(String xml) {
        List<CourseHeat> heats = new ArrayList<>();
        if (xml == null || xml.isEmpty()) {
            return heats;
        }
        Document doc = Dom4jXmlService.parse(xml);
        Element root = doc.getRootElement();
        Element coursesEl = root.element("courses");
        if (coursesEl == null) {
            return heats;
        }
        @SuppressWarnings("unchecked")
        List<Element> courseElements = coursesEl.elements("course");
        for (Element el : courseElements) {
            CourseHeat h = new CourseHeat();
            h.setCourseId(textOf(el, "id"));
            h.setCourseName(textOf(el, "name"));
            h.setCollege(textOf(el, "college"));
            h.setSelectedCount(intOf(el, "selectedCount"));
            heats.add(h);
        }
        return heats;
    }

    private String textOf(Element parent, String tag) {
        Element child = parent.element(tag);
        return child == null ? "" : child.getText();
    }

    private int intOf(Element parent, String tag) {
        String text = textOf(parent, tag);
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    private String readAll(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }
}
