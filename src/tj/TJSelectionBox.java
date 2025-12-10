package tj;

import java.awt.Point;
import java.awt.Rectangle;

public class TJSelectionBox extends Rectangle {
    // fields
    private Point mAnchorPt = null;
    
    // constructor
    public TJSelectionBox(Point pt) {
        super(pt);
        this.mAnchorPt = pt;
    }
    
    // update with a new point
    public void update(Point pt) {
        this.setRect(this.mAnchorPt.x, this.mAnchorPt.y, 0, 0);
        this.add(pt);
    }
}