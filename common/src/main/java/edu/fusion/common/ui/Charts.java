package edu.fusion.common.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.DefaultKeyedValueDataset;

import edu.fusion.common.model.CourseHeat;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public final class Charts {

    // ─── Color palette ───
    private static final Color BLUE  = new Color(0x4A, 0x90, 0xD9);
    private static final Color GREEN = new Color(0x50, 0xC8, 0x78);
    private static final Color ORANGE= new Color(0xFF, 0x8C, 0x42);
    private static final Color PURPLE= new Color(0x9B, 0x59, 0xB6);
    private static final Color RED   = new Color(0xE7, 0x4C, 0x3C);
    private static final Color TEAL  = new Color(0x1A, 0xBC, 0x9C);
    private static final Color BG    = new Color(0xF0, 0xF2, 0xF5);
    private static final Color TITLE_COLOR = new Color(0x33, 0x33, 0x33);
    private static final List<Color> PALETTE = Arrays.asList(BLUE, GREEN, ORANGE, PURPLE, RED, TEAL);

    // ─── Chinese-capable font ───
    private static final Font CHART_TITLE_FONT;
    private static final Font AXIS_FONT;
    private static final Font TICK_FONT;
    private static final Font LEGEND_FONT;
    private static final Font PIE_LABEL_FONT;

    static {
        Font cnFont = findChineseFont("Microsoft YaHei", "SimHei", "SimSun", "WenQuanYi Micro Hei");
        CHART_TITLE_FONT = cnFont.deriveFont(Font.BOLD, 15f);
        AXIS_FONT        = cnFont.deriveFont(Font.PLAIN, 13f);
        TICK_FONT        = cnFont.deriveFont(Font.PLAIN, 11f);
        LEGEND_FONT      = cnFont.deriveFont(Font.PLAIN, 11f);
        PIE_LABEL_FONT   = cnFont.deriveFont(Font.PLAIN, 12f);

        // Apply as JFreeChart default theme (fixes Chinese everywhere)
        StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();
        theme.setExtraLargeFont(CHART_TITLE_FONT);
        theme.setLargeFont(AXIS_FONT);
        theme.setRegularFont(TICK_FONT);
        theme.setSmallFont(LEGEND_FONT);
        ChartFactory.setChartTheme(theme);
    }

    private static Font findChineseFont(String... candidates) {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String candidate : candidates) {
            for (String name : available) {
                if (name.equalsIgnoreCase(candidate)) {
                    return new Font(name, Font.PLAIN, 12);
                }
            }
        }
        for (String name : available) {
            if (name.contains("YaHei") || name.contains("Hei") || name.contains("CJK")) {
                return new Font(name, Font.PLAIN, 12);
            }
        }
        return new Font("SansSerif", Font.PLAIN, 12);
    }

    private Charts() {}

    // ════════════════════════════════════════════════
    // 1.  Grouped bar — 三学院数据对比（柱状图）
    // ════════════════════════════════════════════════
    public static JFreeChart createCollegeCompareChart(
            String[] names, int[] students, int[] courses, int[] selections) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (int i = 0; i < names.length; i++) {
            ds.addValue(students[i],   "学生数", names[i]);
            ds.addValue(courses[i],    "课程数", names[i]);
            ds.addValue(selections[i], "选课数", names[i]);
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "三学院数据对比", null, "数量", ds,
                PlotOrientation.VERTICAL, true, true, false);
        styleTitle(chart);
        styleBarPlot((CategoryPlot) chart.getPlot(), BLUE, GREEN, PURPLE);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 2.  Pie — 选课学院分布（饼图）
    // ════════════════════════════════════════════════
    public static JFreeChart createSelectionPieChart(String[] names, int[] selections) {
        DefaultPieDataset ds = new DefaultPieDataset();
        for (int i = 0; i < names.length; i++) {
            ds.setValue(names[i], selections[i]);
        }
        JFreeChart chart = ChartFactory.createPieChart(
                "选课学院分布", ds, true, true, false);
        styleTitle(chart);
        stylePiePlot((PiePlot) chart.getPlot(), names);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 3.  Horizontal bar — 热门课程 TOP10
    // ════════════════════════════════════════════════
    public static JFreeChart createTopCoursesChart(List<CourseHeat> topCourses) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (CourseHeat h : topCourses) {
            ds.addValue(h.getSelectedCount(), "选课人数", h.getCourseId() + " " + h.getCourseName());
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "热门课程 TOP10", null, "选课人数", ds,
                PlotOrientation.HORIZONTAL, false, true, false);
        styleTitle(chart);
        styleBarPlot((CategoryPlot) chart.getPlot(), BLUE);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 4.  Ring (Donut) — 各学院学生占比
    // ════════════════════════════════════════════════
    public static JFreeChart createStudentRingChart(String[] names, int[] studentCounts) {
        DefaultPieDataset ds = new DefaultPieDataset();
        for (int i = 0; i < names.length; i++) {
            ds.setValue(names[i], studentCounts[i]);
        }
        JFreeChart chart = ChartFactory.createRingChart(
                "各学院学生分布", ds, true, true, false);
        styleTitle(chart);
        PiePlot plot = (PiePlot) chart.getPlot();
        stylePiePlot(plot, names);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {1} 人 ({2})", new DecimalFormat("#,###"), new DecimalFormat("0.0%")));
        return chart;
    }

    // ════════════════════════════════════════════════
    // 5.  Stacked bar — 三学院多指标堆积
    // ════════════════════════════════════════════════
    public static JFreeChart createCollegeStackedBarChart(
            String[] names, int[] students, int[] courses, int[] selections) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (int i = 0; i < names.length; i++) {
            ds.addValue(students[i],   "学生数", names[i]);
            ds.addValue(courses[i],    "课程数", names[i]);
            ds.addValue(selections[i], "选课数", names[i]);
        }
        JFreeChart chart = ChartFactory.createStackedBarChart(
                "三学院指标堆积", null, "数量", ds,
                PlotOrientation.VERTICAL, true, true, false);
        styleTitle(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(BG);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setOutlineVisible(false);
        StackedBarRenderer r = (StackedBarRenderer) plot.getRenderer();
        r.setBarPainter(new StandardBarPainter());
        r.setDrawBarOutline(false);
        r.setSeriesPaint(0, BLUE);
        r.setSeriesPaint(1, GREEN);
        r.setSeriesPaint(2, ORANGE);
        r.setShadowVisible(false);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 6.  Line chart — 三学院各维度折线
    // ════════════════════════════════════════════════
    public static JFreeChart createCollegeLineChart(
            String[] names, int[] students, int[] courses, int[] selections) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (int i = 0; i < names.length; i++) {
            ds.addValue(students[i],   "学生数", names[i]);
            ds.addValue(courses[i],    "课程数", names[i]);
            ds.addValue(selections[i], "选课数", names[i]);
        }
        JFreeChart chart = ChartFactory.createLineChart(
                "三学院数据趋势", "学院", "数量", ds,
                PlotOrientation.VERTICAL, true, true, false);
        styleTitle(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(BG);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(0xE0, 0xE0, 0xE0));
        plot.setOutlineVisible(false);
        LineAndShapeRenderer r = (LineAndShapeRenderer) plot.getRenderer();
        r.setSeriesPaint(0, BLUE);  r.setSeriesStroke(0, new java.awt.BasicStroke(2f));
        r.setSeriesPaint(1, GREEN); r.setSeriesStroke(1, new java.awt.BasicStroke(2f));
        r.setSeriesPaint(2, ORANGE);r.setSeriesStroke(2, new java.awt.BasicStroke(2f));
        r.setDefaultShapesVisible(true);
        r.setDefaultShapesFilled(true);
        styleAxisFonts(plot);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 7.  Area chart — 三学院累积面积
    // ════════════════════════════════════════════════
    public static JFreeChart createCollegeAreaChart(
            String[] names, int[] students, int[] courses, int[] selections) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (int i = 0; i < names.length; i++) {
            ds.addValue(students[i],   "学生数", names[i]);
            ds.addValue(courses[i],    "课程数", names[i]);
            ds.addValue(selections[i], "选课数", names[i]);
        }
        JFreeChart chart = ChartFactory.createAreaChart(
                "三学院指标面积图", "学院", "数量", ds,
                PlotOrientation.VERTICAL, true, true, false);
        styleTitle(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(BG);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.getRenderer().setSeriesPaint(0, BLUE);
        plot.getRenderer().setSeriesPaint(1, GREEN);
        plot.getRenderer().setSeriesPaint(2, ORANGE);
        styleAxisFonts(plot);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 8.  Scatter — 学分与选课人数关系
    // ════════════════════════════════════════════════
    public static JFreeChart createCreditScatterChart(
            Object[][] courseData, String[] collegeNames) {
        // courseData: [credit, selectionCount, collegeIndex]
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (Object[] row : courseData) {
            int credit = (int) row[0];
            int sel = (int) row[1];
            int ci = (int) row[2];
            String label = collegeNames[ci] + " (" + credit + "学分)";
            ds.addValue(sel, collegeNames[ci], label);
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "学分 vs 选课人数", "课程", "选课人数", ds,
                PlotOrientation.VERTICAL, true, true, false);
        styleTitle(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(BG);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setOutlineVisible(false);
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setBarPainter(new StandardBarPainter());
        r.setDrawBarOutline(false);
        r.setShadowVisible(false);
        for (int i = 0; i < collegeNames.length && i < PALETTE.size(); i++) {
            r.setSeriesPaint(i, PALETTE.get(i));
        }
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        styleAxisFonts(plot);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 9.  Shared courses bar — 各学院共享课程对比
    // ════════════════════════════════════════════════
    public static JFreeChart createSharedCoursesBarChart(
            String[] names, int[] sharedCounts, int[] totalCounts) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (int i = 0; i < names.length; i++) {
            ds.addValue(totalCounts[i], "总课程数", names[i]);
            ds.addValue(sharedCounts[i], "共享课程数", names[i]);
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "各学院课程共享情况", null, "课程数", ds,
                PlotOrientation.VERTICAL, true, true, false);
        styleTitle(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(BG);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setOutlineVisible(false);
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setBarPainter(new StandardBarPainter());
        r.setDrawBarOutline(false);
        r.setSeriesPaint(0, BLUE);
        r.setSeriesPaint(1, GREEN);
        r.setShadowVisible(false);
        styleAxisFonts(plot);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 10. Selection density — 各学院选课密度对比
    //   人均选课数 = selections / students
    // ════════════════════════════════════════════════
    public static JFreeChart createSelectionDensityChart(
            String[] names, int[] students, int[] selections) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (int i = 0; i < names.length; i++) {
            double density = students[i] > 0 ? (double) selections[i] / students[i] : 0;
            ds.addValue(Math.round(density * 10) / 10.0, "人均选课数", names[i]);
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "人均选课数对比（选课密度）", null, "人均选课数", ds,
                PlotOrientation.VERTICAL, false, true, false);
        styleTitle(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(BG);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setOutlineVisible(false);
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setBarPainter(new StandardBarPainter());
        r.setDrawBarOutline(false);
        r.setSeriesPaint(0, PURPLE);
        r.setShadowVisible(false);
        styleAxisFonts(plot);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 11. Course pie — 各学院课程占比
    // ════════════════════════════════════════════════
    public static JFreeChart createCoursePieChart(String[] names, int[] courseCounts) {
        DefaultPieDataset ds = new DefaultPieDataset();
        for (int i = 0; i < names.length; i++) {
            ds.setValue(names[i], courseCounts[i]);
        }
        JFreeChart chart = ChartFactory.createPieChart(
                "各学院课程占比", ds, true, true, false);
        styleTitle(chart);
        PiePlot plot = (PiePlot) chart.getPlot();
        stylePiePlot(plot, names);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {1} 门 ({2})", new DecimalFormat("#,###"), new DecimalFormat("0.0%")));
        return chart;
    }

    // ════════════════════════════════════════════════
    // Shared styling helpers
    // ════════════════════════════════════════════════

    private static void styleTitle(JFreeChart chart) {
        TextTitle t = chart.getTitle();
        if (t != null) {
            t.setFont(CHART_TITLE_FONT);
            t.setPaint(TITLE_COLOR);
        }
        chart.setBackgroundPaint(BG);
        chart.setBorderPaint(BG);
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(LEGEND_FONT);
        }
    }

    private static void styleBarPlot(CategoryPlot plot, Color... seriesColors) {
        plot.setBackgroundPaint(BG);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setOutlineVisible(false);
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setBarPainter(new StandardBarPainter());
        r.setDrawBarOutline(false);
        for (int i = 0; i < seriesColors.length; i++) {
            r.setSeriesPaint(i, seriesColors[i]);
        }
        r.setShadowVisible(false);
        r.setItemMargin(0.15);
        styleAxisFonts(plot);
    }

    private static void stylePiePlot(PiePlot plot, String[] names) {
        plot.setBackgroundPaint(BG);
        plot.setOutlineVisible(false);
        plot.setLabelFont(PIE_LABEL_FONT);
        plot.setLabelBackgroundPaint(new Color(0xFF, 0xFF, 0xFF, 0xE0));
        plot.setLabelShadowPaint(null);
        plot.setLabelOutlinePaint(new Color(0xDD, 0xDD, 0xDD));
        plot.setIgnoreNullValues(true);
        plot.setIgnoreZeroValues(true);
        for (int i = 0; i < Math.min(names.length, PALETTE.size()); i++) {
            plot.setSectionPaint(names[i], PALETTE.get(i));
        }
    }

    private static void styleAxisFonts(CategoryPlot plot) {
        CategoryAxis domain = plot.getDomainAxis();
        if (domain != null) {
            domain.setTickLabelFont(TICK_FONT);
            domain.setLabelFont(AXIS_FONT);
        }
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        if (range != null) {
            range.setTickLabelFont(TICK_FONT);
            range.setLabelFont(AXIS_FONT);
        }
    }

    // ════════════════════════════════════════════════
    // 12. Teacher workload — horizontal bar (admin)
    // ════════════════════════════════════════════════
    public static JFreeChart createTeacherWorkloadChart(String[] teachers, int[] courseCounts) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (int i = 0; i < teachers.length; i++) {
            ds.addValue(courseCounts[i], "课程数", teachers[i]);
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "教师课程负荷", "教师", "课程数", ds,
                PlotOrientation.HORIZONTAL, false, true, false);
        styleTitle(chart);
        styleBarPlot((CategoryPlot) chart.getPlot(), GREEN);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 13. Credit distribution — vertical bar (admin)
    // ════════════════════════════════════════════════
    public static JFreeChart createCreditDistributionChart(String[] creditLabels, int[] courseCounts) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (int i = 0; i < creditLabels.length; i++) {
            ds.addValue(courseCounts[i], "课程数", creditLabels[i] + "学分");
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "学分分布分析", "学分", "课程数", ds,
                PlotOrientation.VERTICAL, false, true, false);
        styleTitle(chart);
        styleBarPlot((CategoryPlot) chart.getPlot(), PURPLE);
        return chart;
    }

    // ════════════════════════════════════════════════
    // 14. Shared vs non-shared pie (admin)
    // ════════════════════════════════════════════════
    public static JFreeChart createSharedRatioPieChart(int sharedCount, int totalCount) {
        DefaultPieDataset ds = new DefaultPieDataset();
        ds.setValue("共享课程", sharedCount);
        ds.setValue("非共享课程", totalCount - sharedCount);
        JFreeChart chart = ChartFactory.createPieChart(
                "共享课程占比", ds, true, true, false);
        styleTitle(chart);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(BG);
        plot.setOutlineVisible(false);
        plot.setLabelFont(PIE_LABEL_FONT);
        plot.setLabelBackgroundPaint(new Color(0xFF, 0xFF, 0xFF, 0xE0));
        plot.setLabelShadowPaint(null);
        plot.setLabelOutlinePaint(new Color(0xDD, 0xDD, 0xDD));
        plot.setIgnoreNullValues(true);
        plot.setIgnoreZeroValues(true);
        plot.setSectionPaint("共享课程", GREEN);
        plot.setSectionPaint("非共享课程", new Color(0xCC, 0xCC, 0xCC));
        plot.setExplodePercent("共享课程", 0.08);
        return chart;
    }
}
