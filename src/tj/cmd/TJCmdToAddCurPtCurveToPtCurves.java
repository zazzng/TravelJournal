package tj.cmd;

import tj.TJ;
import tj.TJPage;
import tj.TJPtCurve;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToAddCurPtCurveToPtCurves extends XLoggableCmd {
    // fields
    private TJPtCurve mCurPtCurve = null;
    private TJPage mPage = null;
    
    // private constructor
    private TJCmdToAddCurPtCurveToPtCurves(XApp app, TJPage page) {
        super(app);
        this.mPage = page;
    }

    public static boolean execute(XApp app, TJPage page) {
        TJCmdToAddCurPtCurveToPtCurves cmd =
            new TJCmdToAddCurPtCurveToPtCurves(app, page);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        this.mCurPtCurve = tj.getPtCurveMgr().getCurPtCurve();
        
        if (this.mCurPtCurve.getPts().size() >= 2) {
            this.mPage.getPtCurves().add(this.mCurPtCurve);
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
