package tj.cmd;

import tj.TJ;
import tj.scenario.TJSelectScenario;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToDestroySelectionBox extends XLoggableCmd {
    // private constructor
    private TJCmdToDestroySelectionBox(XApp app) {
        super(app);
    }
    
    public static boolean execute(XApp app) {
        TJCmdToDestroySelectionBox cmd = new TJCmdToDestroySelectionBox(app);
        return cmd.execute();
    }
    
    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName());
        return sb.toString();
    }
}