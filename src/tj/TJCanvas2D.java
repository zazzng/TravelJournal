package tj;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JPanel;

public class TJCanvas2D extends JPanel {
    // constants
    private static final Color COLOR_PT_CURVE_DEFAULT = new Color(0, 0, 0);

    private static final Stroke STROKE_PT_CURVE_DEFAULT = new BasicStroke(5f);
    
    public static final double PAGE_ASPECT_RATIO = 0.75;

    // fields
    private TJ mTJ = null;
    private Color mCurColorForPtCurve = null;
    public Color getCurColorForPtCurve() {
        return this.mCurColorForPtCurve;
    }
    public void setCurColorForPtCurve(Color c) {
        this.mCurColorForPtCurve = c;
    }
    
    private Stroke mCurStrokeForPtCurve = null;
    public Stroke getCurStrokeForPtCurve() {
        return this.mCurStrokeForPtCurve;
    }
    
    // constructor
    public TJCanvas2D(TJ tj) {
        this.mTJ = tj;
        this.mCurColorForPtCurve = TJCanvas2D.COLOR_PT_CURVE_DEFAULT;
        this.mCurStrokeForPtCurve = TJCanvas2D.STROKE_PT_CURVE_DEFAULT;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // common world objects
        
        // current scene's world objects
        TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
        curScene.renderWorldObjects(g2);
        
        // common screen objects
        
        // current scene's world obkects
        curScene.renderScreenObjects(g2);
    }
    
    
}
