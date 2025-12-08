package tj.scenario;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJPage;
import tj.TJScene;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJDefaultScenario extends XScenario {
    // singleton pattern
    private static TJDefaultScenario mSingleton = null;
    public static TJDefaultScenario getSingle() {
        assert(TJDefaultScenario.mSingleton != null);
        return TJDefaultScenario.mSingleton;
    }
    public static TJDefaultScenario createSingleton(XApp app) {
        assert(TJDefaultScenario.mSingleton == null);
        TJDefaultScenario.mSingleton = new TJDefaultScenario(app);
        return TJDefaultScenario.mSingleton;
    }
    private TJDefaultScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJDefaultScenario.ReadyScene.createSingleton(this));
    }

    public static class ReadyScene extends TJScene {
        // UI Components
        private JPanel mTopNavPanel;
        private JButton mBackBtn;
        private JLabel mTitleLabel;
        
        private JPanel mBottomNavPanel;
        private JButton mPenBtn;
        private JButton mColorBtn;
        private JButton mImageBtn;
        private JButton mEmojiBtn;

        // singleton pattern
        private static ReadyScene mSingleton = null;
        public static ReadyScene getSingleton() {
            assert(ReadyScene.mSingleton != null);
            return ReadyScene.mSingleton;
        }
        public static ReadyScene createSingleton(XScenario scenario) {
            assert(ReadyScene.mSingleton == null);
            ReadyScene.mSingleton = new ReadyScene(scenario);
            return ReadyScene.mSingleton;
        }
        private ReadyScene(XScenario scenario) {
            super(scenario);
        }
        
        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            int appHeight = tj.getCanvas2D().getHeight();
            if (appHeight == 0) appHeight = 800; // Fallback
            
            int topHeight = (int)(appHeight * TJCanvas2D.TOP_NAV_RATIO);
            
            mTopNavPanel = new JPanel(new BorderLayout());
            mTopNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_DARK);
            mTopNavPanel.setPreferredSize(new Dimension(0, topHeight));
            
            mBackBtn = new JButton("< Back");
            mBackBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
            mBackBtn.setForeground(Color.WHITE);
            
            mBackBtn.setFocusPainted(false);
            mBackBtn.setContentAreaFilled(false);
            mBackBtn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            
            mTitleLabel = new JLabel("Untitled Page", SwingConstants.CENTER);
            mTitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            mTitleLabel.setForeground(Color.WHITE);
            
            mTopNavPanel.add(mBackBtn, BorderLayout.WEST);
            mTopNavPanel.add(mTitleLabel, BorderLayout.CENTER);
            
            // dummy label to balance the center title
            JLabel dummy = new JLabel("       ");
            dummy.setPreferredSize(new Dimension(80, 0)); 
            mTopNavPanel.add(dummy, BorderLayout.EAST);
            
            mBackBtn.addActionListener(e -> {
                XCmdToChangeScene.execute(tj,
                    TJHomeScenario.CatalogueScene.getSingleton(), null);
            });
        }

        private void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            int appHeight = tj.getCanvas2D().getHeight();
            if (appHeight == 0) appHeight = 800;
            int bottomHeight = (int)(appHeight * TJCanvas2D.BOTTOM_NAV_RATIO);
            
            mBottomNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
            mBottomNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_DARK);
            mBottomNavPanel.setPreferredSize(new Dimension(0, bottomHeight));
            mBottomNavPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            
            mPenBtn = new JButton("Pen");
            mColorBtn = new JButton("Color");
            mImageBtn = new JButton("Add Image");
            mEmojiBtn = new JButton("Emoji");

            mBottomNavPanel.add(mPenBtn);
            mBottomNavPanel.add(mColorBtn);
            mBottomNavPanel.add(mImageBtn);
            mBottomNavPanel.add(mEmojiBtn);
            
            mPenBtn.addActionListener(e -> {
                XCmdToChangeScene.execute(tj, 
                    TJDrawScenario.DrawScene.getSingleton(), 
                    this);
            });

            mColorBtn.addActionListener(e -> {
//                XCmdToChangeScene.execute(tj, 
//                    TJColorScenario.ColorChangeScene.getSingleton(), 
//                    this);
            });

            mImageBtn.addActionListener(e -> {
                
            });
            
            mEmojiBtn.addActionListener(e -> {
//                XCmdToChangeScene.execute(tj, 
//                    TJEmojiScenario.EmojiReadyScene.getSingleton(), 
//                    this);
            });
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            // Calculate dimensions (same logic as HomeScenario for consistency)
            int appWidth = canvas.getWidth();
            int appHeight = canvas.getHeight();
            int pageHeight = (int)(appHeight * 0.85); // Slightly larger for editing view
            int pageWidth = (int)(pageHeight * TJCanvas2D.PAGE_ASPECT_RATIO);
            int startX = (appWidth - pageWidth * 2) / 2;
            int startY = (appHeight - pageHeight) / 2;

            TJPage[] curPage = tj.getPageMgr().getCurPage();
            
            // draw the dark background
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, appWidth, appHeight);

            // 1. Draw Left Page
            g2.setColor(Color.WHITE);
            g2.fillRect(startX, startY, pageWidth, pageHeight);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRect(startX, startY, pageWidth, pageHeight);
            
            // 2. Draw Right Page
            g2.setColor(Color.WHITE);
            g2.fillRect(startX + pageWidth, startY, pageWidth, pageHeight);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRect(startX + pageWidth, startY, pageWidth, pageHeight);

            // 3. Draw Content (if your TJPage has a draw method)
            // curPage[0].draw(g2, startX, startY, pageWidth, pageHeight);
            // curPage[1].draw(g2, startX + pageWidth, startY, pageWidth, pageHeight);

            // 4. Draw The Divider (Light Gray Line)
            g2.setStroke(new BasicStroke(2.0f));
            g2.setColor(new Color(200, 200, 200)); // Light Gray
            g2.drawLine(startX + pageWidth, startY, startX + pageWidth, startY + pageHeight);
        }

        @Override public void handleMousePress(MouseEvent e) {}
        
        @Override public void handleMouseDrag(MouseEvent e) {}
        
        @Override public void handleMouseRelease(MouseEvent e) {}
        
        @Override public void handleKeyDown(KeyEvent e) {}
        
        @Override public void handleKeyUp(KeyEvent e) {}
        
        @Override public void updateSupportObjects() {}
        
        @Override public void renderScreenObjects(Graphics2D g2) {}
        
        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            
            if (this.mTopNavPanel == null) {
                initializeTopNav();
            }
            if (this.mBottomNavPanel == null) {
                initializeBottomNav();
            }
            
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }

    }
}