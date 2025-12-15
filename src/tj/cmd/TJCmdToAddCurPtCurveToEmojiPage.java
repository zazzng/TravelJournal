package tj.cmd;

import tj.TJ;
import tj.TJEmojiPage;
import tj.TJPtCurve;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToAddCurPtCurveToEmojiPage extends XLoggableCmd {
    // fields
    private TJPtCurve mCurPtCurve = null;
    
    // private constructor
    private TJCmdToAddCurPtCurveToEmojiPage(XApp app) {
        super(app);
    }

    public static boolean execute(XApp app) {
        TJCmdToAddCurPtCurveToEmojiPage cmd =
            new TJCmdToAddCurPtCurveToEmojiPage(app);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        this.mCurPtCurve = tj.getPtCurveMgr().getCurPtCurve();

        // Add ONLY to emoji page, never to regular journal pages
        TJEmojiPage emojiPage = tj.getJournalBookMgr().getCurEmojiPage();
        
        if (emojiPage != null && this.mCurPtCurve != null &&
            this.mCurPtCurve.getPts().size() >= 2) {
            
            System.out.println(
                "[EMOJI-CURVE] Adding curve to EMOJI page. Curves BEFORE: " +
                emojiPage.getPtCurves().size()
            );

            emojiPage.addPtCurve(this.mCurPtCurve);

            System.out.println(
                "[EMOJI-CURVE] Curve added. Curves AFTER: " +
                emojiPage.getPtCurves().size()
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
