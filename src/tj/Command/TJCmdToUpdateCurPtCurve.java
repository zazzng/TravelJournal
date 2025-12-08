package tj.Command;

import java.awt.Point;
import java.awt.geom.Point2D;
import tj.TJ;
import tj.TJPtCurve;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToUpdateCurPtCurve extends XLoggableCmd {
    // fields
    private Point mScreenPt = null;
    private Point2D.Double mWorldPt = null;
    private TJPtCurve mCurPtCurve = null;
    
    // private constructor
    private TJCmdToUpdateCurPtCurve(XApp app, Point pt) {
        super(app);
        this.mScreenPt = pt;
    }

    // TJCmdToDeleteSelectedPtCurves.execute(app);
    public static boolean execute(XApp app, Point pt) {
        TJCmdToUpdateCurPtCurve cmd = new TJCmdToUpdateCurPtCurve(app, pt);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        this.mCurPtCurve = tj.getPtCurveMgr().getCurPtCurve();
        
        int size = this.mCurPtCurve.getPts().size();
        Point2D.Double lastWorldPt = this.mCurPtCurve.getPts().get(size - 1);
        Point lastScreenPt = tj.getXform().calcPtFromWorldToScreen(lastWorldPt);

        if (this.mScreenPt.distance(lastScreenPt) < 
            TJPtCurve.MIN_DIST_BTWN_PTS) {
            return true;
        }

        this.mWorldPt = tj.getXform().calcPtFromScreenToWorld(
            this.mScreenPt);
        this.mCurPtCurve.addPt(this.mWorldPt);
        
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mScreenPt).append("\t");
        sb.append(this.mWorldPt).append("\t");
        sb.append(this.mCurPtCurve);
        return sb.toString();
    }
}
