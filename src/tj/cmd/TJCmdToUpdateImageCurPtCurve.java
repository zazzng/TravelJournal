package tj.cmd;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import tj.TJ;
import tj.TJPtCurve;
import tj.scenario.TJDrawScenario;
import tj.scenario.TJImageScenario;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToUpdateImageCurPtCurve extends XLoggableCmd {
    // fields
    private Point mScreenPt = null;
    private Point2D.Double mWorldPt = null;
    private TJPtCurve mCurPtCurve = null;
    private String mPrevScenario = "";
    
    // private constructor
    private TJCmdToUpdateImageCurPtCurve(XApp app, Point pt, String scenario) {
        super(app);
        this.mScreenPt = pt;
        this.mPrevScenario = scenario;
    }

    // TJCmdToDeleteSelectedPtCurves.execute(app);
    public static boolean execute(XApp app, Point pt, String scenario) {
        TJCmdToUpdateImageCurPtCurve cmd = new TJCmdToUpdateImageCurPtCurve(app, pt,
            scenario);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        
        this.mCurPtCurve = tj.getPtCurveMgr().getCurPtCurve();
        
        if (this.mCurPtCurve == null) {
            return false;
        }
        
        Rectangle targetBounds = null;
        if (this.mPrevScenario.equals("Draw")) {
            targetBounds = TJDrawScenario.getSingle().getTargetBounds();
        } else if (this.mPrevScenario.equals("Image")) {
            targetBounds = TJImageScenario.getSingle().getTargetBounds();
        }
        
        if (!targetBounds.contains(this.mScreenPt)) {
            // clip the screen point to the boundary of the target page
            this.mScreenPt.x = Math.max(targetBounds.x, Math.min(
                this.mScreenPt.x, targetBounds.x + targetBounds.width));
            this.mScreenPt.y = Math.max(targetBounds.y, Math.min(
                this.mScreenPt.y, targetBounds.y + targetBounds.height));
        }
        
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
