package tj.Command;

import java.awt.Point;
import tj.TJ;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToTranslateTo extends XLoggableCmd{
    // fields
    private Point mPoint = null;
    
    // private constructor
    private TJCmdToTranslateTo (XApp app, Point pt) {
        super(app);
        this.mPoint = pt;
    } 
    
    public static boolean execute (XApp app, Point pt) {
        TJCmdToTranslateTo cmd = new TJCmdToTranslateTo(app, pt);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ) this.mApp;
        tj.getXform().translateTo(this.mPoint);
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        sb.append(this.mPoint);
       
        return sb.toString();
    }
    
}
