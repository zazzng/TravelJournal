package tj;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class TJXform {
    // constants
    public static final Point PIVOT_PT = new Point(100,100);
    public static final double MIN_START_ARM_LENGTH_FOR_SCALING = 100.0;
    
    // fields
    private AffineTransform mCurXformFromWorldToScreen = null;
    public AffineTransform getCurrentXformFromWorldToScreen() {
        return this.mCurXformFromWorldToScreen;
    }
    
    private AffineTransform mCurXformFromScreenToWorld = null;
    public AffineTransform getCurrentXformFromScreenToWorld() {
        // inverse of mCurXformFromWorldToScreen
        return this.mCurXformFromScreenToWorld;
    }
    
    private AffineTransform mStartXformFromWorldToScreen = null;
    private Point mStartScreenPt = null;
    public void setStartScreenPt(Point pt) {
        this.mStartScreenPt = pt;
        this.mStartXformFromWorldToScreen.setTransform(
            this.mCurXformFromWorldToScreen);
    }
    
    // constructor
    public TJXform() {
        // 3x3 identity matrix
        this.mCurXformFromWorldToScreen = new AffineTransform();
        this.mCurXformFromScreenToWorld = new AffineTransform();
        this.mStartXformFromWorldToScreen = new AffineTransform();
    }
    
    // call whenever mCurXformFromWorldToScreen changes to have the
    // corresponding mCurXformFromScreenToWorld
    public void updateCurXformFromScreenToWorld() {
        try {
            this.mCurXformFromScreenToWorld =
                this.mCurXformFromWorldToScreen.createInverse();
        } catch (NoninvertibleTransformException ex) {
            System.out.println("NoninvertableTransformException");
        }
    }
    
    public void setFitToTransform(double scale, double transX, double transY) {
        this.mCurXformFromWorldToScreen.setToIdentity();
        this.mCurXformFromWorldToScreen.scale(scale, scale);
        this.mCurXformFromWorldToScreen.translate(transX / scale,
            transY / scale);
        
        this.updateCurXformFromScreenToWorld();
    }
    
    public Point calcPtFromWorldToScreen(Point2D.Double worldPt) {
        // transform a point from World coordinate to Screen coordinate
        Point screenPt = new Point();
        this.mCurXformFromWorldToScreen.transform(worldPt, screenPt);
        return screenPt;
    }
    
    public Point2D.Double calcPtFromScreenToWorld(Point screenPt) {
        // transform a point from Screen coordinate to World coordinate
        Point2D.Double worldPt = new Point2D.Double();
        this.mCurXformFromScreenToWorld.transform(screenPt, worldPt);
        return worldPt;
    }

    public boolean translateTo(Point pt) {
        // update the position of points in screen coordinate
        if (this.mStartScreenPt == null) {
            return false;
        }
        
        this.mCurXformFromWorldToScreen.setTransform(
            this.mStartXformFromWorldToScreen);
        
        Point2D.Double worldPt0 =
            this.calcPtFromScreenToWorld(this.mStartScreenPt);
        Point2D.Double worldPt1 = this.calcPtFromScreenToWorld(pt);
        double dx = worldPt1.x - worldPt0.x;
        double dy = worldPt1.y - worldPt0.y;
        
        this.mCurXformFromWorldToScreen.translate(dx, dy);
        
        // call whenever mCurTransformFromWorldToScreen changes
        this.updateCurXformFromScreenToWorld();
        
        return true;
    }

    public boolean rotateTo(Point pt) {
        // update the position of points based on rotation
        if (this.mStartScreenPt == null) {
            return false;
        }
        
        this.mCurXformFromWorldToScreen.setTransform(
            this.mStartXformFromWorldToScreen);
        
        double ang0 = StrictMath.atan2(
            this.mStartScreenPt.y - TJXform.PIVOT_PT.y,
            this.mStartScreenPt.x - TJXform.PIVOT_PT.x);
        double ang1 = StrictMath.atan2(
            pt.y - TJXform.PIVOT_PT.y, pt.x - TJXform.PIVOT_PT.x);
        double ang = ang1 - ang0;

        Point2D.Double worldPivotPt = this.calcPtFromScreenToWorld(
            TJXform.PIVOT_PT);
        
        this.mCurXformFromWorldToScreen.translate(
            worldPivotPt.x, worldPivotPt.y);
        this.mCurXformFromWorldToScreen.rotate(ang);
        this.mCurXformFromWorldToScreen.translate(
            -worldPivotPt.x, -worldPivotPt.y);
       
        // call whenever mCurTransfirmFromWorldToScreen changes
        this.updateCurXformFromScreenToWorld();
        
        return true;
    }
    
    public boolean zoomTo(Point pt) {
        if (this.mStartScreenPt == null) {
            return false;
        }
        
        this.mCurXformFromWorldToScreen.setTransform(
            this.mStartXformFromWorldToScreen);
        
        double d0 = TJXform.PIVOT_PT.distance(this.mStartScreenPt);
        if (d0 < TJXform.MIN_START_ARM_LENGTH_FOR_SCALING) {
            return false;
        }
        double d1 = TJXform.PIVOT_PT.distance(pt);
        double s = d1 / d0;

        Point2D.Double worldPivotPt = this.calcPtFromScreenToWorld(
            TJXform.PIVOT_PT);
        
        this.mCurXformFromWorldToScreen.translate(
            worldPivotPt.x, worldPivotPt.y);
            
        // scale step (ZOOM)
        this.mCurXformFromWorldToScreen.scale(s, s);
        
        // translate back from pivot in world space
        this.mCurXformFromWorldToScreen.translate(
            -worldPivotPt.x, -worldPivotPt.y);
        
        this.updateCurXformFromScreenToWorld();
        
        return true;
    }

    public boolean zoomRotateTo(Point pt) {
        // update the position of points based on rotation on zooming
        if (this.mStartScreenPt == null) {
            return false;
        }
        
        this.mCurXformFromWorldToScreen.setTransform(
            this.mStartXformFromWorldToScreen);
        
        // call whenever mCurTransformFromWorldToScreen changes
        double d0 = TJXform.PIVOT_PT.distance(this.mStartScreenPt);
        if (d0 < TJXform.MIN_START_ARM_LENGTH_FOR_SCALING) {
            return false;
        }
        double d1 = TJXform.PIVOT_PT.distance(pt);
        double s = d1 / d0;
        
        double ang0 = StrictMath.atan2(
            this.mStartScreenPt.y - TJXform.PIVOT_PT.y,
            this.mStartScreenPt.x - TJXform.PIVOT_PT.x);
        double ang1 = StrictMath.atan2(
            pt.y - TJXform.PIVOT_PT.y, pt.x - TJXform.PIVOT_PT.x);
        double ang = ang1 - ang0;

        Point2D.Double worldPivotPt = this.calcPtFromScreenToWorld(
            TJXform.PIVOT_PT);
        
        this.mCurXformFromWorldToScreen.translate(
            worldPivotPt.x, worldPivotPt.y);
        this.mCurXformFromWorldToScreen.rotate(ang);
        this.mCurXformFromWorldToScreen.scale(s,s);
        this.mCurXformFromWorldToScreen.translate(
            -worldPivotPt.x, -worldPivotPt.y);
        
        // call whenever mCurTransfirmFromWorldToScreen changes
        this.updateCurXformFromScreenToWorld();
        
        return true;
    }

    public void home() {
        // return screen coordinate to world coordinate
        this.mCurXformFromWorldToScreen.setToIdentity();
        this.updateCurXformFromScreenToWorld();
    }
}