package tj;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

public class TJPenMark {
    // fields
    private ArrayList<Point> mPts = null;
    public ArrayList<Point> getPts() {
        return this.mPts;
    }
    
    private Rectangle mBoundingBox = null;
    public Rectangle getBoundingBox() {
        return this.mBoundingBox;
    }
    
    // constructor
    public TJPenMark(Point pt) {
        this.mPts = new ArrayList<Point>();
        this.mPts.add(pt);
        this.mBoundingBox = new Rectangle(pt.x, pt.y, 0, 0);
    }
    
    public void addPt(Point pt) {
        assert(this.mPts.size() > 0);
        this.mPts.add(pt);
        this.mBoundingBox.add(pt);
    }
    
    public Point getFirstPt() {
        return this.mPts.get(0);
    }
    
    public Point getLastPt() {
        int size = this.mPts.size();
        return this.mPts.get(size - 1);
    }
}
