package tj.Command;

import java.util.ArrayList;
import tj.TJ;
import tj.TJPtCurve;
import tj.scenario.TJDefaultScenario;
import x.XApp;
import x.XCmdToChangeScene;
import x.XLoggableCmd;

public class TJCmdToDeleteSelectedPtCurves extends XLoggableCmd {
    // fields
    private ArrayList<TJPtCurve> mSelectedPtCurves = null;
    private int mNumOfDeletedPtCurves = Integer.MIN_VALUE;
    
    // private constructor
    private TJCmdToDeleteSelectedPtCurves(XApp app) {
        super(app);
    }

    // TJCmdToDeleteSelectedPtCurves.execute(app);
    public static boolean execute(XApp app) {
        TJCmdToDeleteSelectedPtCurves cmd = 
            new TJCmdToDeleteSelectedPtCurves(app);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        this.mSelectedPtCurves = tj.getPtCurveMgr().getSelectedPtCurves();
        this.mNumOfDeletedPtCurves = this.mSelectedPtCurves.size();
        
        this.mSelectedPtCurves.clear();
        XCmdToChangeScene.execute(tj,
            TJDefaultScenario.ReadyScene.getSingleton(), null);
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mSelectedPtCurves).append("\t");
        sb.append(this.mNumOfDeletedPtCurves);
        return sb.toString();
    }
}