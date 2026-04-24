package edu.fusion.integration.service;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.model.GlobalStatistics;
import edu.fusion.common.model.Result;
import edu.fusion.common.service.CollegeGateway;
import edu.fusion.common.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegrationServer {

    private final Map<String, CollegeGateway> gateways;

    public IntegrationServer(List<CollegeGateway> gatewayList) {
        this.gateways = new HashMap<>();
        for (CollegeGateway gateway : gatewayList) {
            gateways.put(gateway.getCollegeCode(), gateway);
        }
    }

    public Result<List<Course>> shareCourses(String sourceCollege) {
        List<Course> result = new ArrayList<>();
        for (CollegeGateway gateway : gateways.values()) {
            if (!gateway.getCollegeCode().equalsIgnoreCase(sourceCollege)) {
                result.addAll(gateway.listSharedCourses());
            }
        }
        return Result.ok(result, "Shared courses fetched");
    }

    public Result<Boolean> crossSelect(String studentId, String courseId) {
        String targetCollege = resolveCollegeByCourseId(courseId);
        CollegeGateway gateway = gateways.get(targetCollege);
        if (gateway == null) {
            return Result.fail("Target college not found for course " + courseId);
        }
        boolean selected = gateway.selectCourse(studentId, courseId);
        if (!selected) {
            return Result.fail("Select failed, duplicate or unknown course");
        }
        return Result.ok(true, "Cross-college course selection succeeded");
    }

    public Result<Boolean> dropCourse(String studentId, String courseId) {
        String targetCollege = resolveCollegeByCourseId(courseId);
        CollegeGateway gateway = gateways.get(targetCollege);
        if (gateway == null) {
            return Result.fail("Target college not found for course " + courseId);
        }
        boolean dropped = gateway.dropCourse(studentId, courseId);
        if (!dropped) {
            return Result.fail("Drop failed, selection not found");
        }
        return Result.ok(true, "Course dropped successfully");
    }

    public Result<GlobalStatistics> statistics() {
        GlobalStatistics stats = new GlobalStatistics();
        List<CourseHeat> allHeats = new ArrayList<>();

        int totalStudents = 0;
        int totalCourses = 0;
        int totalSelections = 0;
        int totalShared = 0;

        for (CollegeGateway gateway : gateways.values()) {
            totalStudents += gateway.countStudents();
            totalCourses += gateway.countCourses();
            totalSelections += gateway.countSelections();
            totalShared += gateway.countSharedCourses();
            allHeats.addAll(gateway.topCourses(10));
        }

        allHeats.sort(Comparator.comparingInt(CourseHeat::getSelectedCount).reversed());

        stats.setTotalStudents(totalStudents);
        stats.setTotalCourses(totalCourses);
        stats.setTotalSelections(totalSelections);
        stats.setTotalSharedCourses(totalShared);
        stats.setTopCourses(allHeats.subList(0, Math.min(10, allHeats.size())));

        return Result.ok(stats, "Statistics generated");
    }

    public Result<List<Course>> queryCourses(String collegeCode) {
        CollegeGateway gateway = gateways.get(normalizeCollegeCode(collegeCode));
        if (gateway == null) {
            return Result.fail("College not found: " + collegeCode);
        }
        return Result.ok(gateway.listAllCourses(), "Courses fetched");
    }

    public Result<List<Course>> myCourses(String collegeCode, String studentId) {
        CollegeGateway gateway = gateways.get(normalizeCollegeCode(collegeCode));
        if (gateway == null) {
            return Result.fail("College not found: " + collegeCode);
        }
        return Result.ok(gateway.listStudentCourses(studentId), "Student courses fetched");
    }

    public Result<Document> processRequestXml(Path requestXml, Path requestXsd) {
        if (!XmlUtil.validateAgainstXsd(requestXml, requestXsd)) {
            return Result.fail("Request XML failed XSD validation");
        }

        Document req = XmlUtil.parse(requestXml);
        return processRequestXml(req);
    }

    public Result<Document> processRequestXml(String requestXml) {
        return processRequestXml(XmlUtil.parse(requestXml));
    }

    public Result<Document> processRequestXml(Document req) {
        Element root = req.getDocumentElement();
        String type = XmlUtil.childText(root, "type");

        switch (type) {
            case "shareCourse":
                return processShare(root);
            case "queryCourses":
                return processQueryCourses(root);
            case "myCourses":
                return processMyCourses(root);
            case "crossSelect":
                return processSelect(root);
            case "dropCourse":
                return processDrop(root);
            case "statistics":
                return processStats();
            default:
                return Result.fail("Unsupported request type: " + type);
        }
    }

    private Result<Document> processShare(Element root) {
        String source = XmlUtil.childText(root, "source");
        Result<List<Course>> shared = shareCourses(source);
        return buildCourseListDocument(shared, "shareCourse done");
    }

    private Result<Document> processQueryCourses(Element root) {
        String college = XmlUtil.childText(root, "college");
        Result<List<Course>> result = queryCourses(college);
        return buildCourseListDocument(result, "queryCourses done");
    }

    private Result<Document> processMyCourses(Element root) {
        String college = XmlUtil.childText(root, "college");
        String studentId = XmlUtil.childText(root, "studentId");
        Result<List<Course>> result = myCourses(college, studentId);
        return buildCourseListDocument(result, "myCourses done");
    }

    private Result<Document> processSelect(Element root) {
        String studentId = XmlUtil.childText(root, "studentId");
        String courseId = XmlUtil.childText(root, "courseId");
        Result<Boolean> result = crossSelect(studentId, courseId);

        Document response = XmlUtil.createDocument("response");
        Element responseRoot = response.getDocumentElement();
        responseRoot.appendChild(buildTextElement(response, "success", String.valueOf(result.isSuccess())));
        responseRoot.appendChild(buildTextElement(response, "message", result.getMessage()));
        return Result.ok(response, "crossSelect done");
    }

    private Result<Document> processDrop(Element root) {
        String studentId = XmlUtil.childText(root, "studentId");
        String courseId = XmlUtil.childText(root, "courseId");
        Result<Boolean> result = dropCourse(studentId, courseId);

        Document response = XmlUtil.createDocument("response");
        Element responseRoot = response.getDocumentElement();
        responseRoot.appendChild(buildTextElement(response, "success", String.valueOf(result.isSuccess())));
        responseRoot.appendChild(buildTextElement(response, "message", result.getMessage()));
        return Result.ok(response, "dropCourse done");
    }

    private Result<Document> processStats() {
        Result<GlobalStatistics> result = statistics();
        Document response = XmlUtil.createDocument("response");
        Element responseRoot = response.getDocumentElement();
        responseRoot.appendChild(buildTextElement(response, "success", String.valueOf(result.isSuccess())));
        responseRoot.appendChild(buildTextElement(response, "message", result.getMessage()));

        if (result.getData() != null) {
            GlobalStatistics stats = result.getData();
            Element statsElement = response.createElement("statistics");
            statsElement.appendChild(buildTextElement(response, "totalStudents", String.valueOf(stats.getTotalStudents())));
            statsElement.appendChild(buildTextElement(response, "totalCourses", String.valueOf(stats.getTotalCourses())));
            statsElement.appendChild(buildTextElement(response, "totalSelections", String.valueOf(stats.getTotalSelections())));
            statsElement.appendChild(buildTextElement(response, "totalSharedCourses", String.valueOf(stats.getTotalSharedCourses())));

            Element topCourses = response.createElement("topCourses");
            for (CourseHeat heat : stats.getTopCourses()) {
                Element c = response.createElement("course");
                c.appendChild(buildTextElement(response, "id", heat.getCourseId()));
                c.appendChild(buildTextElement(response, "name", heat.getCourseName()));
                c.appendChild(buildTextElement(response, "college", heat.getCollege()));
                c.appendChild(buildTextElement(response, "selectedCount", String.valueOf(heat.getSelectedCount())));
                topCourses.appendChild(c);
            }
            statsElement.appendChild(topCourses);
            responseRoot.appendChild(statsElement);
        }

        return Result.ok(response, "statistics done");
    }

    private Result<Document> buildCourseListDocument(Result<List<Course>> result, String successMessage) {
        Document response = XmlUtil.createDocument("response");
        Element responseRoot = response.getDocumentElement();
        responseRoot.appendChild(buildTextElement(response, "success", String.valueOf(result.isSuccess())));
        responseRoot.appendChild(buildTextElement(response, "message", result.getMessage()));

        Element coursesElement = response.createElement("courses");
        responseRoot.appendChild(coursesElement);
        if (result.getData() != null) {
            for (Course c : result.getData()) {
                Element courseElement = response.createElement("course");
                courseElement.appendChild(buildTextElement(response, "id", c.getId()));
                courseElement.appendChild(buildTextElement(response, "name", c.getName()));
                courseElement.appendChild(buildTextElement(response, "credit", String.valueOf(c.getCredit())));
                courseElement.appendChild(buildTextElement(response, "teacher", c.getTeacher()));
                courseElement.appendChild(buildTextElement(response, "location", c.getLocation()));
                courseElement.appendChild(buildTextElement(response, "college", c.getCollege()));
                courseElement.appendChild(buildTextElement(response, "shared", String.valueOf(c.isShared())));
                coursesElement.appendChild(courseElement);
            }
        }
        return Result.ok(response, successMessage);
    }

    private String resolveCollegeByCourseId(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            return "";
        }
        return String.valueOf(Character.toUpperCase(courseId.charAt(0)));
    }

    private String normalizeCollegeCode(String collegeCode) {
        return collegeCode == null ? "" : collegeCode.trim().toUpperCase();
    }

    private Element buildTextElement(Document doc, String name, String value) {
        Element element = doc.createElement(name);
        element.setTextContent(value == null ? "" : value);
        return element;
    }
}
