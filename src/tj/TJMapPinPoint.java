package tj;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.Serializable;

public class TJMapPinPoint implements Serializable {
    private static final long serialVersionUID = 1L;
    private Point2D.Double mMapPoint; 
    private String mBookTitle; 
    
    public static final String PIN_SYMBOL = "📍";
    private static final int PIN_SIZE = 28;

    public TJMapPinPoint(Point2D.Double mapPoint, String bookTitle) {
        this.mMapPoint = mapPoint;
        this.mBookTitle = bookTitle;
    }

    public Point2D.Double getMapPoint() {
        return this.mMapPoint;
    }

    public String getBookTitle() {
        return this.mBookTitle;
    }
    
    public boolean contains(Point screenPoint, Rectangle mapBound) {
        int x = (int)(mapBound.x + mMapPoint.x * mapBound.width);
        int y = (int)(mapBound.y + mMapPoint.y * mapBound.height);
        
        Rectangle bounds = new Rectangle(
            x - PIN_SIZE / 2, 
            y - PIN_SIZE / 2, 
            PIN_SIZE, 
            PIN_SIZE
        );
        return bounds.contains(screenPoint);
    }

    public void draw(Graphics2D g2, Rectangle mapBound) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, PIN_SIZE));

        int x = (int)(mapBound.x + mMapPoint.x * mapBound.width);
        int y = (int)(mapBound.y + mMapPoint.y * mapBound.height);

        g2.drawString(PIN_SYMBOL, x - PIN_SIZE / 2, y + PIN_SIZE / 4);
    }
}