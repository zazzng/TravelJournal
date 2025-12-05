package tj.scenario;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJPage;
import tj.TJPageMgr;
import tj.TJScene;
import x.XApp;
import x.XScenario;

public class TJHomeScenario extends XScenario {
    // constants
    private static final double HIDDEN_PAGE_RATIO = 0.94;
    private static final double HIDDEN_PAGE_OFFSET_X = 0.075;
    private static final double HIDDEN_PAGE_OFFSET_Y = 0.03;
    private static final int PAGE_CORNER_ARC = 25;
    private static final double BUTTON_DIAMETER_RATIO = 0.03;
    
    private static final Color CURRENT_PAGE_COLOR = new Color(255, 255, 255);
    private static final Color HIDDEN_PAGE_COLOR = new Color(245, 245, 245);
    private static final Color ACTIVE_BUTTON_COLOR = new Color(50, 50, 50);
    private static final Color DISABLED_BUTTON_COLOR = new Color(215, 215, 215);
    private static final Color PAGE_BORDER_COLOR = java.awt.Color.LIGHT_GRAY;
    private static final Stroke PAGE_BORDER_STROKE = new BasicStroke(1f);
    
    // singleton pattern
    private static TJHomeScenario mSingleton = null;
    public static TJHomeScenario getSingle() {
        assert(TJHomeScenario.mSingleton != null);
        return TJHomeScenario.mSingleton;
    }
    public static TJHomeScenario createSingleton(XApp app) {
        assert(TJHomeScenario.mSingleton == null);
        TJHomeScenario.mSingleton = new TJHomeScenario(app);
        return TJHomeScenario.mSingleton;
    }
    private TJHomeScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJHomeScenario.CatalogueScene.createSingleton(this));
    }

    public static class CatalogueScene extends TJScene {
        // fields for UI components specific to this scene
        private JPanel mCataloguePanel = null;
        private JButton mLoadBtn;
        private JButton mSaveBtn;
        private JButton mAddBtn;
        private JButton mDeleteBtn;
        
        private Rectangle mPrevNavBounds = null;
        private Rectangle mNextNavBounds = null;
        
        // singleton pattern
        private static CatalogueScene mSingleton = null;
        public static CatalogueScene getSingleton() {
            assert(CatalogueScene.mSingleton != null);
            return CatalogueScene.mSingleton;
        }
        public static CatalogueScene createSingleton(XScenario scenario) {
            assert(CatalogueScene.mSingleton == null);
            CatalogueScene.mSingleton = new CatalogueScene(scenario);
            CatalogueScene.mSingleton.initializeControls(); 
            return CatalogueScene.mSingleton;
        }
        private CatalogueScene(XScenario scenario) {
            super(scenario);
        }
        
        private void initializeControls() {
            TJ tj = (TJ)this.mScenario.getApp();
            TJPageMgr pageMgr = tj.getPageMgr();

            // create buttons
            mLoadBtn = new JButton("Load");
            mSaveBtn = new JButton("Save");
            mDeleteBtn = new JButton("- Delete");
            mAddBtn = new JButton("+ Add");

            // connect actions
            mLoadBtn.addActionListener(e -> {
                try {
                    tj.getPageMgr().loadJournal();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            mSaveBtn.addActionListener(e -> tj.getPageMgr().saveJournal());
            mAddBtn.addActionListener(e -> pageMgr.addEmptyPage());
            mDeleteBtn.addActionListener(e -> pageMgr.deleteCurPage());
            
            // create panel and add buttons
            mCataloguePanel = new JPanel(new FlowLayout());
            mCataloguePanel.add(mLoadBtn);
            mCataloguePanel.add(mSaveBtn);
            mCataloguePanel.add(mAddBtn);
            mCataloguePanel.add(mDeleteBtn);
            
            // set style
            mCataloguePanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
            mCataloguePanel.setBackground(new Color(220, 220, 220)); 
        }
        
        private void updateNavigationBounds(int pageHeight, int pageWidth, int startX, int startY, int appWidth) {
            // button dimensions
            int navDiameter = (int)(appWidth * TJHomeScenario.BUTTON_DIAMETER_RATIO);
            int navWidth = navDiameter;
            int navHeight = navDiameter;
            
            int navY = startY + (pageHeight / 2) - (navHeight / 2);

            // previous button (left of the book)
            int prevX = startX - navWidth - 50; // 10px margin
            mPrevNavBounds = new Rectangle(prevX, navY, navWidth, navHeight);
            
            // next button (right of the book)
            int nextX = startX + pageWidth * 2 + 50; // 10px margin
            mNextNavBounds = new Rectangle(nextX, navY, navWidth, navHeight);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJPageMgr pageMgr = tj.getPageMgr();
            int mx = e.getX();
            int my = e.getY();
            
            int curIndex = pageMgr.getCurPageIndex();
            int totalPages = pageMgr.getJournalPages().size();
            
            // 1. click on previous button
            if (mPrevNavBounds != null && mPrevNavBounds.contains(mx, my)) {
                if (curIndex > 0) { 
                    pageMgr.setCurPageIndex(curIndex - 1);
                }
                return;
            }
            
            // 2. click on next button
            if (mNextNavBounds != null && mNextNavBounds.contains(mx, my)) {
                if (curIndex < totalPages - 1) { 
                    pageMgr.setCurPageIndex(curIndex + 1);
                }
                return;
            }
            
            // 3. click on the page 
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
           
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
        }

        @Override
        public void updateSupportObjects() {
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJHomeScenario scenario = (TJHomeScenario)this.mScenario;
            
            TJ tj = (TJ)scenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            int appWidth = canvas.getWidth();
            int appHeight = canvas.getHeight();
            int pageHeight = (int)(appHeight * 0.7);
            int pageWidth = (int)(pageHeight * TJCanvas2D.PAGE_ASPECT_RATIO);
            int bookHeight = pageHeight;
            int startX = (appWidth - pageWidth * 2) / 2;
            int startY = (appHeight - bookHeight) / 2;
            updateNavigationBounds(pageHeight, pageWidth, startX, startY, appWidth);
            
            scenario.drawJournalBook(g2);
            scenario.drawNavigationButtons(g2);
        }

        @Override
        public void renderScreenObjects(Graphics2D g2) {
        }

        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setControlPanel(this.mCataloguePanel);
            updateSupportObjects();
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setControlPanel(null);
        }
    }
    
    private void drawSinglePage(Graphics2D g2, TJPage page, int x, int y, int width, int height, Color fillColor) {
        // fill the background
        g2.setColor(fillColor);
        g2.fillRoundRect(x, y, width, height,
            TJHomeScenario.PAGE_CORNER_ARC, TJHomeScenario.PAGE_CORNER_ARC);

        // draw the border
        g2.setStroke(TJHomeScenario.PAGE_BORDER_STROKE);
        g2.setColor(TJHomeScenario.PAGE_BORDER_COLOR);
        g2.drawRoundRect(x, y, width, height,
            TJHomeScenario.PAGE_CORNER_ARC, TJHomeScenario.PAGE_CORNER_ARC);
        
        // draw the page content (if any)
        if (!page.isBlankHiddenPage()) {
             // draw the page content here
        }
    }
    
    public void drawJournalBook(Graphics2D g2) {
        TJ tj = (TJ)this.getApp();
        TJCanvas2D canvas = tj.getCanvas2D();

        int appWidth = canvas.getWidth();
        int appHeight = canvas.getHeight();
        
        int pageHeight = (int)(appHeight * 0.7);
        int pageWidth = (int)(pageHeight * TJCanvas2D.PAGE_ASPECT_RATIO);
        int hiddenPageHeight = (int)(pageHeight * TJHomeScenario.HIDDEN_PAGE_RATIO);
        int hiddenPageWidth = (int)(pageWidth * TJHomeScenario.HIDDEN_PAGE_RATIO);
        
        int bookHeight = pageHeight;
        int bookWidth = pageWidth * 2;
        
        // top-left corner of the book
        int startX = (appWidth - bookWidth) / 2;
        int startY = (appHeight - bookHeight) / 2;

        // CALCULATE PAGE POSITIONS
        // current pages
        int leftPageX = startX;
        int rightPageX = startX + pageWidth;

        // sneak peek offset for the prev and next pages
        int peekOffsetX = (int)(pageWidth * TJHomeScenario.HIDDEN_PAGE_OFFSET_X);
        int peekOffsetY = (int)(pageHeight * TJHomeScenario.HIDDEN_PAGE_OFFSET_Y);
        int offsetNextPageX = (int) ((int)pageWidth * (1.0 - TJHomeScenario.HIDDEN_PAGE_RATIO));
        
        // previous and next pages starting points
        int prevPageX = leftPageX - peekOffsetX;
        int nextPageX = rightPageX + peekOffsetX + offsetNextPageX;
        int hiddenPageY = startY + peekOffsetY;
        
        // fetch journal entries
        TJPage[] curPage = tj.getPageMgr().getCurPage(); 
        TJPage[] prevPage = tj.getPageMgr().getPrevPage();
        TJPage[] nextPage = tj.getPageMgr().getNextPage();
        
        assert(curPage != null);
        
        // DRAW VISIBLE PAGES
        // previous page
        drawSinglePage(g2, prevPage[0], prevPageX, hiddenPageY, hiddenPageWidth, 
            hiddenPageHeight, HIDDEN_PAGE_COLOR);
        
        // next page
        drawSinglePage(g2, nextPage[1], nextPageX, hiddenPageY, hiddenPageWidth, 
            hiddenPageHeight, HIDDEN_PAGE_COLOR);
        
        // current left page
        drawSinglePage(g2, curPage[0], leftPageX, startY, pageWidth, pageHeight, 
            CURRENT_PAGE_COLOR);
        
        // current right page
        drawSinglePage(g2, curPage[1], rightPageX, startY, pageWidth, pageHeight, 
            CURRENT_PAGE_COLOR);
    }
    
    public void drawNavigationButtons(Graphics2D g2) {
        CatalogueScene scene = CatalogueScene.getSingleton();
        
        if (scene.mPrevNavBounds == null || scene.mNextNavBounds == null) return;
        
        TJ tj = (TJ)this.getApp();
        TJPageMgr pageMgr = tj.getPageMgr();
        int curIndex = pageMgr.getCurPageIndex();
        int totalPages = pageMgr.getJournalPages().size();
        
        g2.setFont(g2.getFont().deriveFont(20f));

        Rectangle prev = scene.mPrevNavBounds;
        Color prevColor = (curIndex > 0) ? ACTIVE_BUTTON_COLOR : DISABLED_BUTTON_COLOR;
        
        g2.setColor(prevColor);
        g2.fillOval(prev.x, prev.y, prev.width, prev.height);
        
        g2.setColor(Color.WHITE);
        String prevLabel = "<";
        int strWidth = g2.getFontMetrics().stringWidth(prevLabel);
        int strHeight = g2.getFontMetrics().getAscent();
        g2.drawString(prevLabel, prev.x + prev.width/2 - strWidth/2, 
            prev.y + prev.height/2 + strHeight/4);

        Rectangle next = scene.mNextNavBounds;
        Color nextColor = (curIndex < totalPages - 1) ? ACTIVE_BUTTON_COLOR : DISABLED_BUTTON_COLOR;
        
        g2.setColor(nextColor);
        g2.fillOval(next.x, next.y, next.width, next.height);
        
        g2.setColor(Color.WHITE);
        String nextLabel = ">";
        strWidth = g2.getFontMetrics().stringWidth(nextLabel);
        strHeight = g2.getFontMetrics().getAscent();
        g2.drawString(nextLabel, next.x + next.width/2 - strWidth/2, 
            next.y + next.height/2 + strHeight/4);
    }
}