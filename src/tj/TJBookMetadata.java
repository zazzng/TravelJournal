package tj;

import java.awt.geom.Point2D;
import java.io.Serializable;

public class TJBookMetadata implements Serializable {
    private static final long serialVersionUID = 1L;
    public String title;
    public Point2D.Double pinPoint;
    public TJBookMetadata(String title, Point2D.Double pinPoint) {
        this.title = title;
        this.pinPoint = pinPoint;
    }
}