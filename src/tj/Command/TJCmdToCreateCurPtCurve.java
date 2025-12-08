package tj.Command;

import tj.TJ;
import tj.TJPtCurve;
import x.XApp;
import x.XLoggableCmd;
import java.awt.Point;
import java.awt.geom.Point2D;

public class TJCmdToCreateCurPtCurve extends XLoggableCmd{
    private Point mScreenPt = null;
    private Point2D.Double mWorldPt = null;
    
    // private constructor
    private TJCmdToCreateCurPtCurve (XApp app, Point pt) {
        super(app);
        this.mScreenPt = pt;
    } 
    
    public static boolean execute (XApp app, Point pt) {
        TJCmdToCreateCurPtCurve cmd = new TJCmdToCreateCurPtCurve(app, pt);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ) this.mApp;
        this.mWorldPt = tj.getXform().calcPtFromScreenToWorld(this.mScreenPt);
        TJPtCurve ptCurve = new TJPtCurve(this.mWorldPt, 
            tj.getCanvas2D().getCurColorForPtCurve(),
            tj.getCanvas2D().getCurStrokeForPtCurve()
        );
        tj.getPtCurveMgr().setCurPtCurve(ptCurve);
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mScreenPt).append("\t");
        sb.append(this.mWorldPt);
        return sb.toString();
    }
}
