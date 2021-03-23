/* ************************************************************************** */
/*                                                                            */
/*                                                                            */
/*   MazeSolver.java                                                          */
/*                                                                            */
/*   By: Loïc <lbertran@student.42lyon.fr>                                    */
/*                                                                            */
/*   Created: 2021/03/23 16:06:45 by Loïc                                     */
/*   Updated: 2021/03/23 19:17:09 by Loïc                                     */
/*                                                                            */
/* ************************************************************************** */
package me.loic;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MazeSolver extends JPanel {

    private BufferedImage canvas;

    static boolean  DEBUG = true;               /* Want your stdout to get spammed ? */
    static int      ACTION_DELAY = 1;           /* Delay (in ms) between each action (1 action represents a move of 1 pixel on the screen) */

    int width = 6;                      /* Width of the square representing the person in the maze */
    int startX = 390;
    int startY = 552;                   /* X and Y coordinates of the entry of the maze */
    int x = startX, y = startY;         /* current X and Y coordinates of the person in the maze */
    int emptyColorThreshold = 10000000; /* Empty spaces aren't exactly white in the maze picture, this will be our color threshold for what we consider an empty space */
    int exitColor = 65301;              /* Color to find in the maze (the green square at the exit) */

    public MazeSolver() {

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                debug(me.getX() + " " + me.getY() + " = " + (canvas.getRGB(me.getX(), me.getY()) & 0xFFFFFF));
            }
        });

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File("C:\\Users\\troti\\Desktop\\maze.png")); // Edit to your path to the maze image (or update to read from resources folder)
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        canvas = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < canvas.getWidth(); i++)
            for (int j = 0; j < canvas.getHeight(); j++)
                canvas.setRGB(i, j, image.getRGB(i, j));

        drawRect(Color.RED.getRGB(), x, y, this.width, this.width);
        solve();
    }

    int getColorAt(int x, int y)
    {
        return canvas.getRGB(x, y) & 0xFFFFFF;
    }

    public void solve() {

        int color = getColorAt(x, y);
        boolean solvable = true, solved = false;

        int colorRight = getColorAt(x + width, y);
        int colorRight1 = getColorAt(x + this.width, y + width - 1);
        int colorLeft = getColorAt(x - 1, y);
        int colorLeft1 = getColorAt(x - 1, y + width - 1);
        int colorUp = getColorAt(x, y - 1);
        int colorUp1 = getColorAt(x + width - 1, y - 1);
        int colorDown = getColorAt(x, Math.min(y + width, canvas.getHeight() - 6));
        int colorDown1 = getColorAt(x + width - 1, Math.min(y + width, canvas.getHeight() - 6));

        debug("---------------------------------------");
        debug(x + " " + y + " (" + canvas.getWidth() + " " + canvas.getHeight() + ")");
        debug("Up: " + colorUp + " " + colorUp1);
        debug("Down: " + colorDown + " " + colorDown1);
        debug("Left: " + colorLeft + " " + colorLeft1);
        debug("Right: " + colorRight + " " + colorRight1);
        debug("---------------------------------------");
        if (colorRight > emptyColorThreshold && colorRight1 > emptyColorThreshold && colorRight != color) {
            x++;
            debug("moving forward right");
        } else if (colorLeft > emptyColorThreshold && colorLeft1 > emptyColorThreshold && colorLeft != color) {
            x--;
            debug("moving forward left");
        } else if (y < canvas.getHeight() - 6 && colorDown > emptyColorThreshold && colorDown1 > emptyColorThreshold && colorDown != color) {
            y++;
            debug("moving forward down");
        } else if (colorUp > emptyColorThreshold && colorUp1 > emptyColorThreshold && colorUp != color) {
            y--;
            debug("moving forward up");
        } else if (colorRight == color && colorRight1 == color) {
            drawRect(Color.GRAY.getRGB(), x, y, 1, width);
            debug("moving back right");
            x++;
        } else if (colorLeft == color && colorLeft1 == color) {
            drawRect(Color.GRAY.getRGB(), x + width - 1, y, 1, width);
            debug("moving back left");
            x--;
        } else if (colorUp == color && colorUp1 == color) {
            drawRect(Color.GRAY.getRGB(), x, y + width - 1, width, 1);
            debug("moving back up");
            y--;
        } else if (colorDown == color && colorDown1 == color) {
            drawRect(Color.GRAY.getRGB(), x, y, width, 1);
            debug("moving back down");
            y++;
        } else
            solvable = false; // We can't move anywhere, the maze isn't solvable
        drawRect(Color.RED.getRGB(), x, y, width, width);
        if (colorUp == exitColor || colorDown == exitColor || colorRight == exitColor || colorLeft == exitColor) {
            debug("Solved !");
            solved = true;
        }
        if (solvable && !solved) {
            Thread th = new Thread(new Runnable() {
                public void run() {

                    try {
                        Thread.sleep(ACTION_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    solve();
                }
            });
            th.start();
        }
        else if (!solvable)
            debug("Can't solve maze");
    }

    public Dimension getPreferredSize() {
        return new Dimension(canvas.getWidth(), canvas.getHeight());
    }

    public void drawRect(int color, int x1, int y1, int width, int height) {
        for (int x = x1; x < x1 + width; x++) {
            for (int y = y1; y < y1 + height; y++) {
                canvas.setRGB(x, y, color);
            }
        }
        repaint();
    }

    private void debug(String message) {
        if (DEBUG)
            System.out.println(message);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Maze solver");

        MazeSolver panel = new MazeSolver();

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);
    }
}