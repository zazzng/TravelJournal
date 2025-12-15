package tj.cmd;

import tj.TJ;
import tj.scenario.TJEmojiSelectScenario;
import tj.scenario.TJSelectScenario;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToUpdateEmojiSelectedPtCurves extends XLoggableCmd{
    private int mNumOfSelectedPtCurves = Integer.MIN_VALUE;

    private TJCmdToUpdateEmojiSelectedPtCurves(XApp app) {
        super(app);
    }

    public static boolean execute(XApp app) {
        TJCmdToUpdateEmojiSelectedPtCurves cmd  = 
            new TJCmdToUpdateEmojiSelectedPtCurves(app);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        TJEmojiSelectScenario.getSingle().updateSelectedPtCurves();
        this.mNumOfSelectedPtCurves = tj.getPtCurveMgr().
            getSelectedPtCurves().size();
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mNumOfSelectedPtCurves);
        return sb.toString();
    }
    
}
