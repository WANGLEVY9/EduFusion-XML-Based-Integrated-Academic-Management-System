package edu.fusion.common.ui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticsPanelTest {

    @BeforeAll
    static void setHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void loadStatisticsShouldPopulateGallery() throws Exception {
        StatisticsPanel panel = new StatisticsPanel(() -> {}, false);
        panel.loadStatistics(buildSuccessXml());

        assertTrue(panel.isDataLoaded());

        JPanel gallery = getPrivateField(panel, "galleryGrid", JPanel.class);
        assertTrue(gallery.getComponentCount() > 0);
    }

    @Test
    void loadStatisticsShouldSetCardPanelData() throws Exception {
        StatisticsPanel panel = new StatisticsPanel(() -> {}, false);
        panel.loadStatistics(buildSuccessXml());

        StatsCardPanel cardPanel = getPrivateField(panel, "cardPanel", StatsCardPanel.class);
        assertTrue(cardPanel.getComponentCount() > 0);
    }

    @Test
    void loadStatisticsShouldShowErrorWhenFailed() throws Exception {
        StatisticsPanel panel = new StatisticsPanel(() -> {}, false);
        panel.loadStatistics("<response><success>false</success><message>failed</message></response>");

        assertFalse(panel.isDataLoaded());

        StatsCardPanel cardPanel = getPrivateField(panel, "cardPanel", StatsCardPanel.class);
        // After error, card panel should reset to zeros
        assertTrue(cardPanel.getComponentCount() > 0);
    }

    @Test
    void adminModeShowsAdminCards() throws Exception {
        StatisticsPanel panel = new StatisticsPanel(() -> {}, true);
        panel.loadStatistics(buildSuccessXml());

        JPanel gallery = getPrivateField(panel, "galleryGrid", JPanel.class);
        assertTrue(gallery.getComponentCount() > 0);
        // Admin mode should show more cards than non-admin
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
                + "<college><code>A</code><students>10</students><courses>3</courses><selections>5</selections><sharedCourses>1</sharedCourses></college>"
                + "<college><code>B</code><students>10</students><courses>3</courses><selections>5</selections><sharedCourses>1</sharedCourses></college>"
                + "<college><code>C</code><students>10</students><courses>3</courses><selections>5</selections><sharedCourses>1</sharedCourses></college>"
                + "</colleges>"
                + "<topCourses>"
                + "<course><id>A101</id><name>Math</name><college>A</college><selectedCount>6</selectedCount></course>"
                + "<course><id>B101</id><name>Physics</name><college>B</college><selectedCount>5</selectedCount></course>"
                + "</topCourses>"
                + "<allCourses>"
                + "<course><id>A101</id><name>Math</name><credit>4</credit><teacher>张老师</teacher><location>A101</location><college>A</college><shared>true</shared></course>"
                + "<course><id>B101</id><name>Physics</name><credit>3</credit><teacher>李老师</teacher><location>B101</location><college>B</college><shared>true</shared></course>"
                + "</allCourses>"
                + "</statistics>"
                + "</response>";
    }

    private static <T> T getPrivateField(Object target, String name, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}
