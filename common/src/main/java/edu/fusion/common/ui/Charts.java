package edu.fusion.common.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import edu.fusion.common.model.CourseHeat;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

public final class Charts {

    private static final Color COLOR_A = new Color(0x4A, 0x90, 0xD9);
    private static final Color COLOR_B = new Color(0x50, 0xC8, 0x78);
    private static final Color COLOR_C = new Color(0xFF, 0x8C, 0x42);
    private static final Color BG_COLOR = new Color(0xF0, 0xF2, 0xF5);

    private Charts() {
    }

    /**
     * 三学院对比分组柱状图
     */
    public static JFreeChart createCollegeCompareChart(
            String[] collegeNames, int[] studentCounts, int[] courseCounts, int[] selectionCounts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < collegeNames.length; i++) {
            dataset.addValue(studentCounts[i], "学生数", collegeNames[i]);
            dataset.addValue(courseCounts[i], "课程数", collegeNames[i]);
            dataset.addValue(selectionCounts[i], "选课数", collegeNames[i]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "三学院数据对比", null, "数量",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, COLOR_A);
        renderer.setSeriesPaint(1, COLOR_B);
        renderer.setSeriesPaint(2, COLOR_C);
        renderer.setDrawBarOutline(false);

        return chart;
    }

    /**
     * 选课学院占比饼图
     */
    public static JFreeChart createSelectionPieChart(
            String[] collegeNames, int[] selectionCounts) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (int i = 0; i < collegeNames.length; i++) {
            dataset.setValue(collegeNames[i], selectionCounts[i]);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "选课学院分布", dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setSectionPaint(collegeNames[0], COLOR_A);
        plot.setSectionPaint(collegeNames[1], COLOR_B);
        plot.setSectionPaint(collegeNames[2], COLOR_C);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setIgnoreNullValues(true);
        plot.setIgnoreZeroValues(true);

        return chart;
    }

    /**
     * 热门课程 TOP10 水平柱状图
     */
    public static JFreeChart createTopCoursesChart(List<CourseHeat> topCourses) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (CourseHeat heat : topCourses) {
            String label = heat.getCourseId() + " " + heat.getCourseName();
            dataset.addValue(heat.getSelectedCount(), "选课人数", label);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "热门课程 TOP10", null, "选课人数",
                dataset, PlotOrientation.HORIZONTAL,
                false, true, false);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(BG_COLOR);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, COLOR_A);
        renderer.setDrawBarOutline(false);

        return chart;
    }
}
