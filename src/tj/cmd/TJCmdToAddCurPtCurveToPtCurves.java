package tj.cmd;

import tj.TJ;
import tj.TJPage;
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
        TJCmdToAddCurPtCurveToPtCurves cmd =
            new TJCmdToAddCurPtCurveToPtCurves(app);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        this.mCurPtCurve = tj.getPtCurveMgr().getCurPtCurve();

        // Add ONLY to regular journal pages, never to emoji pages
        TJPage[] curPageSpread = tj.getJournalBookMgr().getCurPage();
        TJPage page = null;
        if (curPageSpread != null && curPageSpread.length >= 2) {
            page = curPageSpread[1];  // Right page for journal drawing
        }
        
        if (page != null && this.mCurPtCurve != null &&
            this.mCurPtCurve.getPts().size() >= 2) {

            System.out.println(
                "[DRAW-CURVE] Adding curve to JOURNAL page. Curves BEFORE: " +
                page.getPtCurves().size()
            );

            page.addPtCurve(this.mCurPtCurve);

            System.out.println(
                "[DRAW-CURVE] Curve added. Curves AFTER: " +
                page.getPtCurves().size()
            );
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
