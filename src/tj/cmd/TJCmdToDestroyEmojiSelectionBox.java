package tj.cmd;

import tj.TJ;
import tj.scenario.TJEmojiSelectScenario;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToDestroyEmojiSelectionBox extends XLoggableCmd {
    // private constructor
    private TJCmdToDestroyEmojiSelectionBox(XApp app) {
        super(app);
    }
    
    public static boolean execute(XApp app) {
        TJCmdToDestroyEmojiSelectionBox cmd = new TJCmdToDestroyEmojiSelectionBox(app);
        return cmd.execute();
    }
    
    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ)this.mApp;
        TJEmojiSelectScenario.getSingle().setSelectionBox(null);
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName());
        return sb.toString();
    }
}
