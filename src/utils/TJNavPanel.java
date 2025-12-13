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
import tj.scenario.TJHomeScenario;
import tj.scenario.TJImageScenario;
import tj.TJScene; // Import TJScene to handle the return scene correctly
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
            // Placeholder if TJColorScenario is not yet implemented:
            // XCmdToChangeScene.execute(tj, TJColorScenario.ColorChangeScene.getSingleton(), returnScene);
        });

        imageBtn.addActionListener(e -> {
            XCmdToChangeScene.execute(tj, 
                TJImageScenario.ImageReadyScene.getSingleton(), 
                returnScene);
        });
        
        emojiBtn.addActionListener(e -> {
            // XCmdToChangeScene.execute(tj, TJEmojiScenario.EmojiReadyScene.getSingleton(), returnScene);
        });

        return bottomNavPanel;
    }
    
    public static JPanel createMapTopNavPanel(TJ tj) {
        int appHeight = tj.getCanvas2D().getHeight();
        if (appHeight == 0) appHeight = 800;
        int topHeight = (int)(appHeight * TJCanvas2D.TOP_NAV_RATIO);
        
        JPanel topNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        topNavPanel.setBackground(TJCanvas2D.COLOR_PANEL_BACKGROUND_LIGHT);
        topNavPanel.setPreferredSize(new Dimension(0, topHeight));
        
        JLabel title = new JLabel("User's Travel Journal");
        title.setFont(new Font("SansSerif", Font.PLAIN, 16));
        title.setForeground(new Color(50, 50, 50));
        
        topNavPanel.add(title);
        
        return topNavPanel;
    }
}