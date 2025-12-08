package tj;

import javax.swing.JFrame;
import x.XApp;
import x.XLogMgr;
import x.XScenarioMgr;

public class TJ extends XApp {
    // fields
    public static int WIDTH = 800;
    public static int HEIGHT = 600;
    
    private JFrame mFrame = null;
    private TJCanvas2D mCanvas2D = null;
    public TJCanvas2D getCanvas2D() {
        return this.mCanvas2D;
    }

    private TJXform mXform = null;
    public TJXform getXform() {
        return this.mXform;
    }
    
    private TJColorChooser mColorChooser = null;
    public TJColorChooser getColorChooser() {
        return this.mColorChooser;
    }
    
    private TJEventListener mEventListener = null;
    public TJEventListener getEventListener() {
        return this.mEventListener;
    }
    
    private TJPenMarkMgr mPenMarkMgr = null;
    public TJPenMarkMgr getPenMarkMgr() {
        return this.mPenMarkMgr;
    }
    
    private TJPtCurveMgr mPtCurveMgr = null;
    public TJPtCurveMgr getPtCurveMgr() {
        return this.mPtCurveMgr;
    }
    
    private XScenarioMgr mScenarioMgr = null;
    @Override
    public XScenarioMgr getScenarioMgr() {
        return this.mScenarioMgr;
    }
    
    private XLogMgr mLogMgr = null;
    @Override
    public XLogMgr getLogMgr() {
        return this.mLogMgr;
    }

    // constructor
    public TJ() {
        this.mFrame = new JFrame("JustSketchIt");
        this.mCanvas2D = new TJCanvas2D(this);
        this.mXform = new TJXform();
        this.mColorChooser = new TJColorChooser();
        this.mEventListener = new TJEventListener(this);
        this.mPenMarkMgr = new TJPenMarkMgr();
        this.mPtCurveMgr = new TJPtCurveMgr();
        this.mScenarioMgr = new TJScenarioMgr(this);
        this.mLogMgr = new XLogMgr();
        this.mLogMgr.setPrintOn(true);
        
        // connect event listeners
        this.mCanvas2D.addMouseListener(this.mEventListener);
        this.mCanvas2D.addMouseMotionListener(this.mEventListener);
        this.mCanvas2D.setFocusable(true);
        this.mCanvas2D.addKeyListener(this.mEventListener);
        
        // build and show visual components
        this.mFrame.add(this.mCanvas2D);
        this.mFrame.setSize(WIDTH, HEIGHT);
        this.mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mFrame.setVisible(true);
    }

    public static void main(String[] args) {
        // create a TJ instance
        new TJ();
    }
}