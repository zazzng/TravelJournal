package tj.cmd;

import java.awt.Point;
import tj.TJ;
import tj.TJSelectionBox;
import tj.scenario.TJSelectScenario;
import x.XApp;
import x.XLoggableCmd;


public class TJCmdToUpdateSelectionBox extends XLoggableCmd {
    // fields 
    private Point mScreenPt = null;
    private TJSelectionBox mSelectionBox = null;
    
    // private constructor 
    private TJCmdToUpdateSelectionBox(XApp app, Point pt) {
        super(app);
        this.mScreenPt = pt;                
    }
    
    // TJCmdToUpdateSelectionBox.execute(app, pt);
    public static boolean execute(XApp app, Point pt) {
        TJCmdToUpdateSelectionBox cmd = new TJCmdToUpdateSelectionBox(app, pt);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJSelectScenario scenario = TJSelectScenario.getSingle();
        TJSelectionBox selectionBox = scenario.getSelectionBox();
        if (selectionBox != null) {
            selectionBox.update(this.mScreenPt);
            this.mSelectionBox = selectionBox;
            System.out.println("[UPDATE SELECTION BOX] Updated to " + this.mScreenPt);
        }
        return true;
    }
    
    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mScreenPt).append("\t");
        sb.append(this.mSelectionBox);
        return sb.toString();
    }
}
