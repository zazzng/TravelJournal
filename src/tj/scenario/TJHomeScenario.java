package tj.scenario;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJScene;
import x.XApp;
import x.XScenario;

public class TJHomeScenario extends XScenario {
    // constants
    private static final double HIDDEN_PAGE_RATIO = 0.94;
    private static final double HIDDEN_PAGE_OFFSET_X = 0.075;
    private static final double HIDDEN_PAGE_OFFSET_Y = 0.03;
    private static final int PAGE_CORNER_ARC = 25;
    
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
        // singleton pattern
        private static CatalogueScene mSingleton = null;
        public static CatalogueScene getSingleton() {
            assert(CatalogueScene.mSingleton != null);
            return CatalogueScene.mSingleton;
        }
        public static CatalogueScene createSingleton(XScenario scenario) {
            assert(CatalogueScene.mSingleton == null);
            CatalogueScene.mSingleton = new CatalogueScene(scenario);
            return CatalogueScene.mSingleton;
        }
        private CatalogueScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
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
            scenario.drawJournalBook(g2);
        }

        @Override
        public void renderScreenObjects(Graphics2D g2) {
        }

        @Override
        public void getReady() {
        }

        @Override
        public void wrapUp() {
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

        // DRAW VISIBLE PAGES
        // previous page
        g2.setColor(new java.awt.Color(245, 245, 245));
        g2.fillRoundRect(prevPageX, hiddenPageY, hiddenPageWidth, hiddenPageHeight,
            TJHomeScenario.PAGE_CORNER_ARC, TJHomeScenario.PAGE_CORNER_ARC);
        
        // next page
        g2.setColor(new java.awt.Color(245, 245, 245));
        g2.fillRoundRect(nextPageX, hiddenPageY, hiddenPageWidth, hiddenPageHeight,
            TJHomeScenario.PAGE_CORNER_ARC, TJHomeScenario.PAGE_CORNER_ARC);
        
        // border of prev and next pages
        g2.setColor(TJHomeScenario.PAGE_BORDER_COLOR);
        g2.setStroke(TJHomeScenario.PAGE_BORDER_STROKE);
        g2.drawRoundRect(prevPageX, hiddenPageY, hiddenPageWidth, hiddenPageHeight,
            TJHomeScenario.PAGE_CORNER_ARC, TJHomeScenario.PAGE_CORNER_ARC);
        g2.drawRoundRect(nextPageX, hiddenPageY, hiddenPageWidth, hiddenPageHeight,
            TJHomeScenario.PAGE_CORNER_ARC, TJHomeScenario.PAGE_CORNER_ARC);

        // current left page
        g2.setColor(new java.awt.Color(255, 255, 255));
        g2.fillRoundRect(leftPageX, startY, pageWidth, pageHeight,
            TJHomeScenario.PAGE_CORNER_ARC, TJHomeScenario.PAGE_CORNER_ARC);

        // current right page
        g2.setColor(new java.awt.Color(255, 255, 255));
        g2.fillRoundRect(rightPageX, startY, pageWidth, pageHeight,
            TJHomeScenario.PAGE_CORNER_ARC, TJHomeScenario.PAGE_CORNER_ARC);

        // border of current pages
        g2.setColor(TJHomeScenario.PAGE_BORDER_COLOR);
        g2.setStroke(TJHomeScenario.PAGE_BORDER_STROKE);
        g2.drawRoundRect(leftPageX, startY, pageWidth, pageHeight,
            TJHomeScenario.PAGE_CORNER_ARC, TJHomeScenario.PAGE_CORNER_ARC);
        g2.drawRoundRect(rightPageX, startY, pageWidth, pageHeight,
            TJHomeScenario.PAGE_CORNER_ARC, TJHomeScenario.PAGE_CORNER_ARC);
    }
}