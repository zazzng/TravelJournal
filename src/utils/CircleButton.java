package utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.JButton;

public class CircleButton extends JButton {
    private Color mNormalColor;
    private Color mHoverColor;
    private Color mPressedColor;
    private Image mIconImage;
    private int mDiameter;

    public CircleButton(Color color, Image image, int diameter) {
        this.mNormalColor = color;
        this.mHoverColor = color.brighter();
        this.mPressedColor = color.darker();
        
        this.mIconImage = image;
        this.mDiameter = diameter;

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        
        setPreferredSize(new Dimension(diameter, diameter));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // set the color
        if (getModel().isPressed()) {
            g2.setColor(mPressedColor);
        } else if (getModel().isRollover()) {
            g2.setColor(mHoverColor);
        } else {
            g2.setColor(mNormalColor);
        }

        int diameter = Math.min(getWidth(), getHeight());
        g2.fillOval(0, 0, diameter, diameter);

        // draw icon
        if (mIconImage != null) {
            int iconW = (int) (diameter * 0.4);
            int iconH = (int) (diameter * 0.4);
            
            int x = (getWidth() - iconW) / 2;
            int y = (getHeight() - iconH) / 2;
            
            g2.drawImage(mIconImage, x, y, iconW, iconH, null);
        }

        g2.dispose();
    }

    @Override
    public boolean contains(int x, int y) {
        int radius = Math.min(getWidth(), getHeight()) / 2;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
       
        return Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) <= Math.pow(radius, 2);
    }
}