package tj.cmd;

import java.awt.Point;
import tj.TJ;
import tj.TJSelectionBox;
import tj.scenario.TJEmojiSelectScenario;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToCreateEmojiSelectionBox extends XLoggableCmd {
    // fields
    private Point mScreenPt = null;
    
    // private constructor
    private TJCmdToCreateEmojiSelectionBox(XApp app, Point pt) {
        super(app);
        this.mScreenPt = pt;
    }
    
    // TJCmdToCreateEmojiSelectionBox.execute(app, pt);
    public static boolean execute(XApp app, Point pt) {
        TJCmdToCreateEmojiSelectionBox cmd = new TJCmdToCreateEmojiSelectionBox(app, pt);
        return cmd.execute();
    }
    
    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        Point pt = this.mScreenPt;
        TJSelectionBox selectionBox = new TJSelectionBox(pt);
        TJEmojiSelectScenario.getSingle().setSelectionBox(selectionBox);
        System.out.println("[CREATE EMOJI SELECTION BOX] Created at " + pt);
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mScreenPt);
        return sb.toString();
    }
}
