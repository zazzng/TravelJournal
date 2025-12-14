package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.geom.Ellipse2D;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import tj.TJ;
import tj.TJCanvas2D;
import tj.scenario.TJDrawScenario;
import tj.scenario.TJHomeScenario;
import tj.scenario.TJImageScenario;
import tj.TJScene; // Import TJScene to handle the return scene correctly
import x.XScene;
import tj.scenario.TJColorScenario;
import tj.scenario.TJEmojiScenario;
import x.XCmdToChangeScene;

public class TJNavPanel {

    public static JPanel createTopNavPanel(TJ tj, String title) {
        return createTopNavPanel(tj, title, null);
    }

    public static JPanel createTopNavPanel(TJ tj, String title, XScene returnScene) {
        int appHeight = tj.getCanvas2D().getHeight();
        if (appHeight == 0) appHeight = 800;
        int topHeight = (int)(appHeight * TJCanvas2D.TOP_NAV_RATIO);
        
        JPanel topNavPanel = new JPanel(new BorderLayout());
        topNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_DARK);
        topNavPanel.setPreferredSize(new Dimension(0, topHeight));
        
        // back button
        JButton backBtn = new JButton("< Back");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        // title label
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        titleLabel.setForeground(Color.WHITE);
        
        // dummy label
        JLabel dummy = new JLabel("       ");
        dummy.setPreferredSize(new Dimension(80, 0));
        
        topNavPanel.add(backBtn, BorderLayout.WEST);
        topNavPanel.add(titleLabel, BorderLayout.CENTER);
        topNavPanel.add(dummy, BorderLayout.EAST);

        backBtn.addActionListener(e -> {
            if (returnScene != null) {
                XCmdToChangeScene.execute(tj, returnScene, null);
            } else {
                XCmdToChangeScene.execute(tj,
                    TJHomeScenario.CatalogueScene.getSingleton(), null);
            }
        });

        return topNavPanel;
    }

    public static JPanel createBottomNavPanel(TJ tj, XScene returnScene) {
        int appHeight = tj.getCanvas2D().getHeight();
        if (appHeight == 0) appHeight = 800;
        int bottomHeight = (int)(appHeight * TJCanvas2D.BOTTOM_NAV_RATIO);
        
        JPanel bottomNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_DARK);
        bottomNavPanel.setPreferredSize(new Dimension(0, bottomHeight));
        bottomNavPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton penBtn = new JButton("Pen");
        JButton colorBtn = new JButton("Color");
        JButton imageBtn = new JButton("Add Image");
        JButton emojiBtn = new JButton("Emoji");

        bottomNavPanel.add(penBtn);
        bottomNavPanel.add(colorBtn);
        bottomNavPanel.add(imageBtn);
        bottomNavPanel.add(emojiBtn);
        
        penBtn.addActionListener(e -> {
            XCmdToChangeScene.execute(tj, 
                TJDrawScenario.DrawReadyScene.getSingleton(), 
                returnScene);
        });

        colorBtn.addActionListener(e -> {
             XCmdToChangeScene.execute(tj,
                TJColorScenario.ColorChangeScene.getSingleton(), returnScene);
        });

        imageBtn.addActionListener(e -> {
            XCmdToChangeScene.execute(tj, 
                TJImageScenario.ImageReadyScene.getSingleton(), 
                returnScene);
        });
        
        emojiBtn.addActionListener(e -> {

            XCmdToChangeScene.execute(tj, TJEmojiScenario.EmojiDrawScene.getSingleton(), returnScene);
        });

        return bottomNavPanel;
    }

    public static JPanel createEmojiBottomNavPanel(TJ tj, XScene returnScene) {
        int appHeight = tj.getCanvas2D().getHeight();
        if (appHeight == 0) appHeight = 800;
        int bottomHeight = (int)(appHeight * TJCanvas2D.BOTTOM_NAV_RATIO);
        
        JPanel bottomNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_DARK);
        bottomNavPanel.setPreferredSize(new Dimension(0, bottomHeight));
        bottomNavPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton penBtn = new JButton("Pen");
        JButton colorBtn = new JButton("Color");
        JButton emojiBtn = new JButton("Emoji");

        bottomNavPanel.add(penBtn);
        bottomNavPanel.add(colorBtn);
        bottomNavPanel.add(emojiBtn);
        
        penBtn.addActionListener(e -> {
            XCmdToChangeScene.execute(tj, 
                TJEmojiScenario.EmojiDrawScene.getSingleton(), 
                returnScene);
        });

        colorBtn.addActionListener(e -> {
             XCmdToChangeScene.execute(tj,
                TJColorScenario.ColorChangeScene.getSingleton(), returnScene);
        });
        
        emojiBtn.addActionListener(e -> {
            XCmdToChangeScene.execute(tj, TJEmojiScenario.EmojiDrawScene.getSingleton(), returnScene);
        });

        return bottomNavPanel;
    }
    
    public static JPanel createEmojiDrawBottomNavPanel(TJ tj, XScene returnScene) {
        int appHeight = tj.getCanvas2D().getHeight();
        if (appHeight == 0) appHeight = 800;
        int bottomHeight = (int)(appHeight * TJCanvas2D.BOTTOM_NAV_RATIO);
        
        JPanel bottomNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_DARK);
        bottomNavPanel.setPreferredSize(new Dimension(0, bottomHeight));
        bottomNavPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton penBtn = new JButton("Pen");
        JButton colorBtn = new JButton("Color");
        JButton exportBtn = new JButton("Export");

        bottomNavPanel.add(penBtn);
        bottomNavPanel.add(colorBtn);
        bottomNavPanel.add(exportBtn);
        
        penBtn.addActionListener(e -> {
            XCmdToChangeScene.execute(tj, 
                TJEmojiScenario.EmojiDrawScene.getSingleton(), 
                returnScene);
        });

        colorBtn.addActionListener(e -> {
            // Open color chooser dialog and stay in current scene
            Color selectedColor = JColorChooser.showDialog(null, "Choose a color", Color.BLACK);
            if (selectedColor != null) {
                tj.getPtCurveMgr().setDefaultColor(selectedColor);
            }
        });
        
        exportBtn.addActionListener(e -> {
            exportEmojiDrawing(tj);
        });

        return bottomNavPanel;
    }
    
    private static void exportEmojiDrawing(TJ tj) {
        try {
            // Get the emoji circle from the current drawing scene
            TJEmojiScenario.EmojiDrawingScene drawingScene = TJEmojiScenario.EmojiDrawingScene.getSingleton();
            Ellipse2D.Double emojiCircle = drawingScene.getEmojiCircle();
            
            if (emojiCircle == null) {
                JOptionPane.showMessageDialog(null, 
                    "Emoji circle not initialized!",
                    "Export Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Emoji Drawing");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "PNG Image (*.png)", "png"));
            
            // Set default file name with timestamp
            String fileName = "emoji_drawing_" + System.currentTimeMillis() + ".png";
            fileChooser.setSelectedFile(new File(fileName));
            
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                
                // Get circle bounds
                int circleX = (int)emojiCircle.getX();
                int circleY = (int)emojiCircle.getY();
                int circleWidth = (int)emojiCircle.getWidth();
                int circleHeight = (int)emojiCircle.getHeight();
                
                // Create buffered image with circle dimensions
                BufferedImage image = new BufferedImage(
                    circleWidth, circleHeight, BufferedImage.TYPE_INT_RGB);
                
                Graphics2D g2 = image.createGraphics();
                
                // Fill background with white
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, circleWidth, circleHeight);
                
                // Create a clipping path - circle
                Ellipse2D.Double clipCircle = new Ellipse2D.Double(
                    0, 0, circleWidth, circleHeight);
                g2.setClip(clipCircle);
                
                // Translate graphics to render only the circle area
                g2.translate(-circleX, -circleY);
                
                // Get canvas and render it
                TJCanvas2D canvas = tj.getCanvas2D();
                canvas.paint(g2);
                
                g2.dispose();
                
                // Write to file
                ImageIO.write(image, "png", selectedFile);
                JOptionPane.showMessageDialog(null, 
                    "Emoji drawing exported successfully!\n" + selectedFile.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error exporting emoji drawing: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public static JPanel createMapTopNavPanel(TJ tj, boolean defaultTitle) {
        int appHeight = tj.getCanvas2D().getHeight();
        if (appHeight == 0) appHeight = 800;
        int topHeight = (int)(appHeight * TJCanvas2D.TOP_NAV_RATIO);
        
        JPanel topNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        topNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_LIGHT);
        topNavPanel.setPreferredSize(new Dimension(0, topHeight));
        
        JLabel title;
        if (defaultTitle) {
            title = new JLabel("User's Travel Journal");
        } else {
            String journalTitle = tj.getJournalBookMgr().getCurBook().getTitle();
            title = new JLabel(journalTitle);
        }
        title.setFont(new Font("SansSerif", Font.PLAIN, 16));
        title.setForeground(new Color(50, 50, 50));
        
        topNavPanel.add(title);
        
        return topNavPanel;
    }
}