package tj.cmd;

import java.util.ArrayList;
import tj.TJ;
import tj.TJEmojiPage;
import tj.TJPtCurve;
import tj.TJImage;
import tj.scenario.TJEmojiScenario;
import x.XApp;
import x.XCmdToChangeScene;
import x.XLoggableCmd;

public class TJCmdToDeleteEmojiSelectedPtCurves extends XLoggableCmd {
    // fields
    private ArrayList<TJPtCurve> mSelectedPtCurves = null;
    private int mNumOfDeletedPtCurves = Integer.MIN_VALUE;
    
    // private constructor
    private TJCmdToDeleteEmojiSelectedPtCurves(XApp app) {
        super(app);
    }

    public static boolean execute(XApp app) {
        TJCmdToDeleteEmojiSelectedPtCurves cmd = 
            new TJCmdToDeleteEmojiSelectedPtCurves(app);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        TJEmojiScenario emojiScenario = TJEmojiScenario.getSingle();
        TJEmojiPage emojiPage = emojiScenario.getTargetEmojiPage();
        
        if (emojiPage != null) {
            this.mSelectedPtCurves = new ArrayList<>(emojiPage.getSelectedPtCurves());
            System.out.println("[DELETE-EMOJI] Collecting " + this.mSelectedPtCurves.size() + " curves from emoji page");
        } else {
            this.mSelectedPtCurves = new ArrayList<>();
        }
        
        this.mNumOfDeletedPtCurves = this.mSelectedPtCurves.size();
        
        if (emojiPage != null) {
            System.out.println("[DELETE-EMOJI] Before: " + emojiPage.getPtCurves().size() + " curves total");
            emojiPage.getPtCurves().removeAll(this.mSelectedPtCurves);
            emojiPage.getSelectedPtCurves().clear();
            System.out.println("[DELETE-EMOJI] After: " + emojiPage.getPtCurves().size() + " curves total");

            // Also delete selected decorations
            ArrayList<TJImage> selectedDecos = emojiPage.getSelectedDecorations();
            if (selectedDecos != null && !selectedDecos.isEmpty()) {
                System.out.println("[DELETE-EMOJI] Deleting " + selectedDecos.size() + " selected decorations");
                emojiPage.getDecorations().removeAll(selectedDecos);
                selectedDecos.clear();
            }
        }
        
        System.out.println("[DELETE-EMOJI] Removing from PtCurveMgr: " + this.mSelectedPtCurves.size() + " curves");
        tj.getPtCurveMgr().getPtCurves().removeAll(this.mSelectedPtCurves);
        tj.getPtCurveMgr().getSelectedPtCurves().clear();
        
        XCmdToChangeScene.execute(tj, 
            TJEmojiScenario.EmojiDrawScene.getSingleton(), null);
        
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