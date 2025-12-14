package tj;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

public class TJPtCurve implements Serializable {
    // constants
    private static final long serialVersionUID = 1L;
    public static final double MIN_DIST_BTWN_PTS = 5.0;
    
    // fields
    // m = members
    private ArrayList<Point2D.Double> mPts = null;
    // getter
    public ArrayList<Point2D.Double> getPts() {
        return this.mPts;
    }
    
    private Rectangle2D.Double mBoundingBox = null;
    public Rectangle2D.Double getBoundingBox() {
        return this.mBoundingBox;
    }
    
    private Color mColor = null;
    public Color getColor() {
        return this.mColor;
    }
    public void setColor(Color c) {
        this.mColor = c;
    }
    
    // Stroke is not serializable -> store its properties instead
    private float strokeWidth;
    private int strokeEndCap;
    private int strokeLineJoin;
    
    private transient Stroke mStroke = null;
    public Stroke getStroke() {
        if (mStroke == null) {
            mStroke = new BasicStroke(strokeWidth, strokeEndCap, strokeLineJoin);
        }
        return this.mStroke;
    }
    
    // constructor
    public TJPtCurve(Point2D.Double pt, Color c, Stroke s) {
        this.mPts = new ArrayList<Point2D.Double>();
        this.mPts.add(pt);
        this.mBoundingBox = new Rectangle2D.Double(pt.x, pt.y, 0.0, 0.0);
        
        this.mColor = new Color(
            c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        
        BasicStroke bs = (BasicStroke) s;
        this.strokeWidth = bs.getLineWidth();
        this.strokeEndCap = bs.getEndCap();
        this.strokeLineJoin = bs.getLineJoin();
        this.mStroke = new BasicStroke(
            bs.getLineWidth(), bs.getEndCap(), bs.getLineJoin());
    }
    
    // methods
    public void addPt(Point2D.Double pt) {
        this.mPts.add(pt);
        this.mBoundingBox.add(pt);
    }
    
    public void increaseStrokeWidth(float f) {
        // update the stroke width based on user's input
        BasicStroke bs = (BasicStroke) this.mStroke;
        float w = bs.getLineWidth();
        w += f;
        
        if (w < TJCanvas2D.STROKE_MIN_WIDTH) {
            w = TJCanvas2D.STROKE_MIN_WIDTH;
        }
        
        BasicStroke newStroke = new BasicStroke(
            w, bs.getEndCap(), bs.getLineJoin());
        this.mStroke = newStroke;
    }
}