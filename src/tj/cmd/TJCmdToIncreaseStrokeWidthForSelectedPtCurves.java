package tj.cmd;

import java.util.ArrayList;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJPtCurve;
import x.XApp;
import x.XLoggableCmd;


public class TJCmdToIncreaseStrokeWidthForSelectedPtCurves extends 
    XLoggableCmd {
    // field 
    private float mWDelta = Float.NaN;
    private ArrayList<TJPtCurve> mSelectedPtCurves = null;
    
    // private constructor 
    private TJCmdToIncreaseStrokeWidthForSelectedPtCurves(XApp app, 
        float dw) {
        super(app);
        this.mWDelta = dw;
    }
    
    // TJCmdToIncreaseStrokeWidthForSelectedPtCurves.execute(app,
    //     TJCanvas2D.STROKE_WIDTH_INCREMENT);
    public static boolean execute(XApp app, float dw) {
        TJCmdToIncreaseStrokeWidthForSelectedPtCurves cmd = 
            new TJCmdToIncreaseStrokeWidthForSelectedPtCurves(app, dw);
        return cmd.execute();
    }
    
    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        this.mSelectedPtCurves = tj.getPtCurveMgr().getSelectedPtCurves();
        
        if (tj.getPtCurveMgr().getSelectedPtCurves().isEmpty()) {
            TJCmdToIncreaseStrokeWidthForCurPtCurve.execute(tj, this.mWDelta);
        } else {
            for (TJPtCurve ptCurve : this.mSelectedPtCurves) {
                ptCurve.increaseStrokeWidth(this.mWDelta);
            }
        }
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        // implement here
        return sb.toString();
    }
}