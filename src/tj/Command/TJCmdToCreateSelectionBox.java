package tj.Command;

import java.awt.Point;
import tj.TJ;
import tj.TJSelectionBox;
import tj.scenario.TJSelectScenario;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToCreateSelectionBox extends XLoggableCmd {
    // fields
    private Point mScreenPt = null;
    
    // private constructor
    private TJCmdToCreateSelectionBox(XApp app, Point pt) {
        super(app);
        this.mScreenPt = pt;
    }
    
    // TJCmdToCreateSelectionBox.execute(app, pt);
    public static boolean execute(XApp app, Point pt) {
        TJCmdToCreateSelectionBox cmd = new TJCmdToCreateSelectionBox(app, pt);
        return cmd.execute();
    }
    
    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        Point pt = this.mScreenPt;
        TJSelectionBox selectionBox = new TJSelectionBox(pt);
        TJSelectScenario.getSingle().setSelectionBox(selectionBox);
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