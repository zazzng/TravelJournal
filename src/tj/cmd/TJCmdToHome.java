package tj.cmd;

import java.awt.geom.AffineTransform;
import tj.TJ;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToHome extends XLoggableCmd{
    // fields
    private AffineTransform mXformBefore = null;
    private AffineTransform mXformAfter = null;
    
    // private constructor
    private TJCmdToHome (XApp app) {
        super(app);
    } 
    
    public static boolean execute (XApp app) {
        TJCmdToHome cmd = new TJCmdToHome(app);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ) this.mApp;
        this.mXformBefore = new AffineTransform(
            tj.getXform().getCurrentXformFromWorldToScreen());
        tj.getXform().home();
        this.mXformAfter = tj.getXform().getCurrentXformFromWorldToScreen();
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mXformBefore).append("\t");
        sb.append(this.mXformAfter);
        return sb.toString();
    }
}
