package tj;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class TJPenMarkMgr {
    // constants
    private static final int MAX_NUM_PEN_MARKS = 10;
    
    // fields
    private ArrayList<TJPenMark> mPenMarks = null;
    public ArrayList<TJPenMark> getPenMarks() {
        return this.mPenMarks;
    }
    
    // constructor
    public TJPenMarkMgr() {
        this.mPenMarks = new ArrayList<>();
    }
    
    public void addPenMark(TJPenMark penMark) {
        this.mPenMarks.add(penMark);
        if (this.mPenMarks.size() > TJPenMarkMgr.MAX_NUM_PEN_MARKS) {
            this.mPenMarks.remove(0);
            assert(this.mPenMarks.size() <= TJPenMarkMgr.MAX_NUM_PEN_MARKS);
        }
    }
    
    public TJPenMark getLastPenMark() {
        int size = this.mPenMarks.size();
        if (size == 0) {
            return null;
        } else {
            return this.mPenMarks.get(size - 1);
        }
    }
    
    public TJPenMark getRecentPenMark(int i) {
        int size = this.mPenMarks.size();
        int index = size - 1 - i;
        if (index < 0 || index >= size) {
            return null;
        } else {
            return this.mPenMarks.get(index);
        }
    }
    
    public boolean handleMousePress(MouseEvent e) {
        Point pt = e.getPoint();
        TJPenMark penMark = new TJPenMark(pt);
        this.addPenMark(penMark);
        return true;
    }
    
    public boolean handleMouseDrag(MouseEvent e) {
        Point pt = e.getPoint();
        TJPenMark penMark = this.getLastPenMark();
        if (penMark != null) {
            penMark.addPt(pt);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean handleMouseRelease(MouseEvent e) {
        return true;
    }
}
