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
import edu.fusion.common.model.Selection;
import edu.fusion.common.model.Student;
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
        // 只允许选择目标学院标记为共享的课程
        boolean isShared = gateway.listSharedCourses().stream()
                .anyMatch(c -> courseId.equals(c.getId()));
        if (!isShared) {
            Result<Boolean> response = Result.fail("Course " + courseId + " is not shared, cross-college selection not allowed");
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
        List<Course> allCourses = new ArrayList<>();
        for (CollegeGateway gateway : gateways.values()) {
            try {
                allCourses.addAll(gateway.listStudentCourses(studentId));
            } catch (Exception e) {
                ErrorLogger.log("integration.myCourses", "Error querying college " + gateway.getCollegeCode() + " for student " + studentId, e);
            }
        }
        Result<List<Course>> response = Result.ok(allCourses, "Student courses fetched from all colleges");
        AuditLogger.log("myCourses", studentId, "college=" + normalizeCollegeCode(collegeCode), response.isSuccess(), response.getMessage());
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
            // ===== Admin CRUD =====
            case "adminAddStudent":
                return processAdminAddStudent(root);
            case "adminUpdateStudent":
                return processAdminUpdateStudent(root);
            case "adminDeleteStudent":
                return processAdminDeleteStudent(root);
            case "adminAddCourse":
                return processAdminAddCourse(root);
            case "adminUpdateCourse":
                return processAdminUpdateCourse(root);
            case "adminDeleteCourse":
                return processAdminDeleteCourse(root);
            case "adminUpdateScore":
                return processAdminUpdateScore(root);
            case "adminListStudents":
                return processAdminListStudents(root);
            case "adminListSelections":
                return processAdminListSelections(root);
            case "adminAuditLog":
                return processAdminAuditLog(root);
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

            // 全课程列表（含教师、学分等信息，供客户端筛选和绘图）
            Element allCoursesElement = statsElement.addElement("allCourses");
            for (CollegeGateway gateway : gateways.values()) {
                List<Course> courses = gateway.listAllCourses();
                for (Course c : courses) {
                    Element ce = allCoursesElement.addElement("course");
                    Dom4jXmlService.addTextElement(ce, "id", c.getId());
                    Dom4jXmlService.addTextElement(ce, "name", c.getName());
                    Dom4jXmlService.addTextElement(ce, "credit", String.valueOf(c.getCredit()));
                    Dom4jXmlService.addTextElement(ce, "teacher", c.getTeacher());
                    Dom4jXmlService.addTextElement(ce, "location", c.getLocation());
                    Dom4jXmlService.addTextElement(ce, "college", c.getCollege());
                    Dom4jXmlService.addTextElement(ce, "shared", String.valueOf(c.isShared()));
                }
            }
        }

        return Result.ok(response, "statistics done");
    }

    // ===== Admin CRUD handlers =====

    private Result<Document> processAdminAddStudent(Element root) {
        String college = normalizeCollegeCode(Dom4jXmlService.childText(root, "college"));
        CollegeGateway gateway = gateways.get(college);
        if (gateway == null) return buildSimpleResponse(false, "College not found: " + college);
        Student s = new Student();
        s.setId(Dom4jXmlService.childText(root, "id"));
        s.setName(Dom4jXmlService.childText(root, "name"));
        s.setSex(Dom4jXmlService.childText(root, "sex"));
        s.setMajor(Dom4jXmlService.childText(root, "major"));
        s.setCollege(college);
        boolean ok = gateway.addStudent(s);
        AuditLogger.log("adminAddStudent", college, s.getId(), ok, ok ? "Added" : "Failed");
        return buildSimpleResponse(ok, ok ? "Student added" : "Add failed");
    }

    private Result<Document> processAdminUpdateStudent(Element root) {
        String college = normalizeCollegeCode(Dom4jXmlService.childText(root, "college"));
        CollegeGateway gateway = gateways.get(college);
        if (gateway == null) return buildSimpleResponse(false, "College not found: " + college);
        Student s = new Student();
        s.setId(Dom4jXmlService.childText(root, "id"));
        s.setName(Dom4jXmlService.childText(root, "name"));
        s.setSex(Dom4jXmlService.childText(root, "sex"));
        s.setMajor(Dom4jXmlService.childText(root, "major"));
        s.setCollege(college);
        boolean ok = gateway.updateStudent(s);
        AuditLogger.log("adminUpdateStudent", college, s.getId(), ok, ok ? "Updated" : "Failed");
        return buildSimpleResponse(ok, ok ? "Student updated" : "Update failed");
    }

    private Result<Document> processAdminDeleteStudent(Element root) {
        String college = normalizeCollegeCode(Dom4jXmlService.childText(root, "college"));
        CollegeGateway gateway = gateways.get(college);
        if (gateway == null) return buildSimpleResponse(false, "College not found: " + college);
        String id = Dom4jXmlService.childText(root, "id");
        boolean ok = gateway.deleteStudent(id);
        AuditLogger.log("adminDeleteStudent", college, id, ok, ok ? "Deleted" : "Failed");
        return buildSimpleResponse(ok, ok ? "Student deleted" : "Delete failed");
    }

    private Result<Document> processAdminAddCourse(Element root) {
        String college = normalizeCollegeCode(Dom4jXmlService.childText(root, "college"));
        CollegeGateway gateway = gateways.get(college);
        if (gateway == null) return buildSimpleResponse(false, "College not found: " + college);
        Course c = new Course();
        c.setId(Dom4jXmlService.childText(root, "id"));
        c.setName(Dom4jXmlService.childText(root, "name"));
        c.setCredit(Integer.parseInt(Dom4jXmlService.childText(root, "credit")));
        c.setTeacher(Dom4jXmlService.childText(root, "teacher"));
        c.setLocation(Dom4jXmlService.childText(root, "location"));
        c.setCollege(college);
        c.setShared("true".equalsIgnoreCase(Dom4jXmlService.childText(root, "shared")));
        boolean ok = gateway.addCourse(c);
        AuditLogger.log("adminAddCourse", college, c.getId(), ok, ok ? "Added" : "Failed");
        return buildSimpleResponse(ok, ok ? "Course added" : "Add failed");
    }

    private Result<Document> processAdminUpdateCourse(Element root) {
        String college = normalizeCollegeCode(Dom4jXmlService.childText(root, "college"));
        CollegeGateway gateway = gateways.get(college);
        if (gateway == null) return buildSimpleResponse(false, "College not found: " + college);
        Course c = new Course();
        c.setId(Dom4jXmlService.childText(root, "id"));
        c.setName(Dom4jXmlService.childText(root, "name"));
        c.setCredit(Integer.parseInt(Dom4jXmlService.childText(root, "credit")));
        c.setTeacher(Dom4jXmlService.childText(root, "teacher"));
        c.setLocation(Dom4jXmlService.childText(root, "location"));
        c.setCollege(college);
        c.setShared("true".equalsIgnoreCase(Dom4jXmlService.childText(root, "shared")));
        boolean ok = gateway.updateCourse(c);
        AuditLogger.log("adminUpdateCourse", college, c.getId(), ok, ok ? "Updated" : "Failed");
        return buildSimpleResponse(ok, ok ? "Course updated" : "Update failed");
    }

    private Result<Document> processAdminDeleteCourse(Element root) {
        String college = normalizeCollegeCode(Dom4jXmlService.childText(root, "college"));
        CollegeGateway gateway = gateways.get(college);
        if (gateway == null) return buildSimpleResponse(false, "College not found: " + college);
        String id = Dom4jXmlService.childText(root, "id");
        boolean ok = gateway.deleteCourse(id);
        AuditLogger.log("adminDeleteCourse", college, id, ok, ok ? "Deleted" : "Failed");
        return buildSimpleResponse(ok, ok ? "Course deleted" : "Delete failed");
    }

    private Result<Document> processAdminUpdateScore(Element root) {
        String college = normalizeCollegeCode(Dom4jXmlService.childText(root, "college"));
        CollegeGateway gateway = gateways.get(college);
        if (gateway == null) return buildSimpleResponse(false, "College not found: " + college);
        String sid = Dom4jXmlService.childText(root, "studentId");
        String cid = Dom4jXmlService.childText(root, "courseId");
        int score = Integer.parseInt(Dom4jXmlService.childText(root, "score"));
        boolean ok = gateway.updateScore(sid, cid, score);
        AuditLogger.log("adminUpdateScore", college, sid + "/" + cid, ok, "score=" + score);
        return buildSimpleResponse(ok, ok ? "Score updated" : "Update failed");
    }

    private Result<Document> processAdminListStudents(Element root) {
        String college = normalizeCollegeCode(Dom4jXmlService.childText(root, "college"));
        CollegeGateway gateway = gateways.get(college);
        if (gateway == null) return buildSimpleResponse(false, "College not found: " + college);
        return buildStudentListXml(gateway);
    }

    private Result<Document> processAdminListSelections(Element root) {
        String college = normalizeCollegeCode(Dom4jXmlService.childText(root, "college"));
        CollegeGateway gateway = gateways.get(college);
        if (gateway == null) return buildSimpleResponse(false, "College not found: " + college);
        return buildSelectionListXml(gateway);
    }

    private Result<Document> processAdminAuditLog(Element root) {
        String college = normalizeCollegeCode(Dom4jXmlService.childText(root, "college"));
        CollegeGateway gateway = gateways.get(college);
        if (gateway == null) return buildSimpleResponse(false, "College not found: " + college);
        int limit = 50;
        String limitStr = Dom4jXmlService.childText(root, "limit");
        if (!limitStr.isEmpty()) limit = Integer.parseInt(limitStr);
        String logs = gateway.getAuditLogs(limit);
        Document doc = Dom4jXmlService.createDocument("response");
        Element respRoot = doc.getRootElement();
        Dom4jXmlService.addTextElement(respRoot, "success", "true");
        Dom4jXmlService.addTextElement(respRoot, "logs", logs);
        return Result.ok(doc, "Audit log done");
    }

    private Result<Document> buildSimpleResponse(boolean success, String message) {
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", String.valueOf(success));
        Dom4jXmlService.addTextElement(root, "message", message);
        return Result.ok(doc, message);
    }

    private Result<Document> buildStudentListXml(CollegeGateway gateway) {
        List<Student> students = gateway.listAllStudents();
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "true");
        Dom4jXmlService.addTextElement(root, "college", gateway.getCollegeCode());
        Element studentsEl = root.addElement("students");
        for (Student s : students) {
            Element se = studentsEl.addElement("student");
            Dom4jXmlService.addTextElement(se, "id", s.getId());
            Dom4jXmlService.addTextElement(se, "name", s.getName());
            Dom4jXmlService.addTextElement(se, "sex", s.getSex());
            Dom4jXmlService.addTextElement(se, "major", s.getMajor());
            Dom4jXmlService.addTextElement(se, "college", s.getCollege());
        }
        return Result.ok(doc, "Student list done");
    }

    private Result<Document> buildSelectionListXml(CollegeGateway gateway) {
        List<Selection> selections = gateway.listAllSelections();
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "true");
        Dom4jXmlService.addTextElement(root, "college", gateway.getCollegeCode());
        Element selsEl = root.addElement("selections");
        for (Selection s : selections) {
            Element se = selsEl.addElement("selection");
            Dom4jXmlService.addTextElement(se, "studentId", s.getStudentId());
            Dom4jXmlService.addTextElement(se, "courseId", s.getCourseId());
            Dom4jXmlService.addTextElement(se, "college", s.getOwnerCollege());
            Dom4jXmlService.addTextElement(se, "score", s.getScore() == null ? "" : String.valueOf(s.getScore()));
        }
        return Result.ok(doc, "Selection list done");
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
