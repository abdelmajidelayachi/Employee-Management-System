package io.hahnsoftware.emp.ui.button;

import io.hahnsoftware.emp.ui.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * model btn
 */
public class MButton extends JButton implements StyleConstants {
    
    public enum ButtonType {
        PRIMARY(MAIN_COLOR, MAIN_DARKER, TEXT_LIGHT),
        SECONDARY(NEUTRAL_LIGHT, NEUTRAL_MEDIUM, TEXT_PRIMARY),
        BTN_SUCCESS(SUCCESS, new Color(69, 160, 73), TEXT_LIGHT),
        BTN_WARNING(WARNING, new Color(230, 137, 0), TEXT_LIGHT),
        BTN_DANGER(DANGER, new Color(220, 60, 48), TEXT_LIGHT),
        BTN_INFO(INFO, new Color(30, 135, 219), TEXT_LIGHT);

        private final Color baseColor;
        private final Color hoverColor;
        private final Color textColor;

        ButtonType(Color baseColor, Color hoverColor, Color textColor) {
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.textColor = textColor;
        }
    }

    private ButtonType buttonType;
    private Color baseColor;
    private Color hoverColor;
    private Color textColor;
    private boolean isRounded = true;
    private int arcSize = 8;
    private Dimension buttonSize;
    private boolean isAnimated = true;

    public MButton(String text) {
        this(text, ButtonType.PRIMARY);
    }

    public MButton(String text, ButtonType type) {
        super(text);
        this.buttonType = type;
        setupButton();
    }
    private void setupButton() {
        // Set colors based on button type
        this.baseColor = buttonType.baseColor;
        this.hoverColor = buttonType.hoverColor;
        this.textColor = buttonType.textColor;

        // Basic setup
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Set initial colors
        setBackground(baseColor);  // Add this line to set initial background
        setForeground(textColor);

        // Default size and padding
        setPreferredSize(new Dimension(120, 40));
        setBorder(new EmptyBorder(8, 16, 8, 16));

        // Add hover and click effects
        addMouseListener(new ButtonStateHandler());
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isEnabled()) {
            // Add disabled state styling
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Paint disabled background with transparency
            Color disabledColor = new Color(
                    baseColor.getRed(),
                    baseColor.getGreen(),
                    baseColor.getBlue(),
                    128  // Set alpha for transparency
            );

            if (isRounded) {
                g2.setColor(disabledColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arcSize, arcSize));
            } else {
                g2.setColor(disabledColor);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            // Paint disabled text
            FontMetrics metrics = g2.getFontMetrics(getFont());
            Rectangle stringBounds = metrics.getStringBounds(getText(), g2).getBounds();

            int x = (getWidth() - stringBounds.width) / 2;
            int y = (getHeight() - stringBounds.height) / 2 + metrics.getAscent();

            g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 128));
            g2.drawString(getText(), x, y);

            g2.dispose();
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Paint background using current background color
        Color bgColor = getBackground() != null ? getBackground() : baseColor;
        g2.setColor(bgColor);

        if (isRounded) {
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arcSize, arcSize));
        } else {
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // Paint text
        FontMetrics metrics = g2.getFontMetrics(getFont());
        Rectangle stringBounds = metrics.getStringBounds(getText(), g2).getBounds();

        int x = (getWidth() - stringBounds.width) / 2;
        int y = (getHeight() - stringBounds.height) / 2 + metrics.getAscent();

        g2.setColor(getForeground());
        g2.drawString(getText(), x, y);

        g2.dispose();
    }

    private class ButtonStateHandler extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            if (isEnabled()) {
                setBackground(hoverColor);
                if (isAnimated) {
                    increasePadding();
                }
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (isEnabled()) {
                setBackground(baseColor);
                if (isAnimated) {
                    resetPadding();
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (isEnabled()) {
                setBackground(hoverColor.darker());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isEnabled()) {
                setBackground(contains(e.getPoint()) ? hoverColor : baseColor);
            }
        }
    }

    private void increasePadding() {
        setBorder(new EmptyBorder(8, 18, 8, 18));
    }

    private void resetPadding() {
        setBorder(new EmptyBorder(8, 16, 8, 16));
    }

    // Customization methods
    public MButton withRounded(boolean rounded) {
        this.isRounded = rounded;
        return this;
    }

    public MButton withArcSize(int arcSize) {
        this.arcSize = arcSize;
        return this;
    }

    public MButton withAnimation(boolean animated) {
        this.isAnimated = animated;
        return this;
    }

    public MButton withSize(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        return this;
    }

    public MButton withFont(Font font) {
        setFont(font);
        return this;
    }

    public MButton withColors(Color baseColor, Color hoverColor, Color textColor) {
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        this.textColor = textColor;
        setForeground(textColor);
        return this;
    }

    public MButton withPadding(int top, int left, int bottom, int right) {
        setBorder(new EmptyBorder(top, left, bottom, right));
        return this;
    }
}