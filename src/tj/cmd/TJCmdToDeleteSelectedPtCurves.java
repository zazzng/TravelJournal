package tj.cmd;

import java.util.ArrayList;
import tj.TJ;
import tj.TJPage;
import tj.TJPtCurve;
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
        TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
        if (curPage != null) {  
            this.mSelectedPtCurves = new ArrayList<>(curPage[0].getSelectedPtCurves());
            this.mSelectedPtCurves.addAll(curPage[1].getSelectedPtCurves());
            System.out.println("[DELETE] Collecting " + this.mSelectedPtCurves.size() + " curves from pages");
        } else {
            this.mSelectedPtCurves = new ArrayList<>();
        }
        this.mNumOfDeletedPtCurves = this.mSelectedPtCurves.size();

        if (curPage != null) {
            System.out.println("[DELETE] Before: Page[0]=" + curPage[0].getPtCurves().size() + 
                             ", Page[1]=" + curPage[1].getPtCurves().size());
            curPage[0].getPtCurves().removeAll(this.mSelectedPtCurves);
            curPage[1].getPtCurves().removeAll(this.mSelectedPtCurves);
            System.out.println("[DELETE] After: Page[0]=" + curPage[0].getPtCurves().size() + 
                             ", Page[1]=" + curPage[1].getPtCurves().size());

            curPage[0].getSelectedPtCurves().clear();
            curPage[1].getSelectedPtCurves().clear();
        }

        System.out.println("[DELETE] Removing from PtCurveMgr: " + this.mSelectedPtCurves.size() + " curves");
        tj.getPtCurveMgr().getPtCurves().removeAll(this.mSelectedPtCurves);
        tj.getPtCurveMgr().getSelectedPtCurves().clear();

        XCmdToChangeScene.execute(tj, 
            TJDrawScenario.DrawReadyScene.getSingleton(), null);

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