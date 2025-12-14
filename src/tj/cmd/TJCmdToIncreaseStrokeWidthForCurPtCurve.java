package tj.cmd;

import java.awt.BasicStroke;
import tj.TJ;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToIncreaseStrokeWidthForCurPtCurve extends XLoggableCmd{
    // fields
    private float mWBefore = Float.NaN;
    private float mWDelta = Float.NaN;
    private float mWAfter = Float.NaN;
    
    // private constructor
    private TJCmdToIncreaseStrokeWidthForCurPtCurve (XApp app, float dw) {
        super(app);
        this.mWDelta = dw;
    } 
    
    public static boolean execute (XApp app, float dw) {
        TJCmdToIncreaseStrokeWidthForCurPtCurve cmd = 
                new TJCmdToIncreaseStrokeWidthForCurPtCurve(app, dw);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        BasicStroke bs =
            (BasicStroke)tj.getCanvas2D().getCurStrokeForPtCurve();
        this.mWBefore= bs.getLineWidth();
        tj.getCanvas2D().increaseStrokeWidthForCurPtCurve(this.mWDelta);
        bs = (BasicStroke)tj.getCanvas2D().getCurStrokeForPtCurve();
        this.mWAfter = bs.getLineWidth();
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mWBefore).append("\t");
        sb.append(this.mWDelta).append("\t");
        sb.append(this.mWAfter);
        return sb.toString();
    }
    
}
