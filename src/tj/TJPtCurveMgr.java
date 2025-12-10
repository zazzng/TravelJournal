package tj;
import java.awt.Color;
import java.util.ArrayList;

public class TJPtCurveMgr {
    private Color mDefaultColor = Color.BLACK;
    public void setDefaultColor(Color c) {
        this.mDefaultColor = c;
    }

    public Color getDefaultColor() {
        return this.mDefaultColor;
    }
    
    private TJPtCurve mCurPtCurve = null;
    public TJPtCurve getCurPtCurve() {
        return this.mCurPtCurve;
    }
    public void setCurPtCurve(TJPtCurve ptCurve) {
        this.mCurPtCurve = ptCurve;
    }
    
    private ArrayList<TJPtCurve> mPtCurves = null;
    public ArrayList<TJPtCurve> getPtCurves() {
        return this.mPtCurves;
    }

    private ArrayList<TJPtCurve> mSelectedPtCurves = null;
    public ArrayList<TJPtCurve> getSelectedPtCurves() {
        return this.mSelectedPtCurves;
    }
    
    // constructor
    public TJPtCurveMgr() {
        this.mPtCurves = new ArrayList<>();
        this.mSelectedPtCurves = new ArrayList<>();
    }
}