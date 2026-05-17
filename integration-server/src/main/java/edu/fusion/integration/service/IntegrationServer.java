package edu.fusion.integration.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.model.GlobalStatistics;
import edu.fusion.common.model.Result;
import edu.fusion.common.service.CollegeGateway;
import edu.fusion.common.util.AuditLogger;
import edu.fusion.common.util.Dom4jXmlService;
import edu.fusion.common.util.ErrorLogger;

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
        Result<List<Course>> response = Result.ok(result, "Shared courses fetched");
        AuditLogger.log("shareCourse", sourceCollege, "sharedCourses", response.isSuccess(), response.getMessage());
        return response;
    }

    public Result<Boolean> crossSelect(String studentId, String courseId) {
        String targetCollege = resolveCollegeByCourseId(courseId);
        CollegeGateway gateway = gateways.get(targetCollege);
        if (gateway == null) {
            Result<Boolean> response = Result.fail("Target college not found for course " + courseId);
            AuditLogger.log("crossSelect", studentId, courseId, false, response.getMessage());
            return response;
        }
        boolean selected = gateway.selectCourse(studentId, courseId);
        if (!selected) {
            Result<Boolean> response = Result.fail("Select failed, duplicate or unknown course");
            AuditLogger.log("crossSelect", studentId, courseId, false, response.getMessage());
            return response;
        }
        Result<Boolean> response = Result.ok(true, "Cross-college course selection succeeded");
        AuditLogger.log("crossSelect", studentId, courseId, true, response.getMessage());
        return response;
    }

    public Result<Boolean> dropCourse(String studentId, String courseId) {
        String targetCollege = resolveCollegeByCourseId(courseId);
        CollegeGateway gateway = gateways.get(targetCollege);
        if (gateway == null) {
            Result<Boolean> response = Result.fail("Target college not found for course " + courseId);
            AuditLogger.log("dropCourse", studentId, courseId, false, response.getMessage());
            return response;
        }
        boolean dropped = gateway.dropCourse(studentId, courseId);
        if (!dropped) {
            Result<Boolean> response = Result.fail("Drop failed, selection not found");
            AuditLogger.log("dropCourse", studentId, courseId, false, response.getMessage());
            return response;
        }
        Result<Boolean> response = Result.ok(true, "Course dropped successfully");
        AuditLogger.log("dropCourse", studentId, courseId, true, response.getMessage());
        return response;
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
        Result<GlobalStatistics> response = Result.ok(stats, "Statistics generated");
        AuditLogger.log("statistics", "system", "global", response.isSuccess(), response.getMessage());
        return response;
    }

    public Result<List<Course>> queryCourses(String collegeCode) {
        CollegeGateway gateway = gateways.get(normalizeCollegeCode(collegeCode));
        if (gateway == null) {
            Result<List<Course>> response = Result.fail("College not found: " + collegeCode);
            AuditLogger.log("queryCourses", collegeCode, "college=" + collegeCode, false, response.getMessage());
            return response;
        }
        Result<List<Course>> response = Result.ok(gateway.listAllCourses(), "Courses fetched");
        AuditLogger.log("queryCourses", collegeCode, "college=" + collegeCode, response.isSuccess(), response.getMessage());
        return response;
    }

    public Result<List<Course>> myCourses(String collegeCode, String studentId) {
        CollegeGateway gateway = gateways.get(normalizeCollegeCode(collegeCode));
        if (gateway == null) {
            Result<List<Course>> response = Result.fail("College not found: " + collegeCode);
            AuditLogger.log("myCourses", studentId, "college=" + collegeCode, false, response.getMessage());
            return response;
        }
        Result<List<Course>> response = Result.ok(gateway.listStudentCourses(studentId), "Student courses fetched");
        AuditLogger.log("myCourses", studentId, "college=" + collegeCode, response.isSuccess(), response.getMessage());
        return response;
    }

    @Deprecated
    public Result<Document> processRequestXml(Path requestXml, Path requestXsd) {
        if (!Dom4jXmlService.validateAgainstXsd(requestXml, requestXsd)) {
            return Result.fail("Request XML failed XSD validation");
        }
        Document req = Dom4jXmlService.parse(requestXml);
        return processRequestXml(req);
    }

    public Result<Document> processRequestXml(String requestXml) {
        Path xsdPath = resolveXsdPath();
        if (xsdPath != null && !Dom4jXmlService.validateAgainstXsd(requestXml, xsdPath)) {
            return Result.fail("Request XML failed XSD validation");
        }
        return processRequestXml(Dom4jXmlService.parse(requestXml));
    }

    private Path resolveXsdPath() {
        Path fsPath = Paths.get("xsd", "request.xsd");
        if (fsPath.toFile().exists()) {
            return fsPath;
        }
        java.net.URL resource = getClass().getClassLoader().getResource("xsd/request.xsd");
        if (resource != null && "file".equals(resource.getProtocol())) {
            try {
                return Paths.get(resource.toURI());
            } catch (java.net.URISyntaxException e) {
                ErrorLogger.log("integration.resolveXsdPath", "Invalid XSD resource URI: " + resource, e);
            }
        }
        ErrorLogger.log("integration.resolveXsdPath", new IllegalStateException("request.xsd not found"));
        return null;
    }

    public Result<Document> processRequestXml(Document req) {
        Element root = req.getRootElement();
        String type = Dom4jXmlService.childText(root, "type");

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
        String source = Dom4jXmlService.childText(root, "source");
        Result<List<Course>> shared = shareCourses(source);
        return buildCourseListDocument(shared, "shareCourse done");
    }

    private Result<Document> processQueryCourses(Element root) {
        String college = Dom4jXmlService.childText(root, "college");
        Result<List<Course>> result = queryCourses(college);
        return buildCourseListDocument(result, "queryCourses done");
    }

    private Result<Document> processMyCourses(Element root) {
        String college = Dom4jXmlService.childText(root, "college");
        String studentId = Dom4jXmlService.childText(root, "studentId");
        Result<List<Course>> result = myCourses(college, studentId);
        return buildCourseListDocument(result, "myCourses done");
    }

    private Result<Document> processSelect(Element root) {
        String studentId = Dom4jXmlService.childText(root, "studentId");
        String courseId = Dom4jXmlService.childText(root, "courseId");
        Result<Boolean> result = crossSelect(studentId, courseId);

        Document response = Dom4jXmlService.createDocument("response");
        Element responseRoot = response.getRootElement();
        Dom4jXmlService.addTextElement(responseRoot, "success", String.valueOf(result.isSuccess()));
        Dom4jXmlService.addTextElement(responseRoot, "message", result.getMessage());
        return Result.ok(response, "crossSelect done");
    }

    private Result<Document> processDrop(Element root) {
        String studentId = Dom4jXmlService.childText(root, "studentId");
        String courseId = Dom4jXmlService.childText(root, "courseId");
        Result<Boolean> result = dropCourse(studentId, courseId);

        Document response = Dom4jXmlService.createDocument("response");
        Element responseRoot = response.getRootElement();
        Dom4jXmlService.addTextElement(responseRoot, "success", String.valueOf(result.isSuccess()));
        Dom4jXmlService.addTextElement(responseRoot, "message", result.getMessage());
        return Result.ok(response, "dropCourse done");
    }

    private Result<Document> processStats() {
        Result<GlobalStatistics> result = statistics();
        Document response = Dom4jXmlService.createDocument("response");
        Element responseRoot = response.getRootElement();
        Dom4jXmlService.addTextElement(responseRoot, "success", String.valueOf(result.isSuccess()));
        Dom4jXmlService.addTextElement(responseRoot, "message", result.getMessage());

        if (result.getData() != null) {
            GlobalStatistics stats = result.getData();

            Element statsElement = responseRoot.addElement("statistics");
            Dom4jXmlService.addTextElement(statsElement, "totalStudents", String.valueOf(stats.getTotalStudents()));
            Dom4jXmlService.addTextElement(statsElement, "totalCourses", String.valueOf(stats.getTotalCourses()));
            Dom4jXmlService.addTextElement(statsElement, "totalSelections", String.valueOf(stats.getTotalSelections()));
            Dom4jXmlService.addTextElement(statsElement, "totalSharedCourses", String.valueOf(stats.getTotalSharedCourses()));

            Element topCourses = statsElement.addElement("topCourses");
            for (CourseHeat heat : stats.getTopCourses()) {
                Element c = topCourses.addElement("course");
                Dom4jXmlService.addTextElement(c, "id", heat.getCourseId());
                Dom4jXmlService.addTextElement(c, "name", heat.getCourseName());
                Dom4jXmlService.addTextElement(c, "college", heat.getCollege());
                Dom4jXmlService.addTextElement(c, "selectedCount", String.valueOf(heat.getSelectedCount()));
            }

            // 添加三学院分项数据
            Element collegesElement = statsElement.addElement("colleges");
            for (CollegeGateway gateway : gateways.values()) {
                Element collegeElement = collegesElement.addElement("college");
                Dom4jXmlService.addTextElement(collegeElement, "code", gateway.getCollegeCode());
                Dom4jXmlService.addTextElement(collegeElement, "students", String.valueOf(gateway.countStudents()));
                Dom4jXmlService.addTextElement(collegeElement, "courses", String.valueOf(gateway.countCourses()));
                Dom4jXmlService.addTextElement(collegeElement, "selections", String.valueOf(gateway.countSelections()));
                Dom4jXmlService.addTextElement(collegeElement, "sharedCourses", String.valueOf(gateway.countSharedCourses()));
            }
        }

        return Result.ok(response, "statistics done");
    }

    private Result<Document> buildCourseListDocument(Result<List<Course>> result, String successMessage) {
        Document response = Dom4jXmlService.createDocument("response");
        Element responseRoot = response.getRootElement();
        Dom4jXmlService.addTextElement(responseRoot, "success", String.valueOf(result.isSuccess()));
        Dom4jXmlService.addTextElement(responseRoot, "message", result.getMessage());

        Element coursesElement = responseRoot.addElement("courses");
        if (result.getData() != null) {
            for (Course c : result.getData()) {
                Element courseElement = coursesElement.addElement("course");
                Dom4jXmlService.addTextElement(courseElement, "id", c.getId());
                Dom4jXmlService.addTextElement(courseElement, "name", c.getName());
                Dom4jXmlService.addTextElement(courseElement, "credit", String.valueOf(c.getCredit()));
                Dom4jXmlService.addTextElement(courseElement, "teacher", c.getTeacher());
                Dom4jXmlService.addTextElement(courseElement, "location", c.getLocation());
                Dom4jXmlService.addTextElement(courseElement, "college", c.getCollege());
                Dom4jXmlService.addTextElement(courseElement, "shared", String.valueOf(c.isShared()));
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
}
