package tj.Command;
import tj.TJ;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToDoSomething extends XLoggableCmd{
    // fields
    // ...
    
    // private constructor
    private TJCmdToDoSomething (XApp app) {
        super(app);
    } 
    
    public static boolean execute (XApp app) {
        TJCmdToDoSomething cmd = new TJCmdToDoSomething(app);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ) this.mApp;
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
       
        return sb.toString();
    }
}
