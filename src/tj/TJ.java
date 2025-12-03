package tj;

import javax.swing.JFrame;

import x.XApp;
import x.XLogMgr;
import x.XScenarioMgr;

public class TJ extends XApp {
    // fields
    private JFrame mFrame = null;
    private TJCanvas2D mCanvas2D = null;
    public TJCanvas2D getCanvas2D() {
        return this.mCanvas2D;
    }
    
    private TJEventListener mEventListener = null;
    public TJEventListener getEventListener() {
        return this.mEventListener;
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
    
    public TJ() {
        // create components
        // 1) Frame, 2) Canvas, 3) Other components
        // 4) Event listeners, 5) Managers
        this.mFrame = new JFrame("TravelJournal");
        this.mCanvas2D = new TJCanvas2D(this);
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
        this.mFrame.setSize(800, 600);
        this.mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mFrame.setVisible(true);
    }
    
    public static void main(String[] args) {
        // create a TJ instance
        new TJ();
    }
}
