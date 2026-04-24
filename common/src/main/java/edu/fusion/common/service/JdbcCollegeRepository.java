package edu.fusion.common.service;

import edu.fusion.common.model.Course;
import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.util.DbUtil;
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
            String selectionScoreColumn) {
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
