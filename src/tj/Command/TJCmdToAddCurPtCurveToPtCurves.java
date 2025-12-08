package tj.Command;

import tj.TJ;
import tj.TJPtCurve;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToAddCurPtCurveToPtCurves extends XLoggableCmd {
    // fields
    private TJPtCurve mCurPtCurve = null;
    
    // private constructor
    private TJCmdToAddCurPtCurveToPtCurves(XApp app) {
        super(app);
    }

    public static boolean execute(XApp app) {
        TJCmdToAddCurPtCurveToPtCurves cmd = new TJCmdToAddCurPtCurveToPtCurves(app);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        this.mCurPtCurve = tj.getPtCurveMgr().getCurPtCurve();
        
        if (this.mCurPtCurve.getPts().size() >= 2) {
            tj.getPtCurveMgr().getPtCurves().add(this.mCurPtCurve);
        }
        tj.getPtCurveMgr().setCurPtCurve(null);
        
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mCurPtCurve);
        return sb.toString();
    }
}
