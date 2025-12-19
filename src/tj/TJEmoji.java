package tj;

import java.io.Serializable;
import java.util.ArrayList;

public class TJEmoji implements Serializable {
    // unique ID for serialization compatibility
    private static final long serialVersionUID = 1L;
    
    private ArrayList<TJPtCurve> mPtCurves;
    public ArrayList<TJPtCurve> getPtCurves() {
        return this.mPtCurves;
    }
    
    private ArrayList<TJPtCurve> mSelectedPtCurves;
    public ArrayList<TJPtCurve> getSelectedPtCurves() {
        return this.mSelectedPtCurves;
    }
    
    public TJEmoji() {
        this.mPtCurves = new ArrayList<>();
        this.mSelectedPtCurves = new ArrayList<>();
    }
    
    public boolean isContentEmpty() {
        return this.mPtCurves.isEmpty();
    }
    
    public void clearContent() {
        this.mPtCurves.clear();
        this.mSelectedPtCurves.clear();
    }
    
    public void addCurve(TJPtCurve curve) {
        this.mPtCurves.add(curve);
    }
}