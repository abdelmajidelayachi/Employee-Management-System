package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.ui.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {
    
    public enum ButtonVariant {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        WARNING,
        INFO
    }

    public enum ButtonSize {
        SMALL(8, 16, 12),
        MEDIUM(10, 20, 14),
        LARGE(12, 24, 16);

        final int verticalPadding;
        final int horizontalPadding;
        final int fontSize;

        ButtonSize(int verticalPadding, int horizontalPadding, int fontSize) {
            this.verticalPadding = verticalPadding;
            this.horizontalPadding = horizontalPadding;
            this.fontSize = fontSize;
        }
    }

    private final ButtonVariant variant;
    private final ButtonSize size;
    private boolean loading = false;
    private String originalText;
    private Icon originalIcon;

    public ModernButton(String text) {
        this(text, ButtonVariant.PRIMARY, ButtonSize.MEDIUM);
    }

    public ModernButton(String text, ButtonVariant variant) {
        this(text, variant, ButtonSize.MEDIUM);
    }

    public ModernButton(String text, ButtonVariant variant, ButtonSize size) {
        super(text);
        this.variant = variant;
        this.size = size;
        this.originalText = text;
        setupButton();
    }

    private void setupButton() {
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFont(new Font("Segoe UI Semibold", Font.PLAIN, size.fontSize));

        // Apply initial styling
        updateStyle(false, false);
        
        // Add hover and press effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) updateStyle(true, false);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) updateStyle(false, false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) updateStyle(false, true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) updateStyle(true, false);
            }
        });
    }

    private void updateStyle(boolean hover, boolean pressed) {
        Color bgColor = getBackgroundColor(hover, pressed);
        Color fgColor = getForegroundColor();
        
        setBackground(bgColor);
        setForeground(fgColor);
        setBorder(new EmptyBorder(size.verticalPadding, size.horizontalPadding, 
                                size.verticalPadding, size.horizontalPadding));
    }

    private Color getBackgroundColor(boolean hover, boolean pressed) {
        Color baseColor = switch (variant) {
            case PRIMARY -> StyleConstants.MAIN_COLOR;
            case SECONDARY -> StyleConstants.BG_SECONDARY;
            case SUCCESS -> StyleConstants.SUCCESS;
            case DANGER -> StyleConstants.DANGER;
            case WARNING -> StyleConstants.WARNING;
            case INFO -> StyleConstants.INFO;
        };

        if (pressed) {
            return darker(baseColor);
        } else if (hover) {
            return brighter(baseColor);
        }
        return baseColor;
    }

    private Color getForegroundColor() {
        return switch (variant) {
            case PRIMARY, SECONDARY, SUCCESS, DANGER, INFO -> StyleConstants.TEXT_LIGHT;
            case WARNING -> StyleConstants.TEXT_PRIMARY;
        };
    }

    private Color brighter(Color color) {
        int r = Math.min(255, (int)(color.getRed() * 1.2));
        int g = Math.min(255, (int)(color.getGreen() * 1.2));
        int b = Math.min(255, (int)(color.getBlue() * 1.2));
        return new Color(r, g, b);
    }

    private Color darker(Color color) {
        int r = (int)(color.getRed() * 0.8);
        int g = (int)(color.getGreen() * 0.8);
        int b = (int)(color.getBlue() * 0.8);
        return new Color(r, g, b);
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        setEnabled(!loading);
        
        if (loading) {
            originalText = getText();
            originalIcon = getIcon();
            setText("Loading...");
            setIcon(null);
        } else {
            setText(originalText);
            setIcon(originalIcon);
        }
    }

    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateStyle(false, false);
        float alpha = enabled ? 1.0f : 0.6f;
        setAlpha(alpha);
    }

    private void setAlpha(float alpha) {
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        repaint();
    }
}