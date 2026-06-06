package edu.fusion.common.service;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.model.Selection;
import edu.fusion.common.model.Student;
import edu.fusion.common.util.DbUtil;
import edu.fusion.common.util.ErrorLogger;
import edu.fusion.common.util.JdbcConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class JdbcCollegeRepository implements CollegeGateway {

    private final String collegeCode;
    private final JdbcConfig config;
    private final String studentTable;
    private final String studentIdColumn;
    private final String studentPasswordColumn;
    private final String adminTable;
    private final String adminUsernameColumn;
    private final String adminPasswordColumn;
    private final String courseTable;
    private final String courseIdColumn;
    private final String courseNameColumn;
    private final String courseCreditColumn;
    private final String courseTeacherColumn;
    private final String courseRoomColumn;
    private final String courseSharedColumn;
    private final String sharedFlagValue;
    private final String selectionTable;
    private final String selectionStudentColumn;
    private final String selectionCourseColumn;
    private final String selectionScoreColumn;
    private final String studentNameColumn;
    private final String studentGenderColumn;
    private final String studentMajorColumn;

    public JdbcCollegeRepository(String collegeCode,
            JdbcConfig config,
            String studentTable,
            String studentIdColumn,
            String studentPasswordColumn,
            String adminTable,
            String adminUsernameColumn,
            String adminPasswordColumn,
            String courseTable,
            String courseIdColumn,
            String courseNameColumn,
            String courseCreditColumn,
            String courseTeacherColumn,
            String courseRoomColumn,
            String courseSharedColumn,
            String sharedFlagValue,
            String selectionTable,
            String selectionStudentColumn,
            String selectionCourseColumn,
            String selectionScoreColumn,
            String studentNameColumn,
            String studentGenderColumn,
            String studentMajorColumn) {
        this.collegeCode = collegeCode;
        this.config = config;
        this.studentTable = studentTable;
        this.studentIdColumn = studentIdColumn;
        this.studentPasswordColumn = studentPasswordColumn;
        this.adminTable = adminTable;
        this.adminUsernameColumn = adminUsernameColumn;
        this.adminPasswordColumn = adminPasswordColumn;
        this.courseTable = courseTable;
        this.courseIdColumn = courseIdColumn;
        this.courseNameColumn = courseNameColumn;
        this.courseCreditColumn = courseCreditColumn;
        this.courseTeacherColumn = courseTeacherColumn;
        this.courseRoomColumn = courseRoomColumn;
        this.courseSharedColumn = courseSharedColumn;
        this.sharedFlagValue = sharedFlagValue;
        this.selectionTable = selectionTable;
        this.selectionStudentColumn = selectionStudentColumn;
        this.selectionCourseColumn = selectionCourseColumn;
        this.selectionScoreColumn = selectionScoreColumn;
        this.studentNameColumn = studentNameColumn;
        this.studentGenderColumn = studentGenderColumn;
        this.studentMajorColumn = studentMajorColumn;
    }

    @Override
    public String getCollegeCode() {
        return collegeCode;
    }

    @Override
    public boolean authenticateStudent(String username, String password) {
        String sql = "select count(1) from " + studentTable + " where " + studentIdColumn + " = ? and " + studentPasswordColumn + " = ?";
        return querySingleInt(sql, username, password) > 0;
    }

    @Override
    public boolean authenticateAdmin(String username, String password) {
        String sql = "select count(1) from " + adminTable + " where " + adminUsernameColumn + " = ? and " + adminPasswordColumn + " = ?";
        return querySingleInt(sql, username, password) > 0;
    }

    @Override
    public List<Course> listAllCourses() {
        String sql = buildCourseSelectSql(null);
        return queryCourses(sql);
    }

    @Override
    public List<Course> listSharedCourses() {
        String sql = buildCourseSelectSql(courseSharedColumn + " in ('1', 'Y', 'y', 'T', 't')");
        return queryCourses(sql);
    }

    @Override
    public List<Course> listStudentCourses(String studentId) {
        String sql = "select c." + courseIdColumn + ", c." + courseNameColumn + ", c." + courseCreditColumn + ", c." + courseTeacherColumn + ", c." + courseRoomColumn + ", c." + courseSharedColumn
                + " from " + courseTable + " c inner join " + selectionTable + " s on c." + courseIdColumn + " = s." + selectionCourseColumn
                + " where s." + selectionStudentColumn + " = ? order by c." + courseIdColumn;
        return queryCourses(sql, studentId);
    }

    @Override
    public boolean selectCourse(String studentId, String courseId) {
        if (studentId == null || studentId.trim().isEmpty() || courseId == null || courseId.trim().isEmpty()) {
            return false;
        }
        if (!exists(courseTable, courseIdColumn, courseId)) {
            return false;
        }
        if (exists(selectionTable, selectionStudentColumn, studentId, selectionCourseColumn, courseId)) {
            return false;
        }
        String sql = "insert into " + selectionTable + "(" + selectionStudentColumn + ", " + selectionCourseColumn + ", " + selectionScoreColumn + ") values (?, ?, ?)";
        try (Connection connection = DbUtil.getConnection(config); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentId);
            statement.setString(2, courseId);
            statement.setNull(3, Types.INTEGER);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to select course for college " + collegeCode, ex);
        }
    }

    @Override
    public boolean dropCourse(String studentId, String courseId) {
        String sql = "delete from " + selectionTable + " where " + selectionStudentColumn + " = ? and " + selectionCourseColumn + " = ?";
        try (Connection connection = DbUtil.getConnection(config); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentId);
            statement.setString(2, courseId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to drop course for college " + collegeCode, ex);
        }
    }

    @Override
    public int countStudents() {
        return querySingleInt("select count(1) from " + studentTable);
    }

    @Override
    public int countCourses() {
        return querySingleInt("select count(1) from " + courseTable);
    }

    @Override
    public int countSelections() {
        return querySingleInt("select count(1) from " + selectionTable);
    }

    @Override
    public int countSharedCourses() {
        String sql = "select count(1) from " + courseTable + " where " + courseSharedColumn + " in ('1', 'Y', 'y', 'T', 't')";
        return querySingleInt(sql);
    }

    @Override
    public List<CourseHeat> topCourses(int topN) {
        String sql = "select c." + courseIdColumn + ", c." + courseNameColumn + ", count(s." + selectionStudentColumn + ") as heat"
                + " from " + courseTable + " c left join " + selectionTable + " s on c." + courseIdColumn + " = s." + selectionCourseColumn
                + " group by c." + courseIdColumn + ", c." + courseNameColumn
                + " order by heat desc, c." + courseIdColumn + " asc";
        List<CourseHeat> result = new ArrayList<CourseHeat>();
        try (Connection connection = DbUtil.getConnection(config); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setMaxRows(topN);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next() && result.size() < topN) {
                    CourseHeat heat = new CourseHeat();
                    heat.setCourseId(resultSet.getString(1));
                    heat.setCourseName(resultSet.getString(2));
                    heat.setSelectedCount(resultSet.getInt(3));
                    heat.setCollege(collegeCode);
                    result.add(heat);
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to query top courses for college " + collegeCode, ex);
        }
    }

    @Override
    public List<Student> listAllStudents() {
        String sql = "select " + studentIdColumn + ", " + studentNameColumn + ", " + studentGenderColumn + ", " + studentMajorColumn
                + " from " + studentTable + " order by " + studentIdColumn;
        List<Student> result = new ArrayList<>();
        try (Connection connection = DbUtil.getConnection(config); PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Student student = new Student();
                    student.setId(resultSet.getString(1));
                    student.setName(resultSet.getString(2));
                    student.setSex(resultSet.getString(3));
                    student.setMajor(resultSet.getString(4));
                    student.setCollege(collegeCode);
                    result.add(student);
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to list all students for college " + collegeCode, ex);
        }
    }

    @Override
    public List<Selection> listAllSelections() {
        String sql = "select " + selectionStudentColumn + ", " + selectionCourseColumn + ", " + selectionScoreColumn
                + " from " + selectionTable + " order by " + selectionStudentColumn + ", " + selectionCourseColumn;
        List<Selection> result = new ArrayList<>();
        try (Connection connection = DbUtil.getConnection(config); PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Selection selection = new Selection();
                    selection.setStudentId(resultSet.getString(1));
                    selection.setCourseId(resultSet.getString(2));
                    Object scoreObj = resultSet.getObject(3);
                    selection.setScore(scoreObj != null ? ((Number) scoreObj).intValue() : null);
                    selection.setOwnerCollege(collegeCode);
                    result.add(selection);
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to list all selections for college " + collegeCode, ex);
        }
    }

    // ===== Admin CRUD: Students =====

    @Override
    public boolean addStudent(Student student) {
        String sql = "insert into " + studentTable + " (" + studentIdColumn + ", " + studentNameColumn
                + ", " + studentGenderColumn + ", " + studentMajorColumn + ") values (?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection(config); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getId());
            stmt.setString(2, student.getName());
            stmt.setString(3, student.getSex());
            stmt.setString(4, student.getMajor());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ErrorLogger.log("jdbc.addStudent", "id=" + student.getId(), ex);
            return false;
        }
    }

    @Override
    public boolean updateStudent(Student student) {
        String sql = "update " + studentTable + " set " + studentNameColumn + "=?, " + studentGenderColumn
                + "=?, " + studentMajorColumn + "=? where " + studentIdColumn + "=?";
        try (Connection conn = DbUtil.getConnection(config); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getName());
            stmt.setString(2, student.getSex());
            stmt.setString(3, student.getMajor());
            stmt.setString(4, student.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ErrorLogger.log("jdbc.updateStudent", "id=" + student.getId(), ex);
            return false;
        }
    }

    @Override
    public boolean deleteStudent(String studentId) {
        String delSel = "delete from " + selectionTable + " where " + selectionStudentColumn + "=?";
        String delStu = "delete from " + studentTable + " where " + studentIdColumn + "=?";
        try (Connection conn = DbUtil.getConnection(config);
             PreparedStatement stmt1 = conn.prepareStatement(delSel);
             PreparedStatement stmt2 = conn.prepareStatement(delStu)) {
            stmt1.setString(1, studentId);
            stmt1.executeUpdate();
            stmt2.setString(1, studentId);
            return stmt2.executeUpdate() > 0;
        } catch (SQLException ex) {
            ErrorLogger.log("jdbc.deleteStudent", "id=" + studentId, ex);
            return false;
        }
    }

    // ===== Admin CRUD: Courses =====

    @Override
    public boolean addCourse(Course course) {
        String sql = "insert into " + courseTable + " (" + courseIdColumn + ", " + courseNameColumn
                + ", " + courseCreditColumn + ", " + courseTeacherColumn + ", " + courseRoomColumn
                + ", " + courseSharedColumn + ") values (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection(config); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getId());
            stmt.setString(2, course.getName());
            stmt.setInt(3, course.getCredit());
            stmt.setString(4, course.getTeacher());
            stmt.setString(5, course.getLocation());
            stmt.setString(6, course.isShared() ? "1" : "0");
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ErrorLogger.log("jdbc.addCourse", "id=" + course.getId(), ex);
            return false;
        }
    }

    @Override
    public boolean updateCourse(Course course) {
        String sql = "update " + courseTable + " set " + courseNameColumn + "=?, " + courseCreditColumn
                + "=?, " + courseTeacherColumn + "=?, " + courseRoomColumn + "=?, " + courseSharedColumn
                + "=? where " + courseIdColumn + "=?";
        try (Connection conn = DbUtil.getConnection(config); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getName());
            stmt.setInt(2, course.getCredit());
            stmt.setString(3, course.getTeacher());
            stmt.setString(4, course.getLocation());
            stmt.setString(5, course.isShared() ? "1" : "0");
            stmt.setString(6, course.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ErrorLogger.log("jdbc.updateCourse", "id=" + course.getId(), ex);
            return false;
        }
    }

    @Override
    public boolean deleteCourse(String courseId) {
        String sql = "delete from " + courseTable + " where " + courseIdColumn + "=?";
        try (Connection conn = DbUtil.getConnection(config); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ErrorLogger.log("jdbc.deleteCourse", "id=" + courseId, ex);
            return false;
        }
    }

    // ===== Admin: Scores =====

    @Override
    public boolean updateScore(String studentId, String courseId, int score) {
        String sql = "update " + selectionTable + " set " + selectionScoreColumn + "=? where "
                + selectionStudentColumn + "=? and " + selectionCourseColumn + "=?";
        try (Connection conn = DbUtil.getConnection(config); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, score);
            stmt.setString(2, studentId);
            stmt.setString(3, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ErrorLogger.log("jdbc.updateScore", "sid=" + studentId + " cid=" + courseId, ex);
            return false;
        }
    }

    // ===== Admin: Audit Logs =====

    @Override
    public String getAuditLogs(int limit) {
        return "";
    }

    private String buildCourseSelectSql(String whereClause) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ")
                .append(courseIdColumn).append(", ")
                .append(courseNameColumn).append(", ")
                .append(courseCreditColumn).append(", ")
                .append(courseTeacherColumn).append(", ")
                .append(courseRoomColumn).append(", ")
                .append(courseSharedColumn)
                .append(" from ").append(courseTable);
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql.append(" where ").append(whereClause);
        }
        sql.append(" order by ").append(courseIdColumn);
        return sql.toString();
    }

    private List<Course> queryCourses(String sql, Object... args) {
        List<Course> result = new ArrayList<Course>();
        try (Connection connection = DbUtil.getConnection(config); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, args);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Course course = new Course();
                    course.setId(resultSet.getString(1));
                    course.setName(resultSet.getString(2));
                    course.setCredit(resultSet.getInt(3));
                    course.setTeacher(resultSet.getString(4));
                    course.setLocation(resultSet.getString(5));
                    course.setShared(isSharedValue(resultSet.getString(6)));
                    course.setCollege(collegeCode);
                    result.add(course);
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to query courses for college " + collegeCode, ex);
        }
    }

    private int querySingleInt(String sql, Object... args) {
        try (Connection connection = DbUtil.getConnection(config); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, args);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to execute count query for college " + collegeCode, ex);
        }
    }

    private boolean exists(String table, String column, String value) {
        return querySingleInt("select count(1) from " + table + " where " + column + " = ?", value) > 0;
    }

    private boolean exists(String table, String column1, String value1, String column2, String value2) {
        return querySingleInt("select count(1) from " + table + " where " + column1 + " = ? and " + column2 + " = ?", value1, value2) > 0;
    }

    private void bind(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    private boolean isSharedValue(String value) {
        if (value == null) {
            return false;
        }
        return sharedFlagValue.equalsIgnoreCase(value) || "1".equals(value) || "Y".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value);
    }
}
