package tj.cmd;

import java.awt.Color;
import java.util.ArrayList;

import tj.TJ;
import tj.TJPtCurve;
import x.XApp;
import x.XLoggableCmd;

public class TJCmdToChangeColorOfSelectedPtCurves extends XLoggableCmd{
    // fields
    private Color mColor = null;
    
    // private constructor
    private TJCmdToChangeColorOfSelectedPtCurves (XApp app, Color c) {
        super(app);
        this.mColor = c;
    } 
    
    public static boolean execute (XApp app, Color c) {
        TJCmdToChangeColorOfSelectedPtCurves cmd = new TJCmdToChangeColorOfSelectedPtCurves(app, c);
        return cmd.execute();
    }

    @Override
    protected boolean defineCmd() {
        TJ tj = (TJ) this.mApp;
        // 1. Get selected curves
        ArrayList<TJPtCurve> selected = tj.getPtCurveMgr().getSelectedPtCurves();

        // 2. Apply the new color to each selected curve
        for (TJPtCurve curve : selected) {
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
 