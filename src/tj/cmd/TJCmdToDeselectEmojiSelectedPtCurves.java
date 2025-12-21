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

public class TJCmdToDeselectEmojiSelectedPtCurves extends XLoggableCmd {
    // fields
    private ArrayList<TJPtCurve> mUnselectedPtCurves = null;
    private ArrayList<TJPtCurve> mSelectedPtCurves = null;
    private int mNumOfDeselectedPtCurves = Integer.MIN_VALUE;
    
    // private constructor 
    private TJCmdToDeselectEmojiSelectedPtCurves(XApp app) {
        super(app);
    }
    
    // TJCmdToDeselectEmojiSelectedPtCurves.execute(app);
    public static boolean execute(XApp app) {
        TJCmdToDeselectEmojiSelectedPtCurves cmd = 
            new TJCmdToDeselectEmojiSelectedPtCurves(app);
        return cmd.execute();
    }
    
    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        
        // Get target emoji page from emoji scenario
        TJEmojiScenario emojiScenario = TJEmojiScenario.getSingle();
        TJEmojiPage targetEmojiPage = emojiScenario.getTargetEmojiPage();
        
        if (targetEmojiPage != null) {
            this.mSelectedPtCurves = new ArrayList<>(targetEmojiPage.getSelectedPtCurves());
            this.mNumOfDeselectedPtCurves = this.mSelectedPtCurves.size();
            
            // Clear selected curves from emoji page
            targetEmojiPage.getSelectedPtCurves().clear();

            // Also clear selected decorations and restore alpha
            if (targetEmojiPage.getSelectedDecorations() != null) {
                for (TJImage img : targetEmojiPage.getSelectedDecorations()) {
                    img.setAlpha(1.0f);
                }
                targetEmojiPage.getSelectedDecorations().clear();
            }
            // Clear single-selection state in scenario
            emojiScenario.setSelectedDecoration(null);
        } else {
            this.mSelectedPtCurves = new ArrayList<>();
            this.mNumOfDeselectedPtCurves = 0;
        }
        
        XCmdToChangeScene.execute(tj,
            TJEmojiScenario.EmojiDrawScene.getSingleton(), null);
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
