package tj;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.imageio.ImageIO;

public class TJImage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // position and transform data
    private double mX, mY;
    public double getX() {
        return this.mX;
    }
    public double getY() {
        return this.mY;
    }
    private double mScale = 1.0;
    public double getScale() {
        return this.mScale;
    }
    private double mRotation = 0.0; // in radians
    public double getRotation() {
        return this.mRotation;
    }
    
    // image data
    private transient BufferedImage mImage;
    private byte[] mImageData;
    
    public TJImage(BufferedImage img, int centerX, int centerY, double scale) {
        this.mImage = img;
        this.mX = centerX;
        this.mY = centerY;
        this.mScale = scale;
        convertImageToBytes();
    }
    
    // serialization helper: save image as bytes
    private void convertImageToBytes() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(mImage, "png", baos);
            mImageData = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // serialization helper: load image from bytes
    private void readObject(ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        in.defaultReadObject();
        if (mImageData != null) {
            this.mImage = ImageIO.read(new ByteArrayInputStream(mImageData));
        }
    }
    
    public int getWidth() { 
        return mImage != null ? mImage.getWidth() : 0; 
    }

    public int getHeight() { 
        return mImage != null ? mImage.getHeight() : 0; 
    }
    
    public Rectangle getBounds() {
        if (mImage == null) return new Rectangle();

        // Calculate scaled dimensions
        int scaledWidth = (int) (mImage.getWidth() * mScale);
        int scaledHeight = (int) (mImage.getHeight() * mScale);

        // Calculate top-left corner
        int x = (int) (mX - scaledWidth / 2);
        int y = (int) (mY - scaledHeight / 2);

        return new Rectangle(x, y, scaledWidth, scaledHeight);
    }
    
    public void draw(Graphics2D g2) {
        if (mImage == null) return;

        AffineTransform saveAT = g2.getTransform();
        
        g2.translate(mX, mY);
        g2.rotate(mRotation);
        g2.scale(mScale, mScale);
        
        int w = mImage.getWidth();
        int h = mImage.getHeight();
        g2.drawImage(mImage, -w/2, -h/2, null);
        
//        if (isSelected) {
//            g2.setColor(new Color(50, 150, 255));
//            g2.setStroke(new BasicStroke(3.0f));
//            g2.drawRect(-w/2, -h/2, w, h);
//        }

        g2.setTransform(saveAT);
    }

    public boolean contains(Point p) {
        if (mImage == null) return false;
        
        // inverse transform the point to check against the un-rotated image rect
        try {
            AffineTransform at = new AffineTransform();
            at.translate(mX, mY);
            at.rotate(mRotation);
            at.scale(mScale, mScale);
            
            Point2D inversePt = at.inverseTransform(p, null);
            
            int w = mImage.getWidth();
            int h = mImage.getHeight();
            Rectangle localRect = new Rectangle(-w/2, -h/2, w, h);
            
            return localRect.contains(inversePt);
        } catch (Exception e) {
            return false;
        }
    }
    
    // getters and setters for manipulation
    public void translate(double dx, double dy) {
        this.mX += dx; this.mY += dy;
    }
    
    public void rotate(double theta) {
        this.mRotation += theta;
    }
    
    public void scale(double factor) { 
        this.mScale *= factor;
    }
    
    public void setPosition(double x, double y) {
        this.mX = x; this.mY = y;
    }
    
}
