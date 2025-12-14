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
        scenario.getSelectionBox().update(this.mScreenPt);
        this.mSelectionBox = scenario.getSelectionBox();
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
