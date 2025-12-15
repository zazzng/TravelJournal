package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import tj.TJ;
import tj.TJCanvas2D;
import tj.scenario.TJDrawScenario;
import tj.scenario.TJEmojiScenario;
import tj.scenario.TJEmojiSelectScenario;
import tj.scenario.TJHomeScenario;
import tj.scenario.TJImageScenario;
import tj.TJScene; // Import TJScene to handle the return scene correctly
import tj.scenario.TJColorScenario;
import x.XCmdToChangeScene;

public class TJNavPanel {

    public static JPanel createTopNavPanel(TJ tj, String title) {
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
            XCmdToChangeScene.execute(tj,
                TJHomeScenario.CatalogueScene.getSingleton(), null);
        });

        return topNavPanel;
    }

    public static JPanel createBottomNavPanel(TJ tj, TJScene returnScene) {
        int appHeight = tj.getCanvas2D().getHeight();
        if (appHeight == 0) appHeight = 800;
        int bottomHeight = (int)(appHeight * TJCanvas2D.BOTTOM_NAV_RATIO);
        
        JPanel bottomNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_DARK);
        bottomNavPanel.setPreferredSize(new Dimension(0, bottomHeight));
        bottomNavPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton penBtn = new JButton("Pen");
        JButton colorBtn = new JButton("Color");
        JButton faceColorBtn = new JButton("Face Color");
        JButton imageBtn = new JButton("Add Image");
        JButton emojiBtn = new JButton("Emoji");
        JButton exportBtn = new JButton("Export");

        // Don't add image button in emoji scenario
        if (!(returnScene instanceof TJEmojiScenario.EmojiDrawScene) && 
            !(returnScene instanceof TJEmojiScenario.EmojiDrawingScene) &&
            !(returnScene instanceof TJEmojiSelectScenario.EmojiSelectReadyScene) &&
            !(returnScene instanceof TJEmojiSelectScenario.EmojiSelectScene) &&
            !(returnScene instanceof TJEmojiSelectScenario.EmojiSelectedReadyScene)) {
            bottomNavPanel.add(imageBtn);
        }
        
        // Add face color and export buttons in all emoji scenarios (drawing and selection)
        if (returnScene instanceof TJEmojiScenario.EmojiDrawScene || 
            returnScene instanceof TJEmojiScenario.EmojiDrawingScene ||
            returnScene instanceof TJEmojiSelectScenario.EmojiSelectReadyScene ||
            returnScene instanceof TJEmojiSelectScenario.EmojiSelectScene ||
            returnScene instanceof TJEmojiSelectScenario.EmojiSelectedReadyScene) {
            bottomNavPanel.add(faceColorBtn);
            bottomNavPanel.add(exportBtn);
        }

        bottomNavPanel.add(penBtn);
        bottomNavPanel.add(colorBtn);
        
        // Add emoji button only when NOT in emoji scenario, otherwise it becomes "Back to Draw"
        if (!(returnScene instanceof TJEmojiScenario.EmojiDrawScene) && 
            !(returnScene instanceof TJEmojiScenario.EmojiDrawingScene)) {
            bottomNavPanel.add(emojiBtn);
        } else {
            // In emoji scenario, show "Back to Draw" button
            bottomNavPanel.add(emojiBtn);
        }
        
        penBtn.addActionListener(e -> {
            // If currently in emoji scenario, go to EmojiDrawingScene
            if (returnScene instanceof TJEmojiScenario.EmojiDrawScene) {
                // Set the emoji page as target for drawing
                TJEmojiScenario scenario = TJEmojiScenario.getSingle();
                scenario.setTargetEmojiPage(tj.getJournalBookMgr().getCurEmojiPage());
                XCmdToChangeScene.execute(tj, 
                    TJEmojiScenario.EmojiDrawingScene.getSingleton(), 
                    returnScene);
            } else {
                // Otherwise go to draw scenario
                XCmdToChangeScene.execute(tj, 
                    TJDrawScenario.DrawReadyScene.getSingleton(), 
                    returnScene);
            }
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
        
        // Replace emoji button with back-to-draw button when in emoji scenario (drawing or selection)
        if (returnScene instanceof TJEmojiScenario.EmojiDrawScene || 
            returnScene instanceof TJEmojiScenario.EmojiDrawingScene ||
            returnScene instanceof TJEmojiSelectScenario.EmojiSelectReadyScene ||
            returnScene instanceof TJEmojiSelectScenario.EmojiSelectScene ||
            returnScene instanceof TJEmojiSelectScenario.EmojiSelectedReadyScene) {
            emojiBtn.setText("Back to Draw");
            emojiBtn.addActionListener(e -> {
                XCmdToChangeScene.execute(tj, TJDrawScenario.DrawReadyScene.getSingleton(), null);
            });
        } else {
            emojiBtn.addActionListener(e -> {
                XCmdToChangeScene.execute(tj, TJEmojiScenario.EmojiDrawScene.getSingleton(), returnScene);
            });
        }
        
        faceColorBtn.addActionListener(e -> {
            // Open face color picker for emoji circle
            if (returnScene instanceof TJEmojiScenario.EmojiDrawScene ||
                returnScene instanceof TJEmojiScenario.EmojiDrawingScene ||
                returnScene instanceof TJEmojiSelectScenario.EmojiSelectReadyScene ||
                returnScene instanceof TJEmojiSelectScenario.EmojiSelectScene ||
                returnScene instanceof TJEmojiSelectScenario.EmojiSelectedReadyScene) {
                TJEmojiScenario.openFaceColorPicker(tj, returnScene);
            }
        });
        
        exportBtn.addActionListener(e -> {
            // Export emoji circle drawing
            if (returnScene instanceof TJEmojiScenario.EmojiDrawScene ||
                returnScene instanceof TJEmojiScenario.EmojiDrawingScene ||
                returnScene instanceof TJEmojiSelectScenario.EmojiSelectReadyScene ||
                returnScene instanceof TJEmojiSelectScenario.EmojiSelectScene ||
                returnScene instanceof TJEmojiSelectScenario.EmojiSelectedReadyScene) {
                TJEmojiScenario.exportEmojiCircleDrawing(tj);
            }
        });

        return bottomNavPanel;
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