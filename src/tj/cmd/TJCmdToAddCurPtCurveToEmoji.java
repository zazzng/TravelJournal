package tj.cmd;

import tj.TJ;
import tj.TJEmoji; // Import the new TJEmoji class
import tj.TJPtCurve;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToAddCurPtCurveToEmoji extends XLoggableCmd {
    // fields
    private TJPtCurve mCurPtCurve = null;
    private TJEmoji mEmoji = null;
    
    // private constructor
    private TJCmdToAddCurPtCurveToEmoji(XApp app, TJEmoji emoji) {
        super(app);
        this.mEmoji = emoji;
    }

    public static boolean execute(XApp app, TJEmoji emoji) {
        TJCmdToAddCurPtCurveToEmoji cmd =
            new TJCmdToAddCurPtCurveToEmoji(app, emoji);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        this.mCurPtCurve = tj.getPtCurveMgr().getCurPtCurve();
        
        if (this.mCurPtCurve.getPts().size() >= 2) {
            this.mEmoji.getPtCurves().add(this.mCurPtCurve);
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