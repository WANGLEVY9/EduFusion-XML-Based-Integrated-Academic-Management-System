package edu.fusion.common.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class StatsCardPanel extends JPanel {

    private final Card[] cards = new Card[4];

    private static final Color BG_COLOR = new Color(0xF0, 0xF2, 0xF5);
    private static final Color[] ACCENT_COLORS = {
        new Color(0x4A, 0x90, 0xD9),
        new Color(0x50, 0xC8, 0x78),
        new Color(0xFF, 0x8C, 0x42),
        new Color(0x9B, 0x59, 0xB6)
    };
    private static final String[] LABELS = {"总学生数", "总课程数", "总选课数", "共享课程数"};

    public StatsCardPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 16, 12));
        setBackground(BG_COLOR);
        for (int i = 0; i < 4; i++) {
            cards[i] = new Card(LABELS[i], ACCENT_COLORS[i]);
            add(cards[i]);
        }
    }

    public void setData(int students, int courses, int selections, int shared) {
        cards[0].setValue(students);
        cards[1].setValue(courses);
        cards[2].setValue(selections);
        cards[3].setValue(shared);
    }

    public void reset() {
        for (Card card : cards) {
            card.setValue(0);
        }
    }

    private static class Card extends JPanel {
        private final JLabel valueLabel;

        Card(String title, Color accentColor) {
            setPreferredSize(new Dimension(165, 85));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0)),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)));
            setLayout(new BorderLayout(10, 4));

            JPanel accent = new JPanel();
            accent.setPreferredSize(new Dimension(6, 65));
            accent.setBackground(accentColor);
            add(accent, BorderLayout.WEST);

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            textPanel.setBackground(Color.WHITE);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            titleLabel.setForeground(new Color(0x88, 0x88, 0x88));
            textPanel.add(titleLabel);

            valueLabel = new JLabel("--");
            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
            valueLabel.setForeground(new Color(0x33, 0x33, 0x33));
            textPanel.add(valueLabel);

            add(textPanel, BorderLayout.CENTER);
        }

        void setValue(int value) {
            valueLabel.setText(String.valueOf(value));
        }
    }
}
