package io.hahnsoftware.emp.ui;

import java.awt.Color;

/**
 * Color Palette
 */
public interface StyleConstants {
    static final Color MAIN_COLOR = new Color(57, 145, 169);     // #3991a9 - Muted Blue-Green Base

    static final Color MAIN_DARKER = new Color(41, 128, 152);    // Deeper version of main color
    static final Color MAIN_LIGHTER = new Color(82, 170, 194);   // Lighter version of main color


    static final Color NEUTRAL_LIGHT = new Color(240, 243, 245);    // Very light gray with blue hint
    static final Color NEUTRAL_MEDIUM = new Color(207, 216, 220);   // Soft medium gray
    static final Color NEUTRAL_DARK = new Color(84, 99, 108);       // Deep grayish blue


    static final Color ACCENT_WARM = new Color(210, 120, 87);    // Complementary warm tone

    static final Color ACCENT_COOL = new Color(95, 158, 160);    // Analogous cool tone

    static final Color SUCCESS = new Color(76, 175, 80);         // Soft Green
    static final Color WARNING = new Color(255, 152, 0);         // Warm Orange
    static final Color DANGER = new Color(244, 67, 54);          // Vibrant Red
    static final Color INFO = new Color(33, 150, 243);           // Bright Blue

    static final Color BG_PRIMARY = new Color(252, 253, 255);    // Almost white with blue undertone
    static final Color BG_SECONDARY = new Color(240, 245, 248);  // Light blue-gray

    static final Color TEXT_PRIMARY = new Color(33, 33, 33);     // Deep charcoal
    static final Color TEXT_SECONDARY = new Color(97, 97, 97);   // Soft gray
    static final Color TEXT_LIGHT = new Color(255, 255, 255);    // Pure white

    static final  Color TABLE_HEADER_COLOR = new Color(51, 51, 51);
    static final Color BORDER_LIGHT = new Color(224, 224, 224);  // Light gray
    static final Color BORDER_DARK = new Color(158, 158, 158);   // Medium gray
    static final Color HOVER_COLOR = new Color(200, 230, 255);   // Soft blue hover effect
}