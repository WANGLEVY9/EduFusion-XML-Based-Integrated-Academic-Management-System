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
import java.util.Arrays;
import java.util.List;

public final class Charts {

    private static final Color BLUE = new Color(0x4A, 0x90, 0xD9);
    private static final Color GREEN = new Color(0x50, 0xC8, 0x78);
    private static final Color ORANGE = new Color(0xFF, 0x8C, 0x42);
    private static final Color BG_COLOR = new Color(0xF0, 0xF2, 0xF5);
    private static final List<Color> COLLEGE_COLORS = Arrays.asList(BLUE, GREEN, ORANGE);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 12);

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

        styleBarPlot((CategoryPlot) chart.getPlot(), BLUE, GREEN, ORANGE);
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
        for (int i = 0; i < Math.min(collegeNames.length, COLLEGE_COLORS.size()); i++) {
            plot.setSectionPaint(collegeNames[i], COLLEGE_COLORS.get(i));
        }
        plot.setLabelFont(LABEL_FONT);
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

        styleBarPlot((CategoryPlot) chart.getPlot(), BLUE);
        return chart;
    }

    private static void styleBarPlot(CategoryPlot plot, Color... seriesColors) {
        plot.setBackgroundPaint(BG_COLOR);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        for (int i = 0; i < seriesColors.length; i++) {
            renderer.setSeriesPaint(i, seriesColors[i]);
        }
    }
}
