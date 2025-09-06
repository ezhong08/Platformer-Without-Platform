/*
 * Copyright (c) 2025 Eric Zhong. All rights reserved.
 * Licensed under the MIT License.
 */

/** File name: KeyInputs.java
 * This class uses KeyListener to take in keyboard input from the user and sets it up for PlayerPanel to use.
 * There's a reason why this class exists.
 * Essentially, if these KeyListener methods were placed in PlayerPanel, the CardLayout switching would disable keyboard inputs while UpgradePanel showed.
 * That lead to various bugs. So instead, a KeyInputs object is an instance variable in Main.
 * This works since Main is the class managing the CardLayout switching and thus isn't affected by it.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;

public class KeyInputs implements KeyListener
{
    // It may be more accurate to say that these booleans track when a key is held.
    private boolean rightPressed = false;
    private boolean leftPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean zPressed = false;
    private boolean xPressed = false;
    private boolean cPressed = false;
    private boolean spacePressed = false;
    private boolean bPressed = false;
    private boolean dPressed = false;
    private boolean pPressed = false;
    private boolean rPressed = false;

    public KeyInputs()
    {
    }
    
    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        } 
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            leftPressed = true;
        } 
        else if (e.getKeyCode() == KeyEvent.VK_UP) {
            upPressed = true;
        } 
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            downPressed = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_Z) {
            zPressed = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_X) {
            xPressed = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_C) {
            cPressed = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            spacePressed = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_B) {
            bPressed = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_D) {
            dPressed = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_P) {
            pPressed = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_R) {
            rPressed = true;
        }
        else {
        }
    }

    public void keyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        } 
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            leftPressed = false;
        } 
        else if (e.getKeyCode() == KeyEvent.VK_UP) {
            upPressed = false;
        } 
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_Z) {
            zPressed = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_X) {
            xPressed = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_C) {
            cPressed = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            spacePressed = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_B) {
            bPressed = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_D) {
            dPressed = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_P) {
            pPressed = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_R) {
            rPressed = false;
        }
        else {
        }
    }
    
    public boolean getIfPressed(String key)
    {
        if (key.equals("right")) {
            return rightPressed;
        } 
        else if (key.equals("left")) {
            return leftPressed;
        } 
        else if (key.equals("up")) {
            return upPressed;
        } 
        else if (key.equals("down")) {
            return downPressed;
        }
        else if (key.equals("z")) {
            return zPressed;
        }
        else if (key.equals("x")) {
            return xPressed;
        }
        else if (key.equals("c")) {
            return cPressed;
        }
        else if (key.equals("space")) {
            return spacePressed;
        }
        else if (key.equals("b")) {
            return bPressed;
        }
        else if (key.equals("d")) {
            return dPressed;
        }
        else if (key.equals("p")) {
            return pPressed;
        }
        else if (key.equals("r")) {
            return rPressed;
        }
        else{
            return false;
        }
    }
}