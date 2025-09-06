/*
 * Copyright (c) 2025 Eric Zhong. All rights reserved.
 * Licensed under the MIT License.
*/

/** File name: GameObjects.java
 * The class GameObjects's purpose is to help PlayerPanel by managing the semi-random generation of levels that the player must beat.
 * Note that the goal square that the player must reach is also generated here.
 * This class also coordinates with PlayerPanel to make the win message.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;

class GameObjects
{
    private ArrayList<Rectangle> obstacles = new ArrayList<Rectangle>();
    private Rectangle goal;
    // The 400x400 screen is divided into 16 100x100 regions for obstacle generation.
    // Each 100x100 region is in turn divided into 16 25x25 regions later on.
    // Nonnegative numbers indicate increasing obstacle density. -1 is for custom sections to be left untouched by semi-random obstacle generation.
    // Examples of custom sections include the player's starting area and the goal area.
    // The rows correspond to x coordinates, the columns correspond to y coordinates.
    private int[][] obstacleDensityMap = new int[4][4];

    public GameObjects()
    {
        generateLevel();
    }
    
    public void generateLevel()
    {
        obstacles.clear();
        if (Main.getLevel() >= 30) // Don't generate obstacles if the end level is reached.
        {
            goal = new Rectangle(450, 450, 0, 0); // Don't let the players reach the goal if the game is won.
            return;
        }
        
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                obstacleDensityMap[i][j] = 0;
            }
        }
        obstacleDensityMap[0][3] = -1; // The player's starting region shouldn't have obstacles.
        // Choose a location for the goal square in a way such that early levels have lower goal squares.
        // After that, the goal square's region shouldn't have obstacles.
        int tempLevelVar = Math.min((int)(0.8*Main.getLevel()+0.2), 16);
        goal = new Rectangle((int)(4*Math.random()+12) * 25, (int)(tempLevelVar*Math.random()+(16-tempLevelVar)) * 25, 25, 25);
        obstacleDensityMap[getMapIndex(goal.x)][getMapIndex(goal.y)] = -1;
        
        // Part 1 of obstacle generation: Creating a difficulty heat map.
        // Note that locations closer to the bottom are to be of higher difficulty.
        for (int i = 0; i < (int)(2.6 * (Main.getLevel() - 1)); i++) // Difficulty increases with level.
        {
            double tempY = Math.random();
            int temp2Y;
            if (Main.getLevel() <= 10)
            {
                // The chances here are (from lowest regions to highest regions) 0.9, 0.1, 0, and 0.
                if (tempY < 0.9)
                {
                    temp2Y = 3;
                }
                else
                {
                    temp2Y = 2;
                }
            }
            else if (Main.getLevel() <= 20)
            {
                // The chances here are (from lowest regions to highest regions) 0.6, 0.3, 0.1, and 0.
                if (tempY < 0.6)
                {
                    temp2Y = 3;
                }
                else if (tempY < 0.9)
                {
                    temp2Y = 2;
                }
                else
                {
                    temp2Y = 1;
                }
            }
            else
            {
                // The chances here are (from lowest regions to highest regions) 0.4, 0.3, 0.2, and 0.1.
                if (tempY < 0.4)
                {
                    temp2Y = 3;
                }
                else if (tempY < 0.7)
                {
                    temp2Y = 2;
                }
                else if (tempY < 0.9)
                {
                    temp2Y = 1;
                }
                else
                {
                    temp2Y = 0;
                }
            }
            
            int tempX = (int)(4 * Math.random());
            if (obstacleDensityMap[tempX][temp2Y] != -1)
            {
                obstacleDensityMap[tempX][temp2Y]++;
            }
            else
            {
                i--; // Redo because custom location hit.
            }
        }
        
        // Part 2 of obstacle generation: Taking a heat map and actually creating obstacles.
        // This nested loop goes through each 100x100 region of the screen sequentially.
        // Note that i and j must be multiplied by 4 to be able to accurately correspond to their 100x100 region.
        // This is because the inner code considers things on a 25x25 basis, not 100x100.
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                int tempDensity = obstacleDensityMap[i][j];
                // The temporary regions are intended to be either 3 or 4 in dimension.
                // The goal with temporary regions is to reduce the chance that different obstacles overlap by decreasing the 100x100 region
                // when something is placed that basically covers an entire edge row/column of that 100x100 region.
                int tempRegionX = 4;
                int tempRegionY = 4;
                
                if (tempDensity == -1) // Custom region.
                {
                    continue;
                }
                
                // Essentially, repeatedly put obstacles of predetermined shapes into the current region until all of that region's difficulty is "spent".
                // Note that a lot of the (Math.random() > 0.5)s are for the orientation of obstacles.
                // Some people may think that this is inefficient. This is hard to prove or disprove, but note that this method
                // is able to create "structures" of obstacles that simpler randomization probably usually won't create.
                // For example, there's code here that can create upside-down u-shaped things on the ground specifically so that
                // players with teleport (or its upgrade) can teleport into said u-shaped things.
                while (tempDensity > 0)
                {
                    if (tempDensity == 1)
                    {
                        obstacles.add(generateRandomGridObstacle(4*i, 4*j, tempRegionX, tempRegionY, 1, 1));
                        tempDensity--;
                    }
                    else if (tempDensity == 2)
                    {
                        if (Math.random() > 0.5)
                        {
                            obstacles.add(generateRandomGridObstacle(4*i, 4*j, tempRegionX, tempRegionY, 2, 1));
                        }
                        else
                        {
                            obstacles.add(generateRandomGridObstacle(4*i, 4*j, tempRegionX, tempRegionY, 1, 2));
                        }
                        tempDensity -= 2;
                    }
                    else if (tempDensity == 3)
                    {
                        if (Math.random() > 0.5)
                        {
                            obstacles.add(generateRandomGridObstacle(4*i, 4*j, tempRegionX, tempRegionY, 3, 1));
                        }
                        else
                        {
                            obstacles.add(generateRandomGridObstacle(4*i, 4*j, tempRegionX, tempRegionY, 1, 3));
                        }
                        tempDensity -= 3;
                    }
                    else if (tempDensity >= 4 && j == 3 && tempRegionY == 4 && Math.random() > 0.6)
                    {
                        obstacles.add(generateRandomGridObstacle(4*i, 4*j+3, 4, 1, 4, 1));
                        tempRegionY = 3;
                        tempDensity -= 4;
                    }
                    else if (tempDensity == 4 || (tempDensity > 4 && Math.random() > 0.9))
                    {
                        // Checks on region size to avoid overlap and obstacles out of their region to an extent.
                        if (tempRegionX == 4 && tempRegionY == 4 && Math.random() > 0.7)
                        {
                            if (Math.random() > 0.5)
                            {
                                obstacles.add(generateRandomGridObstacle(4*i, 4*j, tempRegionX, tempRegionY, 4, 1));
                            }
                            else
                            {
                                obstacles.add(generateRandomGridObstacle(4*i, 4*j, tempRegionX, tempRegionY, 1, 4));
                            }
                        }
                        else if (Math.random() > 0.5)
                        {
                            obstacles.add(generateRandomGridObstacle(4*i, 4*j, tempRegionX, tempRegionY, 2, 2));
                        }
                        else
                        {
                            obstacles.add(generateRandomGridObstacle(4*i, 4*j, tempRegionX, tempRegionY, 2, 1));
                            obstacles.add(generateRandomGridObstacle(4*i, 4*j, tempRegionX, tempRegionY, 1, 2));
                        }
                        tempDensity -= 4;
                    }
                    else if (tempDensity >= 5 && j == 3 && tempRegionY == 4 && Math.random() > 0.6)
                    {
                        obstacles.add(generateRandomGridObstacle(4*i, 4*j+2, 4, 1, 4, 1));
                        obstacles.add(generateRandomGridObstacle(4*i, 4*j+3, 1, 1, 1, 1));
                        obstacles.add(generateRandomGridObstacle(4*i+3, 4*j+3, 1, 1, 1, 1));
                        tempRegionY = 3; // It's not the end of the world if there's a bit of overlap.
                        tempDensity -= 5;
                    }
                    else if (tempDensity >= 6 && j != 3 && Math.random() > 0.8)
                    {
                        obstacles.add(generateRandomGridObstacle(4*i+1, 4*j+1, 2, 2, 2, 2));
                        for (int tempIterator = 0; tempIterator <= 1; tempIterator++)
                        {
                            if (Math.random() > 0.75)
                            {
                                 obstacles.add(generateRandomGridObstacle(4*i, 4*j, 3, 1, 1, 1));
                            }
                            else if (Math.random() > 2.0/3.0)
                            {
                                obstacles.add(generateRandomGridObstacle(4*i+3, 4*j, 1, 3, 1, 1));
                            }
                            else if (Math.random() > 0.5)
                            {
                                obstacles.add(generateRandomGridObstacle(4*i+1, 4*j+3, 3, 1, 1, 1));
                            }
                            else
                            {
                                obstacles.add(generateRandomGridObstacle(4*i, 4*j+1, 1, 3, 1, 1));
                            }
                        }
                        tempDensity -= 6;
                    }
                    else
                    {
                        tempDensity--;
                    }
                }
            }
        }
    }
    
    // This method proposes a Rectangle to be added to the obstacle list.
    // leftX, upY, areaWidth, and areaHeight are for the specific region (when viewed from the 25x25 grid perspective) to create something in.
    // blockX and blockY are the dimensions of the Rectangle to generate.
    public Rectangle generateRandomGridObstacle(int leftX, int upY, int areaWidth, int areaHeight, int blockX, int blockY)
    {
        int randomX = (int)((areaWidth-blockX+1) * Math.random()) + leftX;
        int randomY = (int)((areaHeight-blockY+1) * Math.random()) +  upY;
        return new Rectangle(25*randomX, 25*randomY, 25*blockX, 25*blockY);
    }
    
    public int getMapIndex(int coordinate)
    {
        // The grid is a 4x4 of 100x100 pixel squares.
        // Because of the use of Rectangles, the top and left edges
        // will be considered part of the region.
        // This works well with integer divison.
        return coordinate / 100;
    }
    
    public ArrayList<Rectangle> getObstacles()
    {
        return obstacles;
    }
    
    public Rectangle getGoal()
    {
        return goal;
    }
}