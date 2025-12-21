package tj;

import java.io.Serializable;
import java.util.ArrayList;

public class TJEmojiPage implements Serializable {
    // unique ID for serialization compatibility
    private static final long serialVersionUID = 1L;
    
    // constants
    public static final TJEmojiPage BLANK_HIDDEN_PAGE = new TJEmojiPage(false);
    
    // fields: what goes on the emoji circle page
    private ArrayList<TJPtCurve> mPtCurves;
    public ArrayList<TJPtCurve> getPtCurves() {
        return this.mPtCurves;
    }
    
    private ArrayList<TJPtCurve> mSelectedPtCurves;
    public ArrayList<TJPtCurve> getSelectedPtCurves() {
        return this.mSelectedPtCurves;
    }
    
    private ArrayList<TJImage> mImages;
    public ArrayList<TJImage> getImages() {
        return this.mImages;
    }
    
    private ArrayList<TJImage> mDecorations;
    public ArrayList<TJImage> getDecorations() {
        return this.mDecorations;
    }

    // Selected decorations (for box selection in emoji select scenario)
    private ArrayList<TJImage> mSelectedDecorations;
    public ArrayList<TJImage> getSelectedDecorations() {
        return this.mSelectedDecorations;
    }
    
    private boolean mIsEditable = true;
    
    // private constructor
    private TJEmojiPage(boolean isEditable) {
        this.mPtCurves = new ArrayList<>();
        this.mSelectedPtCurves = new ArrayList<>();
        this.mDecorations = new ArrayList<>();
        this.mSelectedDecorations = new ArrayList<>();
        this.mIsEditable = isEditable;
    }
    
    // public constructor to create editable emoji pages
    public TJEmojiPage() {
        this(true);
    }
    
    public boolean isBlankHiddenPage() {
        return this == BLANK_HIDDEN_PAGE;
    }
    
    public boolean isContentEmpty() {
        // TODO: update this as the functionality increase
        if (!this.mPtCurves.isEmpty()) return false;
        if (!this.mImages.isEmpty()) return false;
        if (!this.mDecorations.isEmpty()) return false;
        return true;
    }

    public void addPtCurve(TJPtCurve curve) {
        if (curve == null) return;
        this.mPtCurves.add(curve);
    }
    
    public void addDecoration(TJImage decoration) {
        if (decoration == null) return;
        this.mDecorations.add(decoration);
        System.out.println("[DEBUG-DECORATION-ADD] Added decoration! Total: " + this.mDecorations.size() + " at position (" + decoration.getX() + ", " + decoration.getY() + ")");
    }
    
    public void removeDecoration(TJImage decoration) {
        this.mDecorations.remove(decoration);
    }
}
