package edu.fusion.common.ui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticsPanelTest {

    @BeforeAll
    static void setHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void loadStatisticsShouldPopulateDetailAndCharts() throws Exception {
        StatisticsPanel panel = new StatisticsPanel();
        panel.loadStatistics(buildSuccessXml());

        JTextArea detailArea = getPrivateField(panel, "detailArea", JTextArea.class);
        assertTrue(detailArea.getText().contains("总学生数: 30"));
        assertTrue(detailArea.getText().contains("热门课程 TOP10"));

        JTabbedPane tabbedPane = getPrivateField(panel, "chartTabPane", JTabbedPane.class);
        assertEquals(2, tabbedPane.getTabCount());
    }

    @Test
    void loadStatisticsShouldShowErrorWhenFailed() throws Exception {
        StatisticsPanel panel = new StatisticsPanel();
        panel.loadStatistics("<response><success>false</success><message>failed</message></response>");

        JTextArea detailArea = getPrivateField(panel, "detailArea", JTextArea.class);
        assertTrue(detailArea.getText().contains("统计请求失败"));
    }

    private static String buildSuccessXml() {
        return "<response>"
                + "<success>true</success>"
                + "<message>ok</message>"
                + "<statistics>"
                + "<totalStudents>30</totalStudents>"
                + "<totalCourses>9</totalCourses>"
                + "<totalSelections>15</totalSelections>"
                + "<totalSharedCourses>3</totalSharedCourses>"
                + "<colleges>"
                + "<college><code>A</code><students>10</students><courses>3</courses><selections>5</selections></college>"
                + "<college><code>B</code><students>10</students><courses>3</courses><selections>5</selections></college>"
                + "<college><code>C</code><students>10</students><courses>3</courses><selections>5</selections></college>"
                + "</colleges>"
                + "<topCourses>"
                + "<course><id>A101</id><name>Math</name><college>A</college><selectedCount>6</selectedCount></course>"
                + "<course><id>B101</id><name>Physics</name><college>B</college><selectedCount>5</selectedCount></course>"
                + "</topCourses>"
                + "</statistics>"
                + "</response>";
    }

    private static <T> T getPrivateField(Object target, String name, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}
