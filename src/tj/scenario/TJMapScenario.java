package tj.scenario;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import tj.TJ;
import tj.TJBookMetadata;
import tj.TJCanvas2D;
import tj.TJJournalBook;
import tj.TJJournalBookMgr;
import tj.TJMapPinPoint;
import tj.TJPageMgr;
import tj.TJScene;
import utils.TJNavPanel; // Assuming TJNavPanel exists for navigation setup
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJMapScenario extends XScenario {
    // constants
    private static final String MAP_ASSET_PATH = "/assets/world_map.png";
    private static final double PINPOINT_TOLERANCE = 10.0; // Max click distance for pinpoint selection

    // fields
    private Image mWorldMap = null;
    private Image mScaledMap = null;
    private int mScaledMapHeight = 0;
    private int mScaledMapWidth = 0;
    
    private Point2D.Double mNewPinPoint = null;
    private ArrayList<TJMapPinPoint> mPinPoints = new ArrayList<>(); 
    
    // singleton pattern
    private static TJMapScenario mSingleton = null;
    public static TJMapScenario getSingle() {
        assert(TJMapScenario.mSingleton != null);
        return TJMapScenario.mSingleton;
    }
    public static TJMapScenario createSingleton(XApp app) {
        assert(TJMapScenario.mSingleton == null);
        TJMapScenario.mSingleton = new TJMapScenario(app);
        return TJMapScenario.mSingleton;
    }
    private TJMapScenario(XApp app) {
        super(app);
        loadMap();
    }
    
    private void loadMap() {
        try {
            this.mWorldMap = ImageIO.read(getClass().getResourceAsStream(MAP_ASSET_PATH));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("ERROR: Failed to load world map asset: " + e.getMessage());
        }
    }
    
    @Override
    protected void addScenes() {
        this.addScene(TJMapScenario.MapReadyScene.createSingleton(this));
        this.addScene(TJMapScenario.MapAddJournalScene.createSingleton(this));
        this.addScene(TJMapScenario.MapSelectJournalScene.createSingleton(this));
    }
    
    private void updateMapScale(TJCanvas2D canvas) {
        if (this.mWorldMap == null) return;
        
        int appWidth = canvas.getWidth();
        int originalWidth = this.mWorldMap.getWidth(null);
        int originalHeight = this.mWorldMap.getHeight(null);
        
        if (originalWidth > 0) {
            double scale = (double)appWidth / originalWidth;
            this.mScaledMapWidth = appWidth;
            this.mScaledMapHeight = (int)(originalHeight * scale);
            this.mScaledMap = this.mWorldMap.getScaledInstance(mScaledMapWidth, mScaledMapHeight, Image.SCALE_SMOOTH);
        }
    }
    
    public void syncPinPoints() {
        TJ tj = (TJ)this.getApp();
        TJJournalBookMgr bookMgr = tj.getJournalBookMgr();
        
        mPinPoints.clear();
        
        for (TJBookMetadata metadata : bookMgr.getBookMetadata()) {
            // Note: metadata.pinPoint holds the original coordinate relative to the map (0-1 range or World coordinates)
            TJMapPinPoint pin = new TJMapPinPoint(metadata.pinPoint, metadata.title);
            mPinPoints.add(pin);
        }
    }

    // draw the map
    private void drawMap(Graphics2D g2) {
        TJ tj = (TJ)this.getApp();
        TJCanvas2D canvas = tj.getCanvas2D();
        updateMapScale(canvas);

        if (mScaledMap != null) {
            // center the map vertically if app height is larger than map height
            int startY = (canvas.getHeight() - mScaledMapHeight) / 2;
            g2.drawImage(mScaledMap, 0, startY, null);
            
            // draw all existing pins
            for (TJMapPinPoint pin : mPinPoints) {
                // PinPoint is currently stored in a coordinate system (Map coordinates, e.g., 0 to 1000)
                // We need to transform it to Screen Coordinates (pixels) here.
                // Assuming pin.getMapPoint() returns normalized coordinates (0 to 1) for simplicity:
                
                // Example conversion (if pin.getMapPoint() holds normalized X, Y):
                // double screenX = pin.getMapPoint().getX() * mScaledMapWidth;
                // double screenY = pin.getMapPoint().getY() * mScaledMapHeight + startY;
                
                // For now, let's assume pin.getMapPoint() holds the coordinates we use for display
                // and TJMapPinPoint.draw() knows how to draw itself at that screen location.
                
                // ** CRITICAL TODO: Implement proper MapPoint -> ScreenPoint scaling and transformation **
                // For demonstration, let's just use the stored point (mMapPoint) assuming it represents Screen/Map coordinates after scaling.
                
                pin.draw(g2);
            }
        }
    }

    public static class MapReadyScene extends TJScene {
        // UI components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        private static MapReadyScene mSingleton = null;
        public static MapReadyScene getSingleton() { return mSingleton; }
        public static MapReadyScene createSingleton(XScenario scenario) {
            mSingleton = new MapReadyScene(scenario); return mSingleton;
        }
        private MapReadyScene(XScenario scenario) { super(scenario); }

        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mTopNavPanel = TJNavPanel.createMapTopNavPanel(tj);
        }
        
        private void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            int appHeight = tj.getCanvas2D().getHeight();
            if (appHeight == 0) appHeight = 800;
            int bottomHeight = (int)(appHeight * TJCanvas2D.BOTTOM_NAV_RATIO);
            
            // create panel and add buttons
            mBottomNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
            mBottomNavPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            mBottomNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_LIGHT);
            mBottomNavPanel.setPreferredSize(new Dimension(0, bottomHeight));
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJMapScenario scenario = (TJMapScenario)this.mScenario;
            Point pt = e.getPoint();
            
            // 1. Check for existing pinpoint click (Select Journal)
            for (TJMapPinPoint pin : scenario.mPinPoints) {
                if (pin.contains(pt)) {
                    // Store the selected book's title/metadata and switch scene
                    // NOTE: Need to find the book index based on title here if needed, 
                    // or just pass the title and handle load in the next scene.
                    
                    // For now, let's store the title and jump to select scene
                    
                    // We need a mechanism to store the selected PinPoint/Book
                    // For simplicity, let's assume we find and select the book in the manager:
                    TJJournalBookMgr bookMgr = tj.getJournalBookMgr();
                    
                    // If the pin's stored point is its exact screen location, 
                    // we can use it to find the book in the manager.
                    // Since the pin points are based on metadata, we look up the title.
                    // This requires iterating through metadata, which is complex.
                    
                    // Let's rely on the pin's title being unique:
                    // For now, we skip the intermediate scene and assume MapSelectJournalScene is next.
//                    XCmdToChangeScene.execute(tj,
//                        TJMapScenario.MapSelectJournalScene.getSingleton(), pin.getBookTitle());
//                    return;
                }
            }
            
            // 2. Click on empty space (Add Journal)
            scenario.mNewPinPoint = new Point2D.Double(pt.getX(), pt.getY());
            XCmdToChangeScene.execute(tj,
                TJMapScenario.MapAddJournalScene.getSingleton(), this);
        }
        
        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJMapScenario scenario = (TJMapScenario)this.mScenario;
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_LIGHT);
            g2.fillRect(0, 0, g2.getClipBounds().width, g2.getClipBounds().height);
            
            scenario.drawMap(g2);
        }
        
        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            
            if (this.mTopNavPanel == null) initializeTopNav();
            if (this.mBottomNavPanel == null) initializeBottomNav();
            
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
            
            // Sync pins every time we enter the map scene
            ((TJMapScenario)this.mScenario).syncPinPoints();
        }

        @Override public void handleMouseDrag(MouseEvent e) {}
        @Override public void handleMouseRelease(MouseEvent e) {}
        @Override public void handleKeyDown(KeyEvent e) {}
        @Override public void handleKeyUp(KeyEvent e) {}
        @Override public void updateSupportObjects() {}
        @Override public void renderScreenObjects(Graphics2D g2) {}
        @Override public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }

        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_LIGHT); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    public static class MapAddJournalScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        private JButton mCancelBtn;
        private JButton mCreateBtn;
        private String mNewTitle = "New Journal"; // placeholder for input
        
        private static MapAddJournalScene mSingleton = null;
        public static MapAddJournalScene getSingleton() { return mSingleton; }
        public static MapAddJournalScene createSingleton(XScenario scenario) {
            mSingleton = new MapAddJournalScene(scenario); return mSingleton;
        }
        private MapAddJournalScene(XScenario scenario) { super(scenario); }

        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mTopNavPanel = TJNavPanel.createMapTopNavPanel(tj);
        }
        
        private void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            int appHeight = tj.getCanvas2D().getHeight();
            int bottomHeight = (int)(appHeight * TJCanvas2D.BOTTOM_NAV_RATIO);
            
            mBottomNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
            mBottomNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_LIGHT);
            mBottomNavPanel.setPreferredSize(new Dimension(0, bottomHeight));
            mBottomNavPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            
            mCancelBtn = new JButton("Cancel");
            mCreateBtn = new JButton("Create Journal");
            
            mBottomNavPanel.add(mCancelBtn);
            mBottomNavPanel.add(mCreateBtn);
            
            mCancelBtn.addActionListener(e -> {
                ((TJMapScenario)this.mScenario).mNewPinPoint = null;
                XCmdToChangeScene.execute(tj, this.mReturnScene, null);
            });

            mCreateBtn.addActionListener(e -> {
                TJMapScenario scenario = (TJMapScenario)this.mScenario;
                if (scenario.mNewPinPoint != null) {
                    
                    // get title from user (e.g., via JOptionPane or a dialog)
                    String title = JOptionPane.showInputDialog(tj.getCanvas2D(),
                        "Enter Journal Title:", mNewTitle);
                    if (title == null || title.trim().isEmpty()) {
                        title = mNewTitle;
                    }
                    
                    // add to manager and save content
                    tj.getJournalBookMgr().addNewBook(title, scenario.mNewPinPoint);
                    
                    scenario.mNewPinPoint = null;
                    XCmdToChangeScene.execute(tj,
                        TJDrawScenario.DrawReadyScene.getSingleton(), null);
                }
            });
        }

        @Override public void handleMousePress(MouseEvent e) {}
        
        @Override public void handleMouseDrag(MouseEvent e) {}
        
        @Override public void handleMouseRelease(MouseEvent e) {}
        
        @Override public void handleKeyDown(KeyEvent e) {}
        
        @Override public void handleKeyUp(KeyEvent e) {}
        
        @Override public void updateSupportObjects() {}
        
        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJMapScenario scenario = (TJMapScenario)this.mScenario;
            scenario.drawMap(g2);
            
            // draw the pinpoint
            if (scenario.mNewPinPoint != null) {
                TJMapPinPoint tempPin = new TJMapPinPoint(scenario.mNewPinPoint, "");
                tempPin.draw(g2);
            }
        }
        
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
            ((TJMapScenario)this.mScenario).syncPinPoints();
        }
        
        @Override public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }

        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_LIGHT); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }
    
    public static class MapSelectJournalScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        private JButton mOpenBtn;
        private JButton mDeleteBtn;
        private String mSelectedBookTitle;
        
        private static MapSelectJournalScene mSingleton = null;
        public static MapSelectJournalScene getSingleton() { return mSingleton; }
        public static MapSelectJournalScene createSingleton(XScenario scenario) {
            mSingleton = new MapSelectJournalScene(scenario); return mSingleton;
        }
        private MapSelectJournalScene(XScenario scenario) { super(scenario); }

        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            String title = "User's Travel Journal";
            
            this.mTopNavPanel = TJNavPanel.createTopNavPanel(tj, title);
        }
        
        private void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            int appHeight = tj.getCanvas2D().getHeight();
            int bottomHeight = (int)(appHeight * TJCanvas2D.BOTTOM_NAV_RATIO);
            
            mBottomNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
            mBottomNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_DARK);
            mBottomNavPanel.setPreferredSize(new Dimension(0, bottomHeight));
            
            mOpenBtn = new JButton("Open Journal");
            mDeleteBtn = new JButton("Delete Journal");
            mBottomNavPanel.add(mOpenBtn);
            mBottomNavPanel.add(mDeleteBtn);

            mOpenBtn.addActionListener(e -> {
                // Find and select the book based on the title captured in getReady
                // and jump to the editing scene (DefaultScenario)
                TJJournalBookMgr bookMgr = tj.getJournalBookMgr();
                
                // This is slightly complex: we need to find the book index or pinpoint
                // in the metadata list using mSelectedBookTitle, then load and switch.
                
                // For demonstration, let's simplify: 
                // We'll rely on the existing find/select logic using the pinpoint 
                // which is not ideal, or just iterate metadata to find index.
                
                int indexToLoad = -1;
                for (int i = 0; i < bookMgr.getBookMetadata().size(); i++) {
                    if (bookMgr.getBookMetadata().get(i).title.equals(mSelectedBookTitle)) {
                        indexToLoad = i;
                        break;
                    }
                }
                
                if (indexToLoad != -1) {
                    bookMgr.selectBook(indexToLoad); // Selects and loads content
                    XCmdToChangeScene.execute(tj, TJDefaultScenario.ReadyScene.getSingleton(), null);
                }
            });
            
            mDeleteBtn.addActionListener(e -> {
                // TODO: Implement deletion logic (remove metadata, delete file)
                XCmdToChangeScene.execute(tj, TJMapScenario.MapReadyScene.getSingleton(), null);
            });
        }
        
        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            if (this.mTopNavPanel == null) initializeTopNav();
            if (this.mBottomNavPanel == null) initializeBottomNav();
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
            
            // The return object from the previous scene (MapReadyScene) is the title
            if (this.mReturnObject instanceof String) {
                mSelectedBookTitle = (String)this.mReturnObject;
            }
            
            // Update title bar to show selected book title
            JLabel titleLabel = (JLabel)((BorderLayout)mTopNavPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (titleLabel != null) {
                titleLabel.setText(mSelectedBookTitle);
            }
            
            ((TJMapScenario)this.mScenario).syncPinPoints();
        }
        
        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJMapScenario scenario = (TJMapScenario)this.mScenario;
            scenario.drawMap(g2);
        }

        // ... (other empty methods)
        @Override public void handleMousePress(MouseEvent e) {}
        @Override public void handleMouseDrag(MouseEvent e) {}
        @Override public void handleMouseRelease(MouseEvent e) {}
        @Override public void handleKeyDown(KeyEvent e) {}
        @Override public void handleKeyUp(KeyEvent e) {}
        @Override public void updateSupportObjects() {}
        @Override public void renderScreenObjects(Graphics2D g2) {}
        @Override public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }

        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_LIGHT); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }
}