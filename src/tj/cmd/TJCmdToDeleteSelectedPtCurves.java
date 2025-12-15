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
        
        // Get selected curves from PAGES, not from manager
        // because that's where they are actually stored
        TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
        if (curPage != null) {
            this.mSelectedPtCurves = new ArrayList<>(curPage[0].getSelectedPtCurves());
            this.mSelectedPtCurves.addAll(curPage[1].getSelectedPtCurves());
        } else {
            this.mSelectedPtCurves = new ArrayList<>();
        }
        
        this.mNumOfDeletedPtCurves = this.mSelectedPtCurves.size();
        
        System.out.println("\n========== DELETE SELECTED PT CURVES ==========");
        System.out.println("[DELETE CURVES] Total selected curves to delete: " + this.mNumOfDeletedPtCurves);
        System.out.println("[DELETE CURVES] Curves to delete: " + this.mSelectedPtCurves);
        System.out.println("[DELETE CURVES] Page[0] curves before: " + curPage[0].getPtCurves().size());
        System.out.println("[DELETE CURVES] Page[1] curves before: " + curPage[1].getPtCurves().size());
        System.out.println("[DELETE CURVES] Page[0] obj: " + System.identityHashCode(curPage[0]));
        System.out.println("[DELETE CURVES] Page[1] obj: " + System.identityHashCode(curPage[1]));
        System.out.println("[DELETE CURVES] Manager curves before: " + tj.getPtCurveMgr().getPtCurves().size());
        
        // Remove the selected curves from the pages
        if (curPage != null) {
            curPage[0].getPtCurves().removeAll(this.mSelectedPtCurves);
            curPage[1].getPtCurves().removeAll(this.mSelectedPtCurves);
            
            // Also clear from pages' selected lists
            curPage[0].getSelectedPtCurves().clear();
            curPage[1].getSelectedPtCurves().clear();
        }
        
        // CRITICAL: Also remove from manager's unselected list!
        tj.getPtCurveMgr().getPtCurves().removeAll(this.mSelectedPtCurves);
        tj.getPtCurveMgr().getSelectedPtCurves().clear();
        
        System.out.println("[DELETE CURVES] Page[0] curves after: " + curPage[0].getPtCurves().size());
        System.out.println("[DELETE CURVES] Page[1] curves after: " + curPage[1].getPtCurves().size());
        System.out.println("[DELETE CURVES] Manager curves after: " + tj.getPtCurveMgr().getPtCurves().size());
        System.out.println("==============================================\n");
        
        XCmdToChangeScene.execute(tj,
            TJDrawScenario.DrawReadyScene.getSingleton(), null);
        
        // Check state AFTER scene change
        TJPage[] curPageAfter = tj.getJournalBookMgr().getCurPage();
        System.out.println("[DELETE CURVES AFTER SCENE CHANGE] Page[0] curves: " + curPageAfter[0].getPtCurves().size());
        System.out.println("[DELETE CURVES AFTER SCENE CHANGE] Page[1] curves: " + curPageAfter[1].getPtCurves().size());
        System.out.println("[DELETE CURVES AFTER SCENE CHANGE] Page[0] obj: " + System.identityHashCode(curPageAfter[0]));
        System.out.println("[DELETE CURVES AFTER SCENE CHANGE] Page[1] obj: " + System.identityHashCode(curPageAfter[1]));
        
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