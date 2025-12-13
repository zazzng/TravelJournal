package tj;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

public class TJColorChooser {
    // constants
    private static final int CELL_NUM_H = 40;
    private static final int CELL_NUM_B = 11;
    private static final float SATURATION_DEFAULT = 1f;
    private static final float OPAQUENESS_DEFAULT = 1f;
    private static final double WIDTH_RATIO = 0.75;
    
    // fields
    private Color[][] mColors = null;
    private float mSaturation = Float.NaN;
    private float mOpaqueness = Float.NaN;
    
    // constructor
    public TJColorChooser() {
        this.mColors = new Color[TJColorChooser.CELL_NUM_B][];
        for (int i = 0; i < TJColorChooser.CELL_NUM_B; i++) {
            this.mColors[i] = new Color[TJColorChooser.CELL_NUM_H];
        }
        this.mSaturation = TJColorChooser.SATURATION_DEFAULT;
        this.mOpaqueness = TJColorChooser.OPAQUENESS_DEFAULT;
        
        this.createCellColors();
    }
    
    private void createCellColors() {
        // initiate cell colors
	float db = 1f / (float)(TJColorChooser.CELL_NUM_B - 1);
        float dh = 1f / (float)(TJColorChooser.CELL_NUM_H - 1);
        
        for (int i = 0; i < TJColorChooser.CELL_NUM_B; i++) {
            float b = db * (float)i;
            for (int j = 0; j < TJColorChooser.CELL_NUM_H; j++) {
                if (i == 0) {
                    // the top row is grayscale (0 saturation)
                    float gray = (float)j / (float)(
                        TJColorChooser.CELL_NUM_H - 1);
                    this.mColors[i][j] = new Color(gray, gray, gray);
                } else {
                    float h = dh * (float)j;
                    Color hsb = Color.getHSBColor(h, this.mSaturation, b);
                    this.mColors[i][j] = new Color(hsb.getRed(), hsb.getGreen(),
                        hsb.getBlue(), (int)(this.mOpaqueness * 255f));
                }
            }
        }
    }
    
    public void drawCells(Graphics2D g2, int w, int h) {
        double totalWidth = w;
        double pickerWidth = totalWidth * WIDTH_RATIO;
        double xStart = (totalWidth - pickerWidth) / 2.0;
        
        // display cell colors
        double ys = (double)h / 3.0 * 1.0;
        double ye = (double)h / 3.0 * 2.0;
        double dx = pickerWidth / (double)TJColorChooser.CELL_NUM_H;
        double dy = (ye - ys) / (double)TJColorChooser.CELL_NUM_B;
        
        for (int i = 0; i < TJColorChooser.CELL_NUM_B; i++) {
            double y = ys + dy * (double)i;
            for (int j = 0; j < TJColorChooser.CELL_NUM_H; j++) {
                double x = xStart + dx * (double)j;
                Rectangle2D rect = new Rectangle2D.Double(x, y, dx, dy);
                g2.setColor(this.mColors[i][j]);
                g2.fill(rect);
            }
        }
    }

    public Color calcColor(Point pt, int w, int h) {
        // calculate the color of a point on a canvas of width w and height h
        double ys = (double)h / 3.0 * 1.0;
        double ye = (double)h / 3.0 * 2.0;
        double dx = (double)w / (double)TJColorChooser.CELL_NUM_H;
        double dy = (ye - ys) / (double)TJColorChooser.CELL_NUM_B;
        
        int i = (int)(((double)pt.y - ys) / dy);
        int j = (int)((double)pt.x / dx);
        if (i < 0 || i >= TJColorChooser.CELL_NUM_B) {
            return null;
        } else {
            return this.mColors[i][j];
        }
    }
}