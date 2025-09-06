/*
 * Copyright (c) 2025 Eric Zhong. All rights reserved.
 * Licensed under the MIT License.
*/

/** File name: PlayerPanel.java
 * The PlayerPanel is what manages the movement of the player, with all of their abilities, in this platformer.
 * A PlayerPanel object is created by Main, and PlayerPanel works together with GameObjects, KeyInputs, and SpecialEffects to do its job.
 * Note that some parts of resetting a level is managed here too.
 * The PlayerPanel also manages the start screen and win screen.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;

public class PlayerPanel extends JPanel implements ActionListener
{
    // Warning: the game doesn't 100% scale with window size (or player size for that matter).
    private int gameWidth;
    private int gameHeight;
    private long startTime;
    private long endTime;
    private boolean gameWon = false;
    
    private int playerX = 25;
    private int playerY = 384;
    private int playerSize = 16;
    private double playerXV = 0;
    private double playerYV = 0;
    private Rectangle playerRect = new Rectangle(25, 384, 16, 16); // This rectangle (and some others) are used for collision detection.
    
    private boolean onPlatform = false;
    private int currentMidairJumps = 0;
    private int currentMidairDashes = 0;
    private boolean jumpHeld = false;
    
    private GameObjects gameObjects; // The red obstacles and the green goal are done in GameObjects.
    private boolean resettingLevel = false; // To deal with a bug where resetting and having the goal go to the player location counted as a level complete.
    private boolean resetHeld = false;
    
    private boolean dashHeld = false;
    private double[] dashingData = new double[3]; // The indices are for: x velocity, y velocity, step()s left.
    
    private boolean attackHeld = false;
    private Rectangle pogoHitbox = new Rectangle(30, 400, 6, 12);
    private boolean pogoing = false;
    private boolean pogoHit = false;
    private int teleportPhase = 0; // Phase 0: initial state, nothing happens. Phase 1: The thing happens. Phase 2: The thing is over, nothing happens.
    private Rectangle waypoint;
    private int alignmentColumnPhase = 0; // See right above for the phase description (it's the same here).
    private int leftAlignmentColumnX;
    private int rightAlignmentColumnX;
    
    private Font titleFont = new Font(Font.SERIF, Font.BOLD, 32);
    private Font secondaryFont = new Font(Font.DIALOG, Font.PLAIN, 24);
    private Font instructionFont = new Font(Font.DIALOG, Font.PLAIN, 14);
    private boolean instructions = false;
    
    private ArrayList<SpecialEffects> specialEffects = new ArrayList<SpecialEffects>();

    public PlayerPanel(int theGameWidth, int theGameHeight)
    {
        // Background, focusable, key listener, mouse listener, and timer originally copied from my version of "Your Moving Ball".
        // Since then some of those have been deleted.
        // Note that I added a lot to the starter code of that assignment before it was copied.
        gameWidth = theGameWidth;
        gameHeight = theGameHeight;
        setBackground(Color.GRAY);

        Timer timer = new Timer(1000/60, this);
        timer.start();
        
        gameObjects = new GameObjects();
    }
    
    public void resetLevel()
    {
        resettingLevel = true;
        resetPlayer();
        gameObjects.generateLevel();
        specialEffects.clear();
        resettingLevel = false;
    }
    
    public void resetPlayer()
    {
        playerX = 25;
        playerY = 384;
        playerRect.setLocation(playerX, playerY); // Dealing with the bug related to level completion upon player reset.
        playerXV = 0;
        playerYV = 0;
        onPlatform = true;
        currentMidairJumps = 0;
        currentMidairDashes = 0;
        dashingData = new double[3];
        pogoing = false;
        pogoHit = false;
        teleportPhase = 0;
        alignmentColumnPhase = 0;
    }
    
    public void actionPerformed(ActionEvent e)
    {
        // As stated in Main, 0 for start screen, 1 for playing screen, 2 for upgrade screen.
        if (Main.getGamePhase() == 0)
        {
            if (Main.getIfPressed("p") && !instructions)
            {
                startTime = System.currentTimeMillis();
                Main.startGame();
            }
            if (Main.getIfPressed("d") && !instructions)
            {
                instructions = true;
            }
            if (Main.getIfPressed("b") && instructions)
            {
                instructions = false;
            }
        }
        else if (Main.getGamePhase() == 1)
        {
            step();
        }
        else if (Main.getGamePhase() == 2)
        {
        }
        
        repaint();
    }
    
    public void step()
    {
        // Keys with these booleans generally aren't meant to work while held, only when pressed.
        // These booleans work with the "__Held" versions of themselves to achieve this.
        boolean jumpPressed = false;
        boolean resetPressed = false;
        boolean dashPressed = false;
        boolean attackPressed = false;
        
        // These variables are for the upgrades related to pressing "c".
        double XVmultiplier = 1;
        double upYVmultiplier = 1;
        double gravityYVmultiplier = 1;
        
        // These variables find the net tilt from the arrow keys (used for "directional input").
        int horizontalTilt = 0;
        int verticalTilt = 0;
        
        if (Main.getIfPressed("c"))
        {
            XVmultiplier = 1 + 0.5 * Main.getUpgrades()[1];
            upYVmultiplier = 1 + 0.2 * Main.getUpgrades()[2];
            gravityYVmultiplier = 1 - 0.2 * Main.getUpgrades()[3];
        }
        
        if (Main.getIfPressed("right"))
        {
            playerXV += 0.4 * XVmultiplier;
            horizontalTilt++;
        }
        if (Main.getIfPressed("left"))
        {
            playerXV -= 0.4 * XVmultiplier;
            horizontalTilt--;
        }
        if (Main.getIfPressed("z"))
        {
            jumpPressed = true;
            if (!jumpHeld)
            {
                dashingData[2] = 0; // Dashes can be canceled by jumps (not necessarily pogo jumps though).
                if (onPlatform)
                {
                    playerYV = -6.0 * upYVmultiplier;
                    specialEffects.add(new SpecialEffects(0, playerX, playerY, new Color(224, 255, 255)));
                }
                else if (currentMidairJumps < Main.getUpgrades()[0])
                {
                    playerYV = -6.0 * upYVmultiplier;
                    currentMidairJumps++;
                    specialEffects.add(new SpecialEffects(0, playerX, playerY, new Color(224, 255, 255)));
                }
            }
        }
        if (Main.getIfPressed("up"))
        {
            verticalTilt++;
        }
        if (Main.getIfPressed("down"))
        {
            verticalTilt--;
        }
        
        // Level reset.
        if (Main.getIfPressed("r"))
        {
            resetPressed = true;
            if (!resetHeld)
            {
                resetLevel();
                resetHeld = resetPressed; // There's a need for a copy of this here to make sure things are updating in BOTH cases (pressed and not pressed).
                return;
            }
        }
        
        // Pogo Jump (key x) that uses down tilt.
        // Note: I never said that pogo jumps cancel dashes, only that jumps cancel dashes.
        if (Main.getIfPressed("x"))
        {
            attackPressed = true;
            if (Main.getUpgrades()[6] > 0 && verticalTilt == -1)
            {
                pogoing = true;
                // The rest of pogo jump is dealt in the paintComponent method.
            }
            else
            {
                pogoing = false;
                pogoHit = false;
            }
        }
        else
        {
            // Pogo Release upgrade.
            if (Main.getUpgrades()[6] > 1 && pogoing && pogoHit)
            {
                playerYV = -6.0;
            }
            
            pogoing = false;
            pogoHit = false;
        }
        
        // Portable Waypoint (key x) that uses neutral tilt (both or neither up and down).
        if (Main.getIfPressed("x"))
        {
            attackPressed = true;
            if (!attackHeld && Main.getUpgrades()[7] > 0 && verticalTilt == 0)
            {
                if (teleportPhase == 0)
                {
                    waypoint = new Rectangle(playerX, playerY, playerSize, playerSize);
                    teleportPhase++;
                    // Some stuff for this ability is done in the paintComponent method.
                }
                else if (teleportPhase == 1)
                {
                    specialEffects.add(new SpecialEffects(2, playerX, playerY, new Color(96, 80, 242)));
                    playerX = waypoint.x;
                    playerY = waypoint.y;
                    specialEffects.add(new SpecialEffects(2, playerX, playerY, new Color(96, 80, 242)));
                    playerXV = 0;
                    playerYV = 0;
                    teleportPhase++;
                }
            }
        }
        
        // Alignment Column (key x) that uses up tilt.
        if (Main.getIfPressed("x"))
        {
            attackPressed = true;
            if (!attackHeld && Main.getUpgrades()[8] > 0 && verticalTilt == 1)
            {
                if (alignmentColumnPhase == 0)
                {
                    leftAlignmentColumnX = playerX + (playerSize / 2) - 16;
                    rightAlignmentColumnX = playerX + (playerSize / 2) + 16;
                    alignmentColumnPhase++;
                    // With this starting point, some work is done in the paintComponent method.
                }
                else if (alignmentColumnPhase == 1)
                {
                    alignmentColumnPhase++;
                }
            }
        }
        if (Main.getUpgrades()[8] > 1 && alignmentColumnPhase == 1) // UFO Beam upgrade to Alignment Column.
        {
            gravityYVmultiplier -= 0.4;
        }
        
        // Various dashes (space bar) that are based on tilt.
        if (Main.getIfPressed("space") && Main.getUpgrades()[4] > 0 && (onPlatform || currentMidairDashes < Main.getUpgrades()[4]))
        {
            dashPressed = true;
            if (!dashHeld && Main.getUpgrades()[5] == 0 && horizontalTilt != 0)
            {
                // Horizontal dashes.
                if (!onPlatform)
                {
                    currentMidairDashes++;
                }
                dashingData[0] = 8.0 * horizontalTilt;
                dashingData[1] = 0.0;
                dashingData[2] = 12;
            }
            else if (!dashHeld && Main.getUpgrades()[5] > 0 && (horizontalTilt != 0 || verticalTilt != 0))
            {
                // Omnidirectional dashes (indices 1 and 3) and teleports (index 2).
                if (!onPlatform)
                {
                    currentMidairDashes++;
                }
                
                if (Main.getUpgrades()[5] != 2)
                {
                    if (horizontalTilt != 0 && verticalTilt != 0)
                    {
                        dashingData[0] = 8.0 / Math.sqrt(2) * horizontalTilt;
                        dashingData[1] = -8.0 / Math.sqrt(2) * verticalTilt;
                        dashingData[2] = 12;
                    }
                    else
                    {
                        dashingData[0] = 8.0 * horizontalTilt;
                        dashingData[1] = -8.0 * verticalTilt;
                        dashingData[2] = 12;
                    }
                    // Invincibility while dashing is implemented at the collision detection code in the paintComponent method.
                }
                else
                {
                    specialEffects.add(new SpecialEffects(2, playerX, playerY, new Color(96, 80, 242)));
                    if (horizontalTilt != 0 && verticalTilt != 0)
                    {
                        playerX += (int)(96.0 / Math.sqrt(2) * horizontalTilt);
                        playerY += (int)(-96.0 / Math.sqrt(2) * verticalTilt);
                    }
                    else
                    {
                        playerX += 96 * horizontalTilt;
                        playerY += -96 * verticalTilt;
                    }
                    specialEffects.add(new SpecialEffects(2, playerX, playerY, new Color(96, 80, 242)));
                    playerXV = 0;
                    playerYV = 0;
                }
            }
        }
        
        // Reminder: Keys with these booleans generally aren't meant to work while held, only when pressed.
        jumpHeld = jumpPressed;
        resetHeld = resetPressed;
        dashHeld = dashPressed;
        attackHeld = attackPressed;
        
        // Dashes override typical movement. Some of those details are done here.
        if (dashingData[2] > 0)
        {
            playerXV = dashingData[0];
            playerYV = dashingData[1];
            dashingData[2]--;
            if (dashingData[2] % 4 == 0)
            {
                specialEffects.add(new SpecialEffects(1, playerX, playerY, new Color(195 + 5 * (int)(dashingData[2]), 195 + 5 * (int)(dashingData[2]), 195 + 5 * (int)(dashingData[2]))));
            }
            // A nerf to the end speed from vertical dashes.
            if (dashingData[2] == 0)
            {
                playerYV /= 2;
            }
        }
        
        // Player acceleration and friction.
        playerXV *= 0.85;
        playerX += (int)(playerXV);
        playerY += (int)(playerYV);
        
        // Code to prevent the player from escaping the alignment columns.
        if (alignmentColumnPhase == 1)
        {
            playerX = Math.max(playerX, leftAlignmentColumnX);
            playerX = Math.min(playerX, rightAlignmentColumnX - playerSize);
        }
        
        // Adjusting player coordinates based on screen edge.
        if (playerX <= 0)
        {
            playerX = 0;
        }
        if (playerY <= 0)
        {
            playerY = 0;
        }
        // It appears that the game's dimensions start at 0 initially, so there's a check for that.
        if (getWidth() != 0 && playerX >= getWidth() - playerSize)
        {
            playerX = getWidth() - playerSize;
        }
        if (getHeight() != 0 && playerY >= getHeight() - playerSize)
        {
            playerY = getHeight() - playerSize;
            // Note: If more platforms are added, this onPlatform stuff needs to be moved.
            onPlatform = true;
            currentMidairJumps = 0;
            currentMidairDashes = 0;
            pogoing = false;
            pogoHit = false;
        }
        else // Simulating gravity.
        {
            if (playerYV < 0) // Upwards velocity.
            {
                playerYV += 0.3;
            }
            else if (playerYV > 3 * gravityYVmultiplier) // High downwards velocity.
            {
                // If slow fall is off, accelerate at a slower rate. If slow fall is on, there's a hard limit to downward acceleration.
                if (gravityYVmultiplier == 1)
                {
                    playerYV += Math.max(0, 0.15 * gravityYVmultiplier);
                }
                else
                {
                    playerYV = 3 * gravityYVmultiplier;
                }
            }
            else // Low downwards velocity.
            {
                playerYV += Math.max(0, 0.3 * gravityYVmultiplier);
            }
            onPlatform = false;
        }
        
        playerRect.setLocation(playerX, playerY);
    }
    
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        if (Main.getGamePhase() == 0)
        {
            g.setColor(Color.WHITE);
            if (!instructions)
            {
                g.setFont(titleFont);
                drawCenteredString(g, "A Platformer", getWidth()/2, getHeight()/5, titleFont);
                drawCenteredString(g, "Without Platforms", getWidth()/2, getHeight()/5 + 40, titleFont);
                g.setFont(secondaryFont);
                drawCenteredString(g, "Press P to play.", getWidth()/2, getHeight()/2 + 40, secondaryFont);
                drawCenteredString(g, "Press D for basic instructions.", getWidth()/2, getHeight()/2 + 100, secondaryFont);
            }
            else
            {
                g.setFont(secondaryFont);
                drawCenteredString(g, "Press B to go back.", getWidth()/2, getHeight()/8 - 10, secondaryFont);

                g.setFont(instructionFont);
                drawCenteredString(g, "You play as a blue square through this game's levels.", getWidth()/2, getHeight()/8 + 30, instructionFont);
                drawCenteredString(g, "The goal is to reach a green square. Avoid touching red.", getWidth()/2, getHeight()/8 + 50, instructionFont);
                drawCenteredString(g, "Every few levels, you will be able to choose an upgrade.", getWidth()/2, getHeight()/8 + 70, instructionFont);
                drawCenteredString(g, "Move left/right by holding down the left/right arrow keys.", getWidth()/2, getHeight()/8 + 90, instructionFont);
                drawCenteredString(g, "Press z to jump.", getWidth()/2, getHeight()/8 + 110, instructionFont);
                drawCenteredString(g, "Press r to generate a new level.", getWidth()/2, getHeight()/8 + 130, instructionFont);
                drawCenteredString(g, "The following applies only if you have certain upgrades:", getWidth()/2, getHeight()/8 + 160, instructionFont);
                drawCenteredString(g, "Holding c gives passive buffs.", getWidth()/2, getHeight()/8 + 180, instructionFont);
                drawCenteredString(g, "Hold the up/down arrow keys to \"tilt\" vertically.", getWidth()/2, getHeight()/8 + 200, instructionFont);
                drawCenteredString(g, "Vertical tilt and left/right determine \"directional input\".", getWidth()/2, getHeight()/8 + 220, instructionFont);
                drawCenteredString(g, "Dash* by pressing space while giving directional input.", getWidth()/2, getHeight()/8 + 240, instructionFont);
                drawCenteredString(g, "Pressing/holding x with directional input does actions.", getWidth()/2, getHeight()/8 + 260, instructionFont);
                drawCenteredString(g, "*With one exception.", getWidth()/2, getHeight() - 20, instructionFont);
            }
        }
        else if (Main.getGamePhase() == 1)
        {
            // Sidenote: This section of code seems to stop upon reaching the upgrade phase but restarts as needed.
            
            // Draw alignment columns if they have been placed, part 1.
            if (alignmentColumnPhase == 1)
            {
                g.setColor(new Color(128, 150, 128));
                g.fillRect(leftAlignmentColumnX, 0, (rightAlignmentColumnX - leftAlignmentColumnX), gameHeight);
            }
            
            // Draw portable waypoint if it has been placed.
            if (teleportPhase == 1)
            {
                g.setColor(new Color(128, 0, 128));
                g.fillRect(waypoint.x, waypoint.y, waypoint.width, waypoint.height);
            }
            
            // Draw special effects.
            for (int i = 0; i < specialEffects.size(); i++)
            {
                int tempID = specialEffects.get(i).getID();
                if (tempID == 0)
                {
                    int[] tempData = specialEffects.get(i).updateJumpEffect();
                    g.setColor(specialEffects.get(i).getColor());
                    g.fillOval(tempData[0], tempData[1], tempData[2], tempData[3]);
                    if (tempData[4] == 1)
                    {
                        specialEffects.remove(i);
                        i--;
                    }
                }
                else if (tempID == 1)
                {
                    int[] tempData = specialEffects.get(i).updateDashEffect();
                    g.setColor(specialEffects.get(i).getColor());
                    g.fillRect(tempData[0], tempData[1], tempData[2], tempData[3]);
                    if (tempData[4] == 1)
                    {
                        specialEffects.remove(i);
                        i--;
                    }
                }
                else if (tempID == 2)
                {
                    int[] tempData = specialEffects.get(i).updateTeleportEffect();
                    g.setColor(specialEffects.get(i).getColor());
                    g.fillOval(tempData[0], tempData[1], tempData[2], tempData[3]);
                    if (tempData[4] == 1)
                    {
                        specialEffects.remove(i);
                        i--;
                    }
                }
            }
            
            // Draw player.
            g.setColor(Color.BLUE);
            g.fillRect(playerRect.x, playerRect.y, playerRect.width, playerRect.height);
            
            // Draw obstacles.
            g.setColor(Color.RED);
            for (Rectangle tempRect : gameObjects.getObstacles())
            {
                g.fillRect(tempRect.x, tempRect.y, tempRect.width, tempRect.height);
            }
            // Draw goal.
            g.setColor(Color.GREEN);
            g.fillRect(gameObjects.getGoal().x, gameObjects.getGoal().y, gameObjects.getGoal().width, gameObjects.getGoal().height);
            
            // Draw pogo jump plus some details.
            pogoHitbox.setLocation(playerRect.x + (playerSize - 6) / 2 , playerRect.y + 16);
            if (pogoing)
            {
                g.setColor(Color.ORANGE);
                g.fillRect(pogoHitbox.x, pogoHitbox.y, pogoHitbox.width, pogoHitbox.height);
                for (Rectangle tempRect : gameObjects.getObstacles())
                {
                    if (pogoHitbox.intersects(tempRect))
                    {
                        playerYV = -3.9;
                        pogoHit = true;
                        break;
                    }
                }
            }
            
            // Draw alignment columns if they have been placed, part 2.
            if (alignmentColumnPhase == 1)
            {
                g.setColor(new Color(255, 255, 0));
                g.drawLine(leftAlignmentColumnX, 0, leftAlignmentColumnX, gameHeight);
                g.drawLine(leftAlignmentColumnX + 8, 0, leftAlignmentColumnX + 8, gameHeight);
                g.drawLine(leftAlignmentColumnX + 16, 0, leftAlignmentColumnX + 16, gameHeight);
                g.drawLine(rightAlignmentColumnX - 8, 0, rightAlignmentColumnX - 8, gameHeight);
                g.drawLine(rightAlignmentColumnX, 0, rightAlignmentColumnX, gameHeight);
            }
            
            if (!resettingLevel) // This if statement helps prevent a bug where resetting the level could make the player touch the goal.
            {
                // Check if goal reached.
                if (playerRect.intersects(gameObjects.getGoal()))
                {
                    Main.nextLevel();
                }
                
                // If the player is invincible (from dashing).
                if (!(Main.getUpgrades()[5] == 3 && dashingData[2] > 0))
                {
                    // Check if the player hit an obstacle (after checking the goal, meaning that goal has priority).
                    for (Rectangle tempRect : gameObjects.getObstacles())
                    {
                        if (playerRect.intersects(tempRect))
                        {
                            resetPlayer();
                        }
                    }
                }
            }
            
            // Level counter display.
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.DIALOG, Font.PLAIN, 32));
            g.drawString("Level: " + Main.getLevel(), 0, 32);
            
            // Win message.
            if (Main.getLevel() >= 30)
            {
                if (!gameWon)
                {
                    gameWon = true;
                    endTime = System.currentTimeMillis();
                }
                g.setFont(titleFont);
                drawCenteredString(g, "You win!", getWidth()/2, getHeight()/2 - 30, titleFont);
                drawCenteredString(g, "Your time: " + String.valueOf((endTime - startTime) / 1000) + "s.", getWidth()/2, getHeight()/2 + 30, titleFont);
            }
        }
        else if (Main.getGamePhase() == 2)
        {
            // Reminder: Game phase 2 is when the player gets to upgrade.
        }
    }
    
    // Method that does the default drawString but centered at (x, y).
    // The original method came from https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
    // I have made some modifications to suit this project.
    public void drawCenteredString(Graphics g, String text, int x, int y, Font font)
    {
        FontMetrics metrics = g.getFontMetrics(font);
        int bottomRightX = x - (metrics.stringWidth(text) / 2);
        // Apparently the ascent of a font is the distance from the tops of the tallest glyphs to the baseline.
        // This y coordinate doesn't seem perfect but it's fine since this project doesn't require ulta-specific text placements.
        int bottomRightY = y + (metrics.getAscent() / 2);
        g.setFont(font);
        g.drawString(text, bottomRightX, bottomRightY);
    }
}