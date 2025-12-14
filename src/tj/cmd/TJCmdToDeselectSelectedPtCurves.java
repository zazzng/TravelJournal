package tj.cmd;

import java.util.ArrayList;
import tj.TJ;
import tj.TJPage;
import tj.TJPtCurve;
import tj.scenario.TJDefaultScenario;
import tj.scenario.TJDrawScenario;
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
        
        // Get selected curves from PAGES, not from manager
        TJPage[] curPage = tj.getPageMgr().getCurPage();
        if (curPage != null) {
            this.mSelectedPtCurves = new ArrayList<>(curPage[0].getSelectedPtCurves());
            this.mSelectedPtCurves.addAll(curPage[1].getSelectedPtCurves());
        } else {
            this.mSelectedPtCurves = new ArrayList<>();
        }
        
        this.mNumOfDeselectedPtCurves = this.mSelectedPtCurves.size();
        
        // Clear selected curves from pages
        curPage[0].getSelectedPtCurves().clear();
        curPage[1].getSelectedPtCurves().clear();
        
        // Clear from manager
        tj.getPtCurveMgr().getSelectedPtCurves().clear();
        
        XCmdToChangeScene.execute(tj,
            TJDrawScenario.DrawReadyScene.getSingleton(), null);
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