/*
 * Copyright (c) 2025 Eric Zhong. All rights reserved.
 * Licensed under the MIT License.
*/

/** File name: Main.java
 * The Main class is where the program begins with the main method. The Main class has the frame in which PlayerPanel and UpgradePanel lie.
 * Note that PlayerPanel and UpgradePanel are in a CardLayout. This allows for switching between the two JPanels based on the phase of the game.
 * Main is also connected to KeyInputs. More on that in KeyInputs. But in general, Main is where variables that are used in all phases of the game exist in.
 * In this way, Main connects the platforming side of the game and the upgrade side of the game.
 * Note that this game is a platformer in which the player controls a blue square to avoid red obstacles and touch a green goal in each level.
 * Players must reach level 30 (something that's managed in PlayerPanel and GameObjects).
 * Additionally, every 2 levels, the player is able to select an upgrade to their abilities. To match these upgrades, levels get progressively harder.
 * General note: Often times, the methods in this project are organized in a way such that methods are underneath the methods that call them.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;

public class Main {
    private static int gameWidth = 412;
    private static int gameHeight = 437;
    private static JFrame frame;
    private static JPanel mainPanel;
    private static CardLayout cardLayout;
    private static PlayerPanel playerPanel;
    private static KeyInputs keyInputs;
    private static UpgradePanel upgradePanel;
    private static int[] upgrades = new int[9];
    /*
    Index 0 upgrade: Midair jumps.
    Index 1 upgrade: Speed boosts.
    Index 2 upgrade: Jump boosts.
    Index 3 upgrade: Slow falls.
    Index 4 upgrade: Dashes.
    Index 5 upgrade: Dashes part 2.
    Index 6 upgrade: Pogo Jumps.
    Index 7 upgrade: Portable Waypoint.
    Index 8 upgrade: Alignment Columns.
    */
    private static int level = 1;
    private static int gamePhase = 0; // 0 for start screen, 1 for playing screen, 2 for upgrade screen.
    
    public static void main(String[] args) {
        // The following setup of a JFrame was copied from the format used in various Unit 13 - Graphics assignments.
        // Technically, the one here is a slightly modified version of the one in Your Moving Ball.
        frame = new JFrame("A Platformer Without Platforms");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.setSize(gameWidth, gameHeight);
        frame.setLocationRelativeTo(null);
        
        playerPanel = new PlayerPanel(gameWidth, gameHeight);

        frame.setFocusable(true);
        keyInputs = new KeyInputs();
        frame.addKeyListener(keyInputs);
        
        upgradePanel = new UpgradePanel();

        // CardLayout allows for switching between playerPanel and upgradePanel.
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        mainPanel.add(playerPanel, "p");
        mainPanel.add(upgradePanel, "u");
        cardLayout.show(mainPanel, "p");
        
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
    
    public static void startGame()
    {
        gamePhase = 1;
        level = 1;
    }
    
    public static void switchToGamePhase()
    {
        cardLayout.show(mainPanel, "p");
        // Fun fact: without focus, the keylistener fails to work
        // after a cardlayout jpanel switch.
        // Also this has to be after the panel is shown.
        playerPanel.resetLevel();
        gamePhase = 1;
    }
    
    public static void switchToUpgradePhase()
    {
        cardLayout.show(mainPanel, "u");
        gamePhase = 2;
    }
    
    public static void nextLevel()
    {
        level++;
        if (level % 2 == 1)
        {
            switchToUpgradePhase();
        }
        else
        {
            playerPanel.resetLevel();
        }
    }
    
    public static int[] getUpgrades()
    {
        return upgrades;
    }
    
    public static int getLevel()
    {
        return level;
    }
    
    public static int getGamePhase()
    {
        return gamePhase;
    }
    
    public static boolean getIfPressed(String key)
    {
        return keyInputs.getIfPressed(key);
    }
}

/* This was my original plan for the game.
Plans for upgrades:
Optional boosts triggered by pressing c:
Baseline -> Run -> Sprint.
Baseline -> High Jump -> Super High Jump.
Baseline -> Slow Fall -> Glide.

The standard pressing up when midair:
Baseline -> Double Jump -> Triple Jump -> Etc.

Press space for burst movement:
Baseline -> Dash (horizontal by default)
            Dash -> Omnidirectional Dash -> Teleport -> Dash with Invincibility.
            Dash -> Double Dash -> Triple Dash -> Etc.

Press x to use something seemingly external:
While pressing down:
Baseline -> Pogo Jump.
While pressing up:
Baseline -> Grenade. (Quick note: Ideally its usage should be recovered when midair if it doesn't work.)
While not pressing up or down:
Baseline -> Portable Waypoint.
Baseline -> Grappling Hook somehow if possible.
*/