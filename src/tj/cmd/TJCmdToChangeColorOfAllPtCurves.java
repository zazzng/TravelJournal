package tj.cmd;

import java.awt.Color;
import java.util.ArrayList;

import tj.TJ;
import tj.TJPtCurve;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToChangeColorOfAllPtCurves extends XLoggableCmd {
    // fields
    private Color mColor = null;
    
    // private constructor
    private TJCmdToChangeColorOfAllPtCurves(XApp app, Color c) {
        super(app);
        this.mColor = c;
    }
    
    public static boolean execute(XApp app, Color c) {
        TJCmdToChangeColorOfAllPtCurves cmd = new TJCmdToChangeColorOfAllPtCurves(app, c);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ) this.mApp;
        
        // 1. Get all unselected curves
        ArrayList<TJPtCurve> allCurves = tj.getPtCurveMgr().getPtCurves();
        
        // 2. Apply the new color to each curve
        for (TJPtCurve curve : allCurves) {
            curve.setColor(this.mColor);
        }
        
        // 3. Update the screen
        tj.getCanvas2D().repaint();
        return true;
    }

    @Override
    protected String createLog() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName()).append("\t");
        return sb.toString();
    }
}
