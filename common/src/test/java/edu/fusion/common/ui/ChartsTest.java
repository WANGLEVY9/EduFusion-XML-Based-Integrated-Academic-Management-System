package edu.fusion.common.ui;

import edu.fusion.common.model.CourseHeat;
import org.jfree.chart.JFreeChart;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChartsTest {

    @Test
    void createCollegeCompareChartShouldBuildChart() {
        JFreeChart chart = Charts.createCollegeCompareChart(
                new String[]{"A", "B", "C"},
                new int[]{10, 12, 8},
                new int[]{6, 7, 5},
                new int[]{20, 18, 15});
        assertNotNull(chart);
        assertEquals("三学院数据对比", chart.getTitle().getText());
    }

    @Test
    void createSelectionPieChartShouldBuildChart() {
        JFreeChart chart = Charts.createSelectionPieChart(
                new String[]{"A", "B", "C"},
                new int[]{20, 18, 15});
        assertNotNull(chart);
        assertEquals("选课学院分布", chart.getTitle().getText());
    }

    @Test
    void createTopCoursesChartShouldBuildChart() {
        CourseHeat a = new CourseHeat();
        a.setCourseId("A101");
        a.setCourseName("Math");
        a.setCollege("A");
        a.setSelectedCount(12);

        CourseHeat b = new CourseHeat();
        b.setCourseId("B101");
        b.setCourseName("Physics");
        b.setCollege("B");
        b.setSelectedCount(9);

        List<CourseHeat> heats = Arrays.asList(a, b);
        JFreeChart chart = Charts.createTopCoursesChart(heats);
        assertNotNull(chart);
        assertEquals("热门课程 TOP10", chart.getTitle().getText());
    }
}
