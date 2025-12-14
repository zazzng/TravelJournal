package tj;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;

public class TJCanvas2D extends JPanel {
    // constants
    private static final Color COLOR_PT_CURVE_DEFAULT = new Color(0, 0, 0);
    private static final Color COLOR_SELECTED_PT_CURVE = Color.ORANGE;
    public static final Color COLOR_SELECTION_BOX = new Color(255, 0, 0, 64);
    public static final Color COLOR_CROSS_HAIR = new Color(255, 0, 0, 64);
    
    public static final Color COLOR_BACKGROUND_LIGHT = new Color(245, 245, 245);
    public static final Color COLOR_PANEL_BACKGROUND_LIGHT = new Color(220, 220, 220);
    public static final Color COLOR_BACKGROUND_DARK = new Color(45, 45, 45);
    public static final Color COLOR_PANEL_BACKGROUND_DARK = new Color(27, 27, 27);

    private static final Stroke STROKE_PT_CURVE_DEFAULT = new BasicStroke(5f);
    public static final Stroke STROKE_SELECTION_BOX = new BasicStroke(5f);
    public static final Stroke STROKE_CROSS_HAIR = new BasicStroke(2f);
    
    public static final double TOP_NAV_RATIO = 0.065;
    public static final double BOTTOM_NAV_RATIO = 0.1;
    public static final double PAGE_VIEW_HEIGHT_RATIO = 0.7;
    public static final double PAGE_EDIT_HEIGHT_RATIO = 0.85;
    public static final double PAGE_ASPECT_RATIO = 0.75;
    
    private static final int PAGE_CORNER_ARC = 25;
    private static final double PEN_TIP_OFFSET = 30.0;
    public static final float STROKE_WIDTH_INCREMENT = 1f;
    public static final float STROKE_MIN_WIDTH = 1f;

    // fields
    private TJ mTJ = null;
    private Color mCurColorForPtCurve = null;
    public Color getCurColorForPtCurve() {
        return this.mCurColorForPtCurve;
    }
    public void setCurColorForPtCurve(Color c) {
        this.mCurColorForPtCurve = c;
    }
    
    private Stroke mCurStrokeForPtCurve = null;
    public Stroke getCurStrokeForPtCurve() {
        return this.mCurStrokeForPtCurve;
    }
    
    // constructor
    public TJCanvas2D(TJ tj) {
        this.mTJ = tj;
        this.mCurColorForPtCurve = TJCanvas2D.COLOR_PT_CURVE_DEFAULT;
        this.mCurStrokeForPtCurve = TJCanvas2D.STROKE_PT_CURVE_DEFAULT;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        
        TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
        curScene.drawBackground(g2);
        
        g2.transform(this.mTJ.getXform().getCurrentXformFromWorldToScreen());
        
        // common world objects
        
        // current scene's world objects
        curScene.renderWorldObjects(g2);
        
        g2.transform(this.mTJ.getXform().getCurrentXformFromScreenToWorld());
        
        // common screen objects
        
        // current scene's screen obkects
        curScene.renderScreenObjects(g2);
        
    }
    
    private void drawSpread(Graphics2D g2) {
        // Draw the standard 1800x1200 spread in World Coordinates
        double width = TJJournalBookMgr.WORLD_PAGE_WIDTH;
        double height = TJJournalBookMgr.WORLD_PAGE_HEIGHT;
        
        // Left Page (0, 0)
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height,
            TJCanvas2D.PAGE_CORNER_ARC, TJCanvas2D.PAGE_CORNER_ARC));
        g2.setColor(Color.LIGHT_GRAY);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Double(0, 0, width, height,
            TJCanvas2D.PAGE_CORNER_ARC, TJCanvas2D.PAGE_CORNER_ARC));
        
        // Right Page (900, 0)
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(width, 0, width, height,
            TJCanvas2D.PAGE_CORNER_ARC, TJCanvas2D.PAGE_CORNER_ARC));
        g2.setColor(Color.LIGHT_GRAY);
        g2.draw(new RoundRectangle2D.Double(width, 0, width, height,
            TJCanvas2D.PAGE_CORNER_ARC, TJCanvas2D.PAGE_CORNER_ARC));
        
        // Divider
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(new java.awt.geom.Line2D.Double(width, 0, width, height));
    }
    
    private void drawPtCurve(
        Graphics2D g2, TJPtCurve ptCurve, Color c, Stroke s) {
        // draw a single saved point curve
        Path2D.Double path = new Path2D.Double();
        ArrayList<Point2D.Double> pts = ptCurve.getPts();
        if(pts.size() < 2) {
            return;
        }
        
        Point2D.Double pt0 = pts.get(0);
        path.moveTo(pt0.x, pt0.y);
        for (int i = 1; i < pts.size(); i++) {
            Point2D.Double pt = pts.get(i);
            path.lineTo(pt.x, pt.y);
        }
        
        g2.setColor(c);
        g2.setStroke(s);
        g2.draw(path);
    }

    public void drawPtCurves(Graphics2D g2, ArrayList<TJPtCurve> ptCurves) {
        // draw all saved point curves
        for (TJPtCurve ptCurve : ptCurves) {
            this.drawPtCurve(g2, ptCurve, ptCurve.getColor(), 
                ptCurve.getStroke());
        }
    }
    
    public void drawSelectedPtCurves(Graphics2D g2,
        ArrayList<TJPtCurve> selectedPtCurves) {
        // draw the selected point curves
        for (TJPtCurve selectedPtCurve : selectedPtCurves) {
            this.drawPtCurve(g2, selectedPtCurve,
                TJCanvas2D.COLOR_SELECTED_PT_CURVE,
                selectedPtCurve.getStroke());
        }
    }
    
    public void drawCurPtCurve(Graphics2D g2) {
        // draw current point curve
        TJPtCurve ptCurve = this.mTJ.getPtCurveMgr().getCurPtCurve();
        if (ptCurve != null) {
            this.drawPtCurve(g2, ptCurve, ptCurve.getColor(),
                ptCurve.getStroke());
        }
    }
    
    public void drawImages(Graphics2D g2, ArrayList<TJImage> images) {
        for (TJImage img : images) {
            img.draw(g2);
        }
    }
    
    public void drawPenTip(Graphics2D g2) {
        // display the current pen tip color and size
        BasicStroke bs = (BasicStroke) this.mCurStrokeForPtCurve;
        Point2D.Double worldPt0 = new Point2D.Double(0.0, 0.0);
        Point2D.Double worldPt1 = new Point2D.Double(bs.getLineWidth(), 0.0);
        Point screenPt0 = this.mTJ.getXform().calcPtFromWorldToScreen(worldPt0);
        Point screenPt1 = this.mTJ.getXform().calcPtFromWorldToScreen(worldPt1);
        double d = screenPt0.distance(screenPt1);
        double r = d / 2.0;

        Point2D.Double ctr = new Point2D.Double(
            this.getWidth() - TJCanvas2D.PEN_TIP_OFFSET,
            TJCanvas2D.PEN_TIP_OFFSET);
        Ellipse2D.Double e = new Ellipse2D.Double(ctr.x - r, ctr.y - r, d, d);
        g2.setColor(this.mCurColorForPtCurve);
        g2.fill(e);

        Stroke originalStroke = g2.getStroke();
        Color originalColor = g2.getColor();

        g2.setStroke(new BasicStroke(1.0f));
        g2.setColor(Color.LIGHT_GRAY);

        g2.draw(e);

        g2.setStroke(originalStroke);
        g2.setColor(originalColor);
     }
    
    public void increaseStrokeWidthForCurPtCurve(float f) {
        // set the stroke width higher
        BasicStroke bs = (BasicStroke) this.mCurStrokeForPtCurve;
        float w = bs.getLineWidth();
        w += f;
        
        if (w < TJCanvas2D.STROKE_MIN_WIDTH) {
            w = TJCanvas2D.STROKE_MIN_WIDTH;
        }
        
        this.mCurStrokeForPtCurve = new BasicStroke(w, bs.getEndCap(),
            bs.getLineJoin());
    }
}
