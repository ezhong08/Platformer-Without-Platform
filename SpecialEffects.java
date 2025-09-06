/*
 * Copyright (c) 2025 Eric Zhong. All rights reserved.
 * Licensed under the MIT License.
*/

/** File name: SpecialEffects.java
 * The SpecialEffects class is closely connected to PlayerPanel.
 * Its purpose is to store information about various (currently only) visual effects (each objects stores information for one effect).
 * Note that the visual effects that impact the platforming experience aren't handled here.
 * When such effects occur and how they are drawn by the graphics is decided in PlayerPanel.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;

public class SpecialEffects
{
    private int effectX;
    private int effectY;
    private int duration; // This isn't a fixed amount of time. It depends on the Timer in PlayerPanel.
    private int ID;
    private Color effectColor;
    
    /*
    ID 0: Jump effect.
    ID 1: Dash effect.
    ID 2: Teleport effect.
    */
    public SpecialEffects(int theID, int playerX, int playerY, Color theColor)
    {
        effectX = playerX;
        effectY = playerY;
        ID = theID;
        effectColor = theColor;
        if (ID == 0)
        {
            duration = 15;
        }
        else if (ID == 1)
        {
            duration = 15;
        }
        else if (ID == 2)
        {
            duration = 15;
        }
    }
    
    // A lot of the following 3 methods are based on player size. They work because player size is currently constant (it's 16).
    public int[] updateJumpEffect()
    {
        int[] tempData = new int[5];
        tempData[0] = effectX + 8 - (16 - duration);
        tempData[1] = effectY + 16;
        tempData[2] = 2 * (16 - duration);
        tempData[3] = (16 - duration);
        tempData[4] = 0;
        duration--;
        if (duration <= 0)
        {
            tempData[4]++;
        }
        return tempData;
    }
    
    public int[] updateDashEffect()
    {
        int[] tempData = new int[5];
        tempData[0] = effectX;
        tempData[1] = effectY;
        tempData[2] = 16;
        tempData[3] = 16;
        tempData[4] = 0;
        duration--;
        if (duration <= 0)
        {
            tempData[4]++;
        }
        return tempData;
    }
    
    public int[] updateTeleportEffect()
    {
        int[] tempData = new int[5];
        tempData[0] = effectX - 8;
        tempData[1] = effectY - 8;
        tempData[2] = 32;
        tempData[3] = 32;
        duration--;
        if (duration <= 0)
        {
            tempData[4]++;
        }
        return tempData;
    }
    
    public int getID()
    {
        return ID;
    }
    
    public Color getColor()
    {
        return effectColor;
    }
}