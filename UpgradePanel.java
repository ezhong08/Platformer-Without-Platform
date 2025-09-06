/*
 * Copyright (c) 2025 Eric Zhong. All rights reserved.
 * Licensed under the MIT License.
*/

/** File name: UpgradePanel.java
 * UpgradePanel is connected to Main and manages the upgrading phase of the game.
 * This includes the buttons and the pop-up descriptions of what the upgrades do that appear when the corresponding button is hovered over.
 * Note that UpgradePanel doesn't manage how these upgrades effect the player's platforming experience.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;

public class UpgradePanel extends JPanel implements ActionListener
{
    private JPanel eastPanel;
    private JPanel centerPanel;
    
    private ArrayList<JButton> upgradeButtons = new ArrayList<JButton>();
    private String[][] upgradeNames = {{"Double Jump", "Triple Jump", "Quadruple Jump", "Quintuple Jump"},
        {"Run", "Sprint"},
        {"High Jump", "Super High Jump"},
        {"Slow Fall", "Glide"},
        {"Horizontal Dash", "Double Dash", "Triple Dash"},
        {"Omnidirectional Dash", "Teleport", "Dash with Invincibility"},
        {"Pogo Jump", "Pogo Release"},
        {"Portable Waypoint"},
        {"Alignment Column", "UFO Beam"}
    };
    private String[][] upgradeDescriptions = {{"You can jump once midair.\nPress z while midair to use this.", "You can jump twice midair.\nPress z while midair to use this.", "You can jump three times midair.\nPress z while midair to use this.", "You can jump four times midair.\nPress z while midair to use this."},
        {"You can optionally run faster.\nHold c while moving left/right to use this.", "You can optionally run much faster.\nHold c while moving left/right to use this."},
        {"You can optionally jump higher.\nHold c while jumping with z to use this.", "You can optionally jump a lot higher.\nHold c while jumping with z to use this."},
        {"You can optionally fall slower.\nHold c while midair to use this.", "You can optionally fall a lot slower.\nHold c while midair to use this."},
        {"You can dash horizontally on the ground or once midair.\nPress space while holding left or right to use this.\nJumping while dashing will cancel the dash.", "You can dash horizontally on the ground or twice midair.\nPress space while holding left or right to use this.\nJumping while dashing will cancel the dash.", "You can dash horizontally on the ground or three times midair this.\nPress space while holding left or right to use.\nJumping while dashing will cancel the dash."},
        {"You can dash on the ground or once midair.\nPress space while giving directional input (arrow keys) to use this.\nJumping while dashing will cancel the dash.", "You can teleport on the ground or once midair.\nPress space while giving directional input (arrow keys) to use this.", "You can dash on the ground or once midair, during which you'll have invincibility.\nPress space while giving directional input (arrow keys) to use this.\nJumping while dashing will cancel the dash."},
        {"You can \"attack\" below yourself.\nHitting an obstacle this way will make you jump.\nHold x and down to use this.", "You retain pogo jump, but can now perform an extra jump when ending a streak of pogo jumps.\nLet go of x while pogo jumping to use this.\nLet go of down while pogo jumping to not use this."},
        {"You can place a waypoint at your current location.\nUsing this ability again will teleport you to that location.\nThis can only be used once per life.\nPress x while not giving vertical directional input (hold both or neither up and down) to use this."},
        {"You can create columns near your x-coordinate that keep you within their bounds.\nUsing this ability again will cancel the columns.\nThis can only be used once per life.\nPress x while holding up to use this.", "You can create columns near your x-coordinate that keep you within their bounds and make you fall slower.\nUsing this ability again will cancel the columns.\nThis can only be used once per life.\nPress x while holding up to use this."}
    };
    private JTextArea descriptionTitle;

    public UpgradePanel()
    {
        setFocusable(true);
        this.setLayout(new BorderLayout());
        
        centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(4, 4));
        centerPanel.setBackground(Color.GRAY);
        setUpButtons();
        
        eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
        eastPanel.setBackground(new Color(164, 116, 73));
        
        // Information about JTextArea was searched from various sources.
        // Note that JTextArea is used in multiple places.
        descriptionTitle = new JTextArea("Upgrade Description:");
        descriptionTitle.setEditable(false);
        // Here, the box layout has 2 parts. The bottom part only seems to appear
        // when the mouse hovers over a button. So in the meantime, the top part
        // must be already brown for eastpanel to appear always brown.
        descriptionTitle.setBackground(new Color(164, 116, 73));
        descriptionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        descriptionTitle.setLineWrap(true);
        descriptionTitle.setWrapStyleWord(true);
        eastPanel.add(descriptionTitle);
        
        JLabel upgradePanelTitle = new JLabel("Choose an Upgrade:");
        upgradePanelTitle.setFont(new Font("Arial", Font.BOLD, 16));
        upgradePanelTitle.setHorizontalAlignment(JLabel.CENTER); // This line was from https://stackoverflow.com/questions/19506769/how-to-center-jlabel-in-jframe-swing
        
        this.add(upgradePanelTitle, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(eastPanel, BorderLayout.EAST);
    }
    
    public void actionPerformed(ActionEvent event)
    {
        int upgradeIndex = Integer.parseInt(event.getActionCommand());
        Main.getUpgrades()[upgradeIndex]++;
        
        setUpButtons();
        eastPanel.removeAll();
        eastPanel.add(descriptionTitle);
        revalidate();
        
        Main.switchToGamePhase();
    }
    
    public void setUpButtons()
    {
        upgradeButtons.clear();
        for (int i = 0; i < Main.getUpgrades().length; i++)
        {
            if (buttonCheck(i))
            {
                // The name will be that of the next upgrade available, upgradeNames[i][Main.getUpgrades()[i]].
                upgradeButtons.add(new JButton(upgradeNames[i][Main.getUpgrades()[i]]));
                upgradeButtons.get(upgradeButtons.size()-1).addActionListener(this);
                upgradeButtons.get(upgradeButtons.size()-1).setActionCommand(String.valueOf(i));
                
                final int tempI = i; // Yes the final is necessary to appease Java.
                // The concept of adding a MouseListener this way comes from https://stackoverflow.com/questions/22638926/how-to-put-hover-effect-on-jbutton
                // However, this particular implementation goes well beyond what the stack overflow template had.
                upgradeButtons.get(upgradeButtons.size()-1).addMouseListener(new MouseListener()
                {
                    public void mouseClicked(MouseEvent e) {
                    }
                
                    public void mousePressed(MouseEvent e) {
                    }
                
                    public void mouseReleased(MouseEvent e) {
                    }
                
                    public void mouseEntered(MouseEvent e) {
                        eastPanel.removeAll();
                        eastPanel.add(descriptionTitle);
                        JTextArea currentUpgradeDescription = new JTextArea(upgradeNames[tempI][Main.getUpgrades()[tempI]] + "\n" + upgradeDescriptions[tempI][Main.getUpgrades()[tempI]]);
                        currentUpgradeDescription.setEditable(false);
                        currentUpgradeDescription.setBackground(new Color(0, 0, 0, 0));
                        currentUpgradeDescription.setLineWrap(true);
                        currentUpgradeDescription.setWrapStyleWord(true);
                        eastPanel.add(currentUpgradeDescription);
                        revalidate();
                    }
                
                    public void mouseExited(MouseEvent e) {
                        eastPanel.removeAll();
                        eastPanel.add(descriptionTitle);
                        revalidate();
                    }
                });
            }
        }
        
        resetButtonDisplay();
    }
    
    public boolean buttonCheck(int upgradeIndex)
    {
        // This basically checks if all upgrades in a path have been gotten (if so, then don't display this path).
        if (upgradeNames[upgradeIndex].length <= Main.getUpgrades()[upgradeIndex])
        {
            return false;
        }
        
        // Special checks for dashes.
        // Can't upgrade the standard path if the special path is upgraded.
        if (upgradeIndex == 4)
        {
            if (Main.getUpgrades()[5] != 0)
            {
                return false;
            }
        }
        // Can't upgrade the special path if the standard path is upgraded beyond the basic dash or not upgraded at all.
        if (upgradeIndex == 5)
        {
            if (Main.getUpgrades()[4] != 1)
            {
                return false;
            }
        }
        
        return true;
    }
    
    public void resetButtonDisplay()
    {
        centerPanel.removeAll();
        for (JButton i : upgradeButtons)
        {
            centerPanel.add(i);
        }
    }
}