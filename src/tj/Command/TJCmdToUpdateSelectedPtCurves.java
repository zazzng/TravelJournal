package tj.Command;

import tj.TJ;
import tj.scenario.TJSelectScenario;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToUpdateSelectedPtCurves extends XLoggableCmd {
    // fields
    private int mNumOfSelectedPtCurves = Integer.MIN_VALUE;
    
    // private constructor 
    private TJCmdToUpdateSelectedPtCurves(XApp app) {
        super(app);
    }
    
    // TJCmdToUpdateSelectedPtCurves.execute(app);
    public static boolean execute(XApp app) {
        TJCmdToUpdateSelectedPtCurves cmd  = 
            new TJCmdToUpdateSelectedPtCurves(app);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
//        tj.updateSelectedPtCurves();
        TJSelectScenario.getSingle().updateSelectedPtCurves();
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