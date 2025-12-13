package tj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
    
    private JPanel mTopPanel = null;
    public JPanel getCurTopPanel() {
        return this.mTopPanel;
    }
    
    private JPanel mBottomPanel = null;
    public JPanel getCurBottomPanel() {
        return this.mBottomPanel;
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
    
    private TJJournalBookMgr mJournalBookMgr = null;
    public TJJournalBookMgr getJournalBookMgr() {
        return this.mJournalBookMgr;
    }
    
    private TJPageMgr mPageMgr = null;
    public TJPageMgr getPageMgr() {
        return this.mPageMgr;
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
    
    public TJ() throws IOException {
        // create components
        // 1) Frame, 2) Canvas, 3) Other components
        // 4) Event listeners, 5) Managers
        this.mFrame = new JFrame("TravelJournal");
        this.mFrame.setLayout(new BorderLayout());
        this.mCanvas2D = new TJCanvas2D(this);
        this.mXform = new TJXform();
        this.mColorChooser = new TJColorChooser();
        this.mEventListener = new TJEventListener(this);
        this.mJournalBookMgr = new TJJournalBookMgr(this);
        this.mPageMgr = new TJPageMgr(this);
        this.mPenMarkMgr = new TJPenMarkMgr();
        this.mPtCurveMgr = new TJPtCurveMgr();
        this.mScenarioMgr = new TJScenarioMgr(this);
        this.mLogMgr = new XLogMgr();
        this.mLogMgr.setPrintOn(true);
        
        // load or initialize journal data
//        boolean loadSuccess = this.mPageMgr.loadJournal();
//        if (!loadSuccess || this.mPageMgr.getJournalPages().isEmpty()) {
//            // Start with a blank spread if loading failed or file was empty
//            this.mPageMgr.addEmptyPage(); 
//        }
        
        // connect event listeners
        this.mCanvas2D.addMouseListener(this.mEventListener);
        this.mCanvas2D.addMouseMotionListener(this.mEventListener);
        this.mCanvas2D.setFocusable(true);
        this.mCanvas2D.addKeyListener(this.mEventListener);
        
        // build and show visual components
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double widthRatio = 0.75;
        double heightRatio = 0.75;
        int appWidth = (int)(screenSize.width * widthRatio);
        int appHeight = (int)(screenSize.height * heightRatio);
        
        this.mFrame.add(this.mCanvas2D, BorderLayout.CENTER);
        this.mFrame.setSize(appWidth, appHeight);
        this.mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mFrame.setVisible(true);
    }
    
    public void setTopPanel(JPanel newPanel) {
        if (this.mTopPanel != null) {
            this.mFrame.remove(this.mTopPanel);
        }
        
        this.mTopPanel = newPanel;
        
        if (this.mTopPanel != null) {
            this.mFrame.add(this.mTopPanel, BorderLayout.NORTH);
        }
        
        refreshFrame();
    }
    
    public void setBottomPanel(JPanel newPanel) {
        if (this.mBottomPanel != null) {
            this.mFrame.remove(this.mBottomPanel);
        }
        
        this.mBottomPanel = newPanel;
        
        if (this.mBottomPanel != null) {
            this.mFrame.add(this.mBottomPanel, BorderLayout.SOUTH);
        }
        
        refreshFrame();
    }
    
    private void refreshFrame() {
        this.mFrame.revalidate();
        this.mFrame.repaint();
    }
    
    public static void main(String[] args) throws IOException {
        // create a TJ instance
        new TJ();
    }
}
