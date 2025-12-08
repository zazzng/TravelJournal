package tj;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.JPanel;

import tj.Button.ColorButton;
import tj.Button.EmojiButton;
import tj.scenario.TJColorScenario;

public class TJCanvas2D extends JPanel {
    // constants
    private static final Color COLOR_PT_CURVE_DEFAULT = new Color(0, 0, 0, 192);
    private static final Color COLOR_SELECTED_PT_CURVE = Color.ORANGE;
    public static final Color COLOR_SELECTION_BOX = new Color(255, 0, 0, 64);
    private static final Color COLOR_INFO = new Color(255, 0, 0, 128);
    public static final Color COLOR_CROSS_HAIR = new Color(255, 0, 0, 64);
    
    private static final Stroke STROKE_PT_CURVE_DEFAULT = new BasicStroke(5f);
    public static final Stroke STROKE_SELECTION_BOX = new BasicStroke(5f);
    public static final Stroke STROKE_CROSS_HAIR = new BasicStroke(5f);
    
    private static final Font FONT_INFO = new Font("Monospaced", Font.PLAIN, 24);
    
    private static final float INFO_TOP_ALIGNMENT_X = 20;
    private static final float INFO_TOP_ALIGNMENT_Y = 30;
    public static final double ZOOM_ROTATE_CROSS_HAIR_RADIUS = 30.0;
    private static final double PEN_TIP_OFFSET = 30.0;
    public static final float STROKE_WIDTH_INCREMENT = 1f;
    static final float STROKE_MIN_WIDTH = 1f;

    private ColorButton mColorButton;
    private EmojiButton mEmojiButton;
    
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

    public ColorButton getColorButton() {
        return this.mColorButton;
    }

    public EmojiButton getEmojiButton() {
        return this.mEmojiButton;
    }
    
    // constructor
    public TJCanvas2D(TJ tj) {
        this.mTJ = tj;
        this.mCurColorForPtCurve = TJCanvas2D.COLOR_PT_CURVE_DEFAULT;
        this.mCurStrokeForPtCurve = TJCanvas2D.STROKE_PT_CURVE_DEFAULT;

        this.mColorButton = new ColorButton(
            20,                      // marginX - distance from right
            5,                       // marginY - distance from bottom (smaller = lower)
            ColorButton.Anchor.BOTTOM_RIGHT
        );
        this.mEmojiButton = new EmojiButton (
            110,                     // marginX - distance from right (further left)
            5,                       // marginY - distance from bottom (smaller = lower)
            EmojiButton.Anchor.BOTTOM_RIGHT
        );
    }
    
    // Source > Insert Code > Override Method  
    @Override
    protected void paintComponent(Graphics g) {
        // draw curves and other canvas components on Graphics g
        // based on the last updated screen position
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // transform the coordinate system from screen to world
        // note: the transformation of a coordinate system is the reverse
        // process of the transformation of a geometric object
        g2.transform(this.mTJ.getXform().getCurrentXformFromWorldToScreen());
        
        // render common world objects
        this.drawPtCurves(g2);
        this.drawSelectedPtCurves(g2);
        this.drawCurPtCurve(g2);


        
        // render the current scene's world objects
        TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
        curScene.renderWorldObjects(g2);
        
        // transform the coordinate system from world to screen
        g2.transform(this.mTJ.getXform().getCurrentXformFromScreenToWorld());
        
        // render common screen objects
        // Set button dimensions before drawing
        this.mColorButton.setPanelDimensions(this.getWidth(), this.getHeight());
        this.mEmojiButton.setPanelDimensions(this.getWidth(), this.getHeight());
        
        this.mColorButton.draw(g2);
        this.mEmojiButton.draw(g2);
    //    this.drawSelectionBox(g2);
//        this.drawCrossHair(g2);
        this.drawColorChooser(g2);
        this.drawPenTip(g2);
        this.drawInfo(g2);
        
        // render the current scene's screen objects
        curScene.renderScreenObjects(g2);
    }
    
    private void drawPtCurve(
        Graphics2D g2, TJPtCurve ptCurve, Color c,Stroke s) {
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

    private void drawPtCurves(Graphics2D g2) {
        // draw all saved point curves
        for (TJPtCurve ptCurve : this.mTJ.getPtCurveMgr().getPtCurves()) {
            this.drawPtCurve(g2, ptCurve, ptCurve.getColor(), 
                ptCurve.getStroke());
        }
    }
    
    private void drawSelectedPtCurves(Graphics2D g2) {
        // draw the selected point curves
        for (TJPtCurve selectedPtCurve : this.mTJ.getPtCurveMgr().getSelectedPtCurves()) {
            this.drawPtCurve(g2, selectedPtCurve,
                TJCanvas2D.COLOR_SELECTED_PT_CURVE,
                selectedPtCurve.getStroke());
        }
    }
    
    private void drawCurPtCurve(Graphics2D g2) {
        // draw current point curve
        TJPtCurve ptCurve = this.mTJ.getPtCurveMgr().getCurPtCurve();
        if (ptCurve != null) {
            this.drawPtCurve(g2, ptCurve, ptCurve.getColor(),
                ptCurve.getStroke());
        }
    }

    private void drawColorChooser(Graphics2D g2) {
        TJScene curScene =
            (TJScene) this.mTJ.getScenarioMgr().getCurScene();

        // Show color chooser ONLY in ColorChangeScene
        if (curScene instanceof TJColorScenario.ColorChangeScene) {
            this.mTJ.getColorChooser().drawCells(
                g2,
                this.getWidth(),
                this.getHeight()
            );
        }
    }

    private void drawPenTip(Graphics2D g2) {
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
    }
    
    private void drawInfo(Graphics2D g2) {
        // display the current mode
//        String str = String.valueOf(this.mTJ.getMode());
        TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
        String str = curScene.getClass().getSimpleName();
        g2.setColor(TJCanvas2D.COLOR_INFO);
        g2.setFont(TJCanvas2D.FONT_INFO);
        g2.drawString(str, TJCanvas2D.INFO_TOP_ALIGNMENT_X, 
            TJCanvas2D.INFO_TOP_ALIGNMENT_Y);
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