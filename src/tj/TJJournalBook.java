package tj;

import java.io.Serializable;
import java.util.ArrayList;
import java.awt.geom.Point2D; // Import for coordinate (or use a custom MapPoint class)

public class TJJournalBook implements Serializable {
    private static final long serialVersionUID = 1L;

    private String mTitle;
    public String getTitle() {
        return this.mTitle;
    }
    public void setTitle(String title) {
        this.mTitle = title;
    }
    
    private Point2D.Double mPinPoint;
    public Point2D.Double getPinPoint() {
        return this.mPinPoint;
    }
    public void setPinPoint(Point2D.Double point) {
        this.mPinPoint = point;
    }
    
    private ArrayList<TJPage[]> mPages;
    public ArrayList<TJPage[]> getPages() {
        return this.mPages;
    }

    public TJJournalBook(String title, Point2D.Double pinPoint) {
        this.mTitle = title;
        this.mPinPoint = pinPoint;
        this.mPages = new ArrayList<>();
        
        TJPage leftPage = new TJPage();
        TJPage rightPage = new TJPage();
        this.mPages.add(new TJPage[] {leftPage, rightPage});
    }
    
    public String getFileName() {
        String sanitizedTitle = this.mTitle.replaceAll("[^a-zA-Z0-9.-]", "_");
        return "journal/" + sanitizedTitle + ".dat";
    }
}