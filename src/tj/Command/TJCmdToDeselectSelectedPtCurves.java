package tj.Command;

import java.util.ArrayList;
import tj.TJ;
import tj.TJPtCurve;
import tj.scenario.TJDefaultScenario;
import x.XApp;
import x.XCmdToChangeScene;
import x.XLoggableCmd;

public class TJCmdToDeselectSelectedPtCurves extends XLoggableCmd {
    // fields
    private ArrayList<TJPtCurve> mUnselectedPtCurves = null;
    private ArrayList<TJPtCurve> mSelectedPtCurves = null;
    private int mNumOfDeselectedPtCurves = Integer.MIN_VALUE;
    
    // private constructor 
    private TJCmdToDeselectSelectedPtCurves(XApp app) {
        super(app);
    }
    
    // TJCmdToDeselectSelectedPtCurves.execute(app);
    public static boolean execute(XApp app) {
        TJCmdToDeselectSelectedPtCurves cmd = 
            new TJCmdToDeselectSelectedPtCurves(app);
        return cmd.execute();
    }
    
    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        this.mUnselectedPtCurves = tj.getPtCurveMgr().getPtCurves();
        this.mSelectedPtCurves = tj.getPtCurveMgr().getSelectedPtCurves();
        this.mNumOfDeselectedPtCurves = this.mSelectedPtCurves.size();
        
        this.mUnselectedPtCurves.addAll(this.mSelectedPtCurves);
        this.mSelectedPtCurves.clear();
        
        XCmdToChangeScene.execute(tj,
            TJDefaultScenario.ReadyScene.getSingleton(), null);
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mUnselectedPtCurves).append("\t");
        sb.append(this.mSelectedPtCurves).append("\t");
        sb.append(this.mNumOfDeselectedPtCurves);
        return sb.toString();
    }    
}