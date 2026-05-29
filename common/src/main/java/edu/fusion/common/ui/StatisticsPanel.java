package edu.fusion.common.ui;

import edu.fusion.common.model.CourseHeat;
import edu.fusion.common.util.XmlUtil;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsPanel extends JPanel {

    // ─── Chart type definitions ───
    private enum ChartType {
        COLLEGE_COMPARE, SELECTION_PIE, STUDENT_RING, STACKED_BAR,
        LINE_TREND, AREA_CHART, SHARED_BAR, DENSITY_BAR,
        COURSE_PIE, TOP10_BAR, CREDIT_SCATTER,
        // Admin-only
        TEACHER_WORKLOAD, CREDIT_DIST, SHARED_RATIO
    }

    private static class ChartDef {
        final String title;
        final String icon;
        final Color color;
        final ChartType type;
        final boolean adminOnly;
        final String description;

        ChartDef(String title, String icon, Color color, ChartType type, boolean adminOnly, String description) {
            this.title = title;
            this.icon = icon;
            this.color = color;
            this.type = type;
            this.adminOnly = adminOnly;
            this.description = description;
        }
    }

    private static final ChartDef[] ALL_CHARTS = {
        new ChartDef("学院数据对比", "📊", new Color(0x4A, 0x90, 0xD9), ChartType.COLLEGE_COMPARE, false, "分组柱状图对比三学院学生数、课程数、选课数"),
        new ChartDef("选课学院分布", "🥧", new Color(0x50, 0xC8, 0x78), ChartType.SELECTION_PIE, false, "饼图展示各学院选课数量占比"),
        new ChartDef("各学院学生分布", "⭕", new Color(0xFF, 0x8C, 0x42), ChartType.STUDENT_RING, false, "环形图展示各学院学生人数占比"),
        new ChartDef("三学院指标堆积", "📊", new Color(0x9B, 0x59, 0xB6), ChartType.STACKED_BAR, false, "堆积柱状图综合对比三学院多维度数据"),
        new ChartDef("三学院数据趋势", "📈", new Color(0x1A, 0xBC, 0x9C), ChartType.LINE_TREND, false, "折线图展示三学院各项指标变化趋势"),
        new ChartDef("三学院指标面积", "📉", new Color(0xE7, 0x4C, 0x3C), ChartType.AREA_CHART, false, "面积图展示三学院各项指标累积对比"),
        new ChartDef("课程共享情况", "📊", new Color(0x34, 0x98, 0xDB), ChartType.SHARED_BAR, false, "各学院总课程与共享课程数量对比"),
        new ChartDef("人均选课密度", "📊", new Color(0xE6, 0x7E, 0x22), ChartType.DENSITY_BAR, false, "各学院人均选课数（选课密度）对比"),
        new ChartDef("各学院课程占比", "🥧", new Color(0x2E, 0xCC, 0x71), ChartType.COURSE_PIE, false, "饼图展示各学院课程数量占比"),
        new ChartDef("热门课程 TOP10", "🏆", new Color(0xF1, 0xC4, 0x0F), ChartType.TOP10_BAR, false, "横向柱状图展示选课人数最多的前10门课程"),
        new ChartDef("学分 vs 选课人数", "📊", new Color(0x8E, 0x44, 0xAD), ChartType.CREDIT_SCATTER, false, "热门课程学分与选课人数关系分析"),
        // Admin-only charts
        new ChartDef("教师课程负荷", "👨‍🏫", new Color(0xC0, 0x39, 0x2B), ChartType.TEACHER_WORKLOAD, true, "横向柱状图展示各教师任教课程数量（管理员）"),
        new ChartDef("学分分布分析", "🎓", new Color(0xD4, 0x6A, 0x00), ChartType.CREDIT_DIST, true, "各学分值对应的课程数量分布（管理员）"),
        new ChartDef("共享课程占比", "🔄", new Color(0x1A, 0x8C, 0x8C), ChartType.SHARED_RATIO, true, "全系统共享课程与非共享课程比例（管理员）"),
    };

    // ─── Constants ───
    private static final Color BG_COLOR = new Color(0xF0, 0xF2, 0xF5);
    private static final Color ACCENT_BLUE = new Color(0x29, 0x80, 0xB9);
    private static final Color CARD_BORDER = new Color(0xD0, 0xD0, 0xD0);
    private static final Color CARD_HOVER = new Color(0xE8, 0xEE, 0xF4);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font CARD_FONT = new Font("SansSerif", Font.BOLD, 12);
    private static final Font DESC_FONT = new Font("SansSerif", Font.PLAIN, 10);
    private static final Color[] CARD_PALETTE;

    static {
        CARD_PALETTE = new Color[]{
            new Color(0x4A, 0x90, 0xD9), new Color(0x50, 0xC8, 0x78),
            new Color(0xFF, 0x8C, 0x42), new Color(0x9B, 0x59, 0xB6),
            new Color(0x1A, 0xBC, 0x9C), new Color(0xE7, 0x4C, 0x3C),
            new Color(0x34, 0x98, 0xDB), new Color(0xE6, 0x7E, 0x22),
            new Color(0x2E, 0xCC, 0x71), new Color(0xF1, 0xC4, 0x0F),
            new Color(0x8E, 0x44, 0xAD),
        };
    }

    // ─── Fields ───
    private final StatsCardPanel cardPanel = new StatsCardPanel();
    private final Runnable loadCallback;
    private final boolean admin;

    // Card layout navigation
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private static final String CARD_PLACEHOLDER = "placeholder";
    private static final String CARD_GALLERY = "gallery";
    private static final String CARD_DETAIL = "detail";

    // Gallery
    private final JPanel galleryGrid = new JPanel(new GridLayout(0, 3, 12, 12));

    // Detail view
    private final JPanel detailPanel = new JPanel(new BorderLayout(8, 8));
    private final ChartPanel detailChartPanel = new ChartPanel(null);
    private final JLabel detailTitle = new JLabel();
    private final JButton backBtn = new JButton("← 返回");
    private final JPanel detailFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private JCheckBox[] collegeChecks;
    private final JComboBox<String> teacherCombo = new JComboBox<>();
    private final JButton applyFilterBtn = new JButton("应用筛选");
    private final JButton resetFilterBtn = new JButton("重置");
    private final JLabel detailMsg = new JLabel("", SwingConstants.CENTER);
    private ChartType activeChartType = null;

    // Cached parsed data
    private List<CollegeData> colleges = Collections.emptyList();
    private List<CourseHeat> topCourses = Collections.emptyList();
    private List<AllCourseData> allCourses = Collections.emptyList();
    private int[] totals = new int[4];
    private boolean dataLoaded = false;
    private int totalSharedCount = 0;

    // ════════════════════════════════════════════════
    // Constructor
    // ════════════════════════════════════════════════

    public StatisticsPanel(Runnable loadCallback, boolean admin) {
        this.loadCallback = loadCallback;
        this.admin = admin;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(cardPanel, BorderLayout.NORTH);

        // ── Card 0: Placeholder ──
        mainPanel.add(createPlaceholderPanel(), CARD_PLACEHOLDER);

        // ── Card 1: Gallery ──
        galleryGrid.setBackground(BG_COLOR);
        JScrollPane galleryScroll = new JScrollPane(galleryGrid);
        galleryScroll.setBorder(null);
        galleryScroll.getVerticalScrollBar().setUnitIncrement(20);

        JPanel galleryPanel = new JPanel(new BorderLayout());
        galleryPanel.setBackground(BG_COLOR);
        galleryPanel.add(galleryScroll, BorderLayout.CENTER);
        mainPanel.add(galleryPanel, CARD_GALLERY);

        // ── Card 2: Detail ──
        buildDetailPanel();
        mainPanel.add(detailPanel, CARD_DETAIL);

        add(mainPanel, BorderLayout.CENTER);

        showPlaceholder();
    }

    // ════════════════════════════════════════════════
    // Placeholder
    // ════════════════════════════════════════════════

    private JPanel createPlaceholderPanel() {
        JPanel placeholder = new JPanel();
        placeholder.setLayout(new BoxLayout(placeholder, BoxLayout.Y_AXIS));
        placeholder.setBackground(BG_COLOR);
        placeholder.setAlignmentX(Component.CENTER_ALIGNMENT);

        placeholder.add(Box.createVerticalGlue());
        JLabel iconLabel = new JLabel("📊", SwingConstants.CENTER);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeholder.add(iconLabel);

        placeholder.add(Box.createVerticalStrut(10));
        JLabel msgLabel = new JLabel("尚未加载统计数据", SwingConstants.CENTER);
        msgLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        msgLabel.setForeground(new Color(0x99, 0x99, 0x99));
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeholder.add(msgLabel);

        placeholder.add(Box.createVerticalStrut(6));
        JLabel tipLabel = new JLabel("点击下方按钮获取各学院整体数据概览", SwingConstants.CENTER);
        tipLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tipLabel.setForeground(new Color(0xBB, 0xBB, 0xBB));
        tipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeholder.add(tipLabel);

        placeholder.add(Box.createVerticalStrut(16));

        JButton loadButton = new JButton("加载统计数据");
        loadButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loadButton.setBackground(ACCENT_BLUE);
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);
        loadButton.setPreferredSize(new Dimension(180, 40));
        loadButton.setMaximumSize(new Dimension(180, 40));
        loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loadButton.addActionListener(e -> {
            if (loadCallback != null) {
                loadButton.setEnabled(false);
                loadButton.setText("加载中...");
                loadCallback.run();
            }
        });
        placeholder.add(loadButton);
        placeholder.add(Box.createVerticalGlue());
        return placeholder;
    }

    private void showPlaceholder() {
        cardLayout.show(mainPanel, CARD_PLACEHOLDER);
        dataLoaded = false;
    }

    // ════════════════════════════════════════════════
    // Detail panel builder
    // ════════════════════════════════════════════════

    private void buildDetailPanel() {
        detailPanel.setBackground(BG_COLOR);

        // Header: back button + title
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(BG_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));

        backBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, CARD_GALLERY));
        header.add(backBtn, BorderLayout.WEST);

        detailTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        detailTitle.setForeground(new Color(0x33, 0x33, 0x33));
        header.add(detailTitle, BorderLayout.CENTER);

        detailPanel.add(header, BorderLayout.NORTH);

        // Center: filter + chart
        JPanel center = new JPanel(new BorderLayout(4, 4));
        center.setBackground(BG_COLOR);

        // Filter bar within detail view
        collegeChecks = new JCheckBox[]{
                new JCheckBox("A学院", true),
                new JCheckBox("B学院", true),
                new JCheckBox("C学院", true)
        };
        detailFilterPanel.setBackground(BG_COLOR);
        detailFilterPanel.add(new JLabel("学院:"));
        for (JCheckBox cb : collegeChecks) {
            cb.setOpaque(false);
            detailFilterPanel.add(cb);
        }
        detailFilterPanel.add(Box.createHorizontalStrut(8));
        detailFilterPanel.add(new JLabel("教师:"));
        teacherCombo.setPreferredSize(new Dimension(150, 24));
        detailFilterPanel.add(teacherCombo);
        detailFilterPanel.add(resetFilterBtn);
        detailFilterPanel.add(applyFilterBtn);

        resetFilterBtn.addActionListener(e -> {
            for (JCheckBox cb : collegeChecks) cb.setSelected(true);
            if (teacherCombo.getItemCount() > 0) teacherCombo.setSelectedIndex(0);
            rebuildActiveChart();
        });
        applyFilterBtn.addActionListener(e -> rebuildActiveChart());

        center.add(detailFilterPanel, BorderLayout.NORTH);

        // Chart area
        detailChartPanel.setPreferredSize(new Dimension(800, 500));
        detailChartPanel.setMinimumSize(new Dimension(400, 300));
        detailChartPanel.setMouseWheelEnabled(true);
        JScrollPane chartScroll = new JScrollPane(detailChartPanel);
        chartScroll.setBorder(null);
        center.add(chartScroll, BorderLayout.CENTER);

        detailMsg.setFont(new Font("SansSerif", Font.PLAIN, 16));
        detailMsg.setForeground(new Color(0x99, 0x99, 0x99));
        detailMsg.setVisible(false);
        center.add(detailMsg, BorderLayout.SOUTH);

        detailPanel.add(center, BorderLayout.CENTER);
    }

    // ════════════════════════════════════════════════
    // Entry point — called with statistics XML
    // ════════════════════════════════════════════════

    public void loadStatistics(String responseXml) {
        dataLoaded = true;
        Document doc = XmlUtil.parse(responseXml);
        Element root = doc.getDocumentElement();

        if (!isSuccess(root)) {
            showError(extractMessage(root));
            return;
        }

        Element stats = findStats(root);
        if (stats == null) {
            showEmptyState();
            return;
        }

        totals = extractTotals(stats);
        colleges = extractCollegeData(stats);
        topCourses = extractTopCourses(stats);
        allCourses = extractAllCoursesData(stats);
        totalSharedCount = totals[3];

        cardPanel.setData(totals[0], totals[1], totals[2], totals[3]);
        populateTeacherFilter();
        buildGallery();
        cardLayout.show(mainPanel, CARD_GALLERY);

        // Update detail filter with teacher data if it was already open
        rebuildActiveChart();
    }

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    // ════════════════════════════════════════════════
    // Gallery
    // ════════════════════════════════════════════════

    private void buildGallery() {
        galleryGrid.removeAll();

        for (int i = 0; i < ALL_CHARTS.length; i++) {
            ChartDef def = ALL_CHARTS[i];
            if (def.adminOnly && !admin) continue;
            galleryGrid.add(createCard(def, i));
        }

        galleryGrid.revalidate();
        galleryGrid.repaint();
    }

    private JPanel createCard(ChartDef def, int index) {
        Color accent = CARD_PALETTE[index % CARD_PALETTE.length];

        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 14, 16)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Color accent stripe at top
        JPanel stripe = new JPanel();
        stripe.setPreferredSize(new Dimension(0, 4));
        stripe.setBackground(accent);
        card.add(stripe, BorderLayout.NORTH);

        // Center: icon + title + description
        JPanel content = new JPanel(new BorderLayout(4, 4));
        content.setBackground(Color.WHITE);

        JPanel iconTitleRow = new JPanel(new BorderLayout(8, 0));
        iconTitleRow.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel(def.icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));
        iconTitleRow.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new BorderLayout(0, 2));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(def.title);
        titleLabel.setFont(CARD_FONT);
        titleLabel.setForeground(new Color(0x33, 0x33, 0x33));
        textPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel descLabel = new JLabel(def.description);
        descLabel.setFont(DESC_FONT);
        descLabel.setForeground(new Color(0x99, 0x99, 0x99));
        textPanel.add(descLabel, BorderLayout.SOUTH);

        iconTitleRow.add(textPanel, BorderLayout.CENTER);
        content.add(iconTitleRow, BorderLayout.NORTH);

        if (def.adminOnly) {
            JLabel badge = new JLabel("管理员专属");
            badge.setFont(new Font("SansSerif", Font.PLAIN, 10));
            badge.setForeground(new Color(0xE7, 0x4C, 0x3C));
            badge.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
            content.add(badge, BorderLayout.EAST);
        }

        card.add(content, BorderLayout.CENTER);

        // Hover + click
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDetail(def);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(CARD_HOVER);
                content.setBackground(CARD_HOVER);
                textPanel.setBackground(CARD_HOVER);
                iconTitleRow.setBackground(CARD_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
                content.setBackground(Color.WHITE);
                textPanel.setBackground(Color.WHITE);
                iconTitleRow.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    // ════════════════════════════════════════════════
    // Detail navigation
    // ════════════════════════════════════════════════

    private void openDetail(ChartDef def) {
        activeChartType = def.type;
        detailTitle.setText(def.icon + "  " + def.title);
        populateTeacherFilter();
        rebuildActiveChart();
        applyFilterBtn.setText("应用筛选");
        cardLayout.show(mainPanel, CARD_DETAIL);
    }

    private void rebuildActiveChart() {
        if (activeChartType == null) return;

        List<CollegeData> cols = getFilteredColleges();
        String teacher = getSelectedTeacher();

        if (cols.isEmpty()) {
            detailChartPanel.setChart(null);
            detailMsg.setText("请至少选择一个学院");
            detailMsg.setVisible(true);
            detailChartPanel.setVisible(false);
            return;
        }

        JFreeChart chart = buildChart(activeChartType, cols, teacher);
        if (chart == null) {
            detailChartPanel.setChart(null);
            detailMsg.setText("当前筛选条件下数据不足以生成此图表（需要至少两个学院）");
            detailMsg.setVisible(true);
            detailChartPanel.setVisible(false);
        } else {
            detailChartPanel.setChart(chart);
            detailChartPanel.setVisible(true);
            detailMsg.setVisible(false);
        }
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    // ════════════════════════════════════════════════
    // Chart builder
    // ════════════════════════════════════════════════

    private JFreeChart buildChart(ChartType type, List<CollegeData> cols, String teacher) {
        String[] names = cols.stream().map(c -> c.name).toArray(String[]::new);
        int[] students = cols.stream().mapToInt(c -> c.students).toArray();
        int[] courses = cols.stream().mapToInt(c -> c.courses).toArray();
        int[] selections = cols.stream().mapToInt(c -> c.selections).toArray();
        int[] shared = cols.stream().mapToInt(c -> c.sharedCourses).toArray();

        boolean multiple = cols.size() >= 2;

        // Teacher-filtered course data
        List<CourseHeat> filteredTop = filterTopByTeacher(teacher);
        List<AllCourseData> filteredAll = filterAllByTeacher(teacher);

        switch (type) {
            case COLLEGE_COMPARE:
                if (!multiple) return null;
                return Charts.createCollegeCompareChart(names, students, courses, selections);
            case SELECTION_PIE:
                return Charts.createSelectionPieChart(names, selections);
            case STUDENT_RING:
                return Charts.createStudentRingChart(names, students);
            case STACKED_BAR:
                if (!multiple) return null;
                return Charts.createCollegeStackedBarChart(names, students, courses, selections);
            case LINE_TREND:
                if (!multiple) return null;
                return Charts.createCollegeLineChart(names, students, courses, selections);
            case AREA_CHART:
                if (!multiple) return null;
                return Charts.createCollegeAreaChart(names, students, courses, selections);
            case SHARED_BAR:
                if (!multiple) return null;
                return Charts.createSharedCoursesBarChart(names, shared, courses);
            case DENSITY_BAR:
                return Charts.createSelectionDensityChart(names, students, selections);
            case COURSE_PIE:
                return Charts.createCoursePieChart(names, courses);
            case TOP10_BAR:
                if (filteredTop.isEmpty()) return null;
                return Charts.createTopCoursesChart(filteredTop);
            case CREDIT_SCATTER:
                if (filteredTop.isEmpty() || filteredAll.isEmpty()) return null;
                Object[][] cd = buildCreditScatterData(filteredTop, filteredAll, cols);
                if (cd.length == 0) return null;
                return Charts.createCreditScatterChart(cd, names);
            // Admin charts
            case TEACHER_WORKLOAD:
                return buildTeacherWorkloadChart(filteredAll.isEmpty() ? allCourses : filteredAll);
            case CREDIT_DIST:
                return buildCreditDistChart(filteredAll.isEmpty() ? allCourses : filteredAll);
            case SHARED_RATIO:
                return buildSharedRatioChart(filteredAll.isEmpty() ? allCourses : filteredAll);
            default:
                return null;
        }
    }

    private JFreeChart buildTeacherWorkloadChart(List<AllCourseData> courses) {
        Map<String, Long> groups = courses.stream()
                .filter(c -> c.teacher != null && !c.teacher.trim().isEmpty())
                .collect(Collectors.groupingBy(c -> c.teacher, Collectors.counting()));
        String[] names = groups.keySet().toArray(new String[0]);
        int[] counts = groups.values().stream().mapToInt(Long::intValue).toArray();
        if (names.length == 0) return null;
        return Charts.createTeacherWorkloadChart(names, counts);
    }

    private JFreeChart buildCreditDistChart(List<AllCourseData> courses) {
        Map<Integer, Long> groups = courses.stream()
                .collect(Collectors.groupingBy(c -> c.credit, Collectors.counting()));
        List<Integer> sortedCredits = new ArrayList<>(groups.keySet());
        Collections.sort(sortedCredits);
        String[] labels = sortedCredits.stream().map(String::valueOf).toArray(String[]::new);
        int[] counts = sortedCredits.stream().mapToInt(k -> groups.get(k).intValue()).toArray();
        if (labels.length == 0) return null;
        return Charts.createCreditDistributionChart(labels, counts);
    }

    private JFreeChart buildSharedRatioChart(List<AllCourseData> courses) {
        long shared = courses.stream().filter(c -> c.shared).count();
        int total = courses.size();
        if (total == 0) return null;
        return Charts.createSharedRatioPieChart((int) shared, total);
    }

    // ════════════════════════════════════════════════
    // Filters
    // ════════════════════════════════════════════════

    private List<CollegeData> getFilteredColleges() {
        List<CollegeData> result = new ArrayList<>();
        for (int i = 0; i < collegeChecks.length && i < colleges.size(); i++) {
            if (collegeChecks[i].isSelected()) {
                result.add(colleges.get(i));
            }
        }
        return result;
    }

    private String getSelectedTeacher() {
        Object sel = teacherCombo.getSelectedItem();
        if (sel == null || "全部教师".equals(sel.toString())) return null;
        return sel.toString();
    }

    private void populateTeacherFilter() {
        teacherCombo.removeAllItems();
        teacherCombo.addItem("全部教师");
        allCourses.stream()
                .map(c -> c.teacher)
                .filter(t -> t != null && !t.trim().isEmpty())
                .distinct()
                .sorted()
                .forEach(t -> teacherCombo.addItem(t));
    }

    private List<CourseHeat> filterTopByTeacher(String teacher) {
        if (teacher == null) return topCourses;
        return topCourses.stream()
                .filter(h -> courseHasTeacher(h.getCourseId(), teacher))
                .collect(Collectors.toList());
    }

    private List<AllCourseData> filterAllByTeacher(String teacher) {
        if (teacher == null) return allCourses;
        return allCourses.stream()
                .filter(c -> teacher.equals(c.teacher))
                .collect(Collectors.toList());
    }

    private boolean courseHasTeacher(String courseId, String teacher) {
        return allCourses.stream().anyMatch(c -> courseId.equals(c.id) && teacher.equals(c.teacher));
    }

    // ════════════════════════════════════════════════
    // Credit scatter builder
    // ════════════════════════════════════════════════

    private Object[][] buildCreditScatterData(List<CourseHeat> heats, List<AllCourseData> all,
                                              List<CollegeData> cols) {
        Map<String, Integer> creditMap = new HashMap<>();
        Map<String, String> courseCollegeMap = new HashMap<>();
        for (AllCourseData c : all) {
            creditMap.put(c.id, c.credit);
            courseCollegeMap.put(c.id, c.college);
        }

        List<Object[]> rows = new ArrayList<>();
        for (CourseHeat h : heats) {
            Integer credit = creditMap.get(h.getCourseId());
            if (credit != null) {
                String college = h.getCollege() != null ? h.getCollege()
                        : courseCollegeMap.getOrDefault(h.getCourseId(), "");
                int ci = -1;
                for (int i = 0; i < cols.size(); i++) {
                    String colName = cols.get(i).name.replace("学院", "");
                    if (college.contains(colName) || colName.contains(college)) {
                        ci = i;
                        break;
                    }
                }
                if (ci >= 0) {
                    rows.add(new Object[]{credit, h.getSelectedCount(), ci});
                }
            }
        }
        return rows.toArray(new Object[0][]);
    }

    // ════════════════════════════════════════════════
    // XML parsing
    // ════════════════════════════════════════════════

    private static boolean isSuccess(Element root) {
        return "true".equalsIgnoreCase(XmlUtil.childText(root, "success"));
    }

    private static String extractMessage(Element root) {
        return XmlUtil.childText(root, "message");
    }

    private static Element findStats(Element root) {
        NodeList list = root.getElementsByTagName("statistics");
        return list.getLength() > 0 ? (Element) list.item(0) : null;
    }

    private static int[] extractTotals(Element stats) {
        return new int[]{
                parseInt(XmlUtil.childText(stats, "totalStudents")),
                parseInt(XmlUtil.childText(stats, "totalCourses")),
                parseInt(XmlUtil.childText(stats, "totalSelections")),
                parseInt(XmlUtil.childText(stats, "totalSharedCourses"))
        };
    }

    private static List<CollegeData> extractCollegeData(Element stats) {
        List<CollegeData> result = new ArrayList<>();
        NodeList collegesNodes = stats.getElementsByTagName("colleges");
        if (collegesNodes.getLength() == 0) return result;

        NodeList collegeList = ((Element) collegesNodes.item(0)).getElementsByTagName("college");
        for (int i = 0; i < collegeList.getLength(); i++) {
            Element c = (Element) collegeList.item(i);
            result.add(new CollegeData(
                    XmlUtil.childText(c, "code") + "学院",
                    parseInt(XmlUtil.childText(c, "students")),
                    parseInt(XmlUtil.childText(c, "courses")),
                    parseInt(XmlUtil.childText(c, "selections")),
                    parseInt(XmlUtil.childText(c, "sharedCourses"))
            ));
        }
        return result;
    }

    private static List<CourseHeat> extractTopCourses(Element stats) {
        List<CourseHeat> result = new ArrayList<>();
        NodeList topNodes = stats.getElementsByTagName("topCourses");
        if (topNodes.getLength() == 0) return result;

        NodeList courseList = ((Element) topNodes.item(0)).getElementsByTagName("course");
        for (int i = 0; i < courseList.getLength(); i++) {
            Element c = (Element) courseList.item(i);
            result.add(new CourseHeat(
                    XmlUtil.childText(c, "id"),
                    XmlUtil.childText(c, "name"),
                    XmlUtil.childText(c, "college"),
                    parseInt(XmlUtil.childText(c, "selectedCount"))
            ));
        }
        return result;
    }

    private static List<AllCourseData> extractAllCoursesData(Element stats) {
        List<AllCourseData> result = new ArrayList<>();
        NodeList allNodes = stats.getElementsByTagName("allCourses");
        if (allNodes.getLength() == 0) return result;

        NodeList courseList = ((Element) allNodes.item(0)).getElementsByTagName("course");
        for (int i = 0; i < courseList.getLength(); i++) {
            Element c = (Element) courseList.item(i);
            result.add(new AllCourseData(
                    XmlUtil.childText(c, "id"),
                    XmlUtil.childText(c, "name"),
                    parseInt(XmlUtil.childText(c, "credit")),
                    XmlUtil.childText(c, "teacher"),
                    XmlUtil.childText(c, "location"),
                    XmlUtil.childText(c, "college"),
                    "true".equalsIgnoreCase(XmlUtil.childText(c, "shared"))
            ));
        }
        return result;
    }

    // ════════════════════════════════════════════════
    // Error / Empty
    // ════════════════════════════════════════════════

    private void showError(String message) {
        cardPanel.reset();
        dataLoaded = false;
        showPlaceholder();
    }

    private void showEmptyState() {
        cardPanel.reset();
        dataLoaded = false;
        showPlaceholder();
    }

    private static int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ════════════════════════════════════════════════
    // Internal models
    // ════════════════════════════════════════════════

    private static class CollegeData {
        final String name;
        final int students;
        final int courses;
        final int selections;
        final int sharedCourses;

        CollegeData(String name, int students, int courses, int selections, int sharedCourses) {
            this.name = name;
            this.students = students;
            this.courses = courses;
            this.selections = selections;
            this.sharedCourses = sharedCourses;
        }
    }

    private static class AllCourseData {
        final String id;
        final String name;
        final int credit;
        final String teacher;
        final String location;
        final String college;
        final boolean shared;

        AllCourseData(String id, String name, int credit, String teacher,
                      String location, String college, boolean shared) {
            this.id = id;
            this.name = name;
            this.credit = credit;
            this.teacher = teacher;
            this.location = location;
            this.college = college;
            this.shared = shared;
        }
    }
}
