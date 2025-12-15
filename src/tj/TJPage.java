package tj;

import java.io.Serializable;
import java.util.ArrayList;

public class TJPage implements Serializable {
    // unique ID for serialization compatibility
    private static final long serialVersionUID = 1L;
    
    // constants
    public static final TJPage BLANK_HIDDEN_PAGE = new TJPage(false);
    
    // fields: what goes on the journal page
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
    
    private boolean mIsEditable = true;
    
    // private constructor
    private TJPage(boolean isEditable) {
        this.mPtCurves = new ArrayList<>();
        this.mSelectedPtCurves = new ArrayList<>();
        this.mImages = new ArrayList<>();
        this.mIsEditable = isEditable;
    }
    // public constructor to create editable pages
    public TJPage() {
        this(true);
    }
    
    public boolean isBlankHiddenPage() {
        return this == BLANK_HIDDEN_PAGE;
    }
    
    public boolean isContentEmpty() {
        // TODO: update this as the functionality increase
        if (!this.mPtCurves.isEmpty()) return false;
        if (!this.mImages.isEmpty()) return false;
        return true;
    }
    
    public void addImage(TJImage img) {
        this.mImages.add(img);
    }

    public void addPtCurve(TJPtCurve curve) {
    if (curve == null) return;
    this.mPtCurves.add(curve);
}

}
