/*
Copyright 2009 Allen Franklin Jordan (allen.jordan@gmail.com).

This file is part of Franklin Math.

Franklin Math is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Franklin Math is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Franklin Math.  If not, see <http://www.gnu.org/licenses/>.
 */
package franklinmath.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
//import javax.swing.*;

/**
 * A random game implemented as an easter egg feature.
 * @author Allen Jordan
 */
public class GamePanel extends javax.swing.JPanel implements ActionListener {

    //store the current window width and height
    protected int windowWidth,  windowHeight;
    //variables for the main character's position, size, and movement
    protected int xLocation,  yLocation,  speed,  circleDiameter;
    //the HP of the main character
    protected int playerHP;
    //the maximum possible player HP
    protected int playerHPMax;
    //variables for attacking
    protected int attackTimer,  attackTime,  attackSize;
    //protection from the player just holding down the attack button
    protected boolean spaceReleasedSinceLastAttack;
    //the rate that enemies appear
    protected double enemySpawnRate;
    //the score of the player
    protected long playerScore;
    //the maximum number of enemies on the screen
    protected int maxEnemies;
    //a list to keep track of enemies
    //todo: switch this to a linked list to improve efficiency
    protected Vector<Enemy> enemyList;

    //an array to store key presses (true indicates a key press at the given KeyEvent VK code)
    protected boolean[] keys;
    //an image to act as a double buffer to prevent flickering while refreshing the screen
    protected Image doubleBufferImage;
    //the graphics object for the double buffer image
    protected Graphics2D doubleBufferGraphics;
    //width and height of the double buffer image
    protected int doubleBufferWidth,  doubleBufferHeight;
    //a timer to manage repainting updates
    protected javax.swing.Timer timer;

    //keep track of various timings to increase difficulty
    protected long startTime;

    //an inner class representing an Enemy that can attack the player
    protected class Enemy {

        //the enemy's x and y positions
        public float xPosition,  yPosition;
        //the state of the enemy
        public int state;
        //the enemy's power level
        public int power;
        //the distance an enemy can see the player
        public int sightDistance;
        //timer for animating enemy death
        public int deathTimer;

        //different possible states the enemy can take
        public final static int ENEMY_STOPPED = 0;
        public final static int ENEMY_ATTACKING = 1;
        public final static int ENEMY_DYING = 2;

        public Enemy() {
            xPosition = 0;
            yPosition = 0;
            power = 4;
            sightDistance = 50;
            deathTimer = 10;
            state = ENEMY_STOPPED;
        }
    };

    public GamePanel() {
        initComponents();

        //setup the initial set of variables
        xLocation = 20;
        yLocation = 20;
        speed = 2;
        circleDiameter = 10;
        playerHP = 100;
        playerHPMax = 100;
        attackTimer = 0;
        attackTime = 8;
        attackSize = 10;
        spaceReleasedSinceLastAttack = true;
        enemySpawnRate = 0.05;
        playerScore = 0;
        maxEnemies = 20;
        enemyList = new Vector<Enemy>();

        doubleBufferWidth = 0;
        doubleBufferHeight = 0;

        //create an initial enemy
        Enemy startingEnemy = new Enemy();
        startingEnemy.xPosition = 80;
        startingEnemy.yPosition = 80;
        startingEnemy.power = 5;
        enemyList.add(startingEnemy);

        startTime = System.currentTimeMillis();

        //setup the key press array
        keys = new boolean[500];
        for (int i = 0; i < 500; i++) {
            keys[i] = false;
        }

        //begin with a null double buffer image, which will be initialized on the first call to update
        doubleBufferImage = null;

        //request keyboard focus
        this.requestFocus();

        timer = new javax.swing.Timer(30, this);
        timer.start();
    }

    //re-draw the panel on each timeout
    @Override
    public void actionPerformed(ActionEvent event) {
        this.update(this.getGraphics());
    }

    public double DistanceBetween(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    //the actual drawing code for the panel
    @Override
    public void update(Graphics g) {
        //update the window size storage variables
        windowWidth = this.getWidth();
        windowHeight = this.getHeight();
        if (windowWidth < 100) {
            windowWidth = 100;
        }
        if (windowHeight < 100) {
            windowHeight = 100;
        }

        //check if we need to create the image for double buffering
        if ((doubleBufferImage == null) || (doubleBufferWidth != windowWidth) || (doubleBufferHeight != windowHeight)) {
            doubleBufferWidth = windowWidth;
            doubleBufferHeight = windowHeight;
            doubleBufferImage = this.createImage(doubleBufferWidth, doubleBufferHeight);
            doubleBufferGraphics = (Graphics2D) doubleBufferImage.getGraphics();
        }

        //check if the player has died
        if (playerHP <= 0) {
            doubleBufferGraphics.setColor(Color.RED);
            doubleBufferGraphics.fillRect(0, 0, windowWidth, windowHeight);
            doubleBufferGraphics.setColor(Color.WHITE);
            doubleBufferGraphics.drawString("Game Over", windowWidth / 2 - 9 * 6 / 2, windowHeight / 2 - 6 / 2);
            doubleBufferGraphics.drawString("Score: " + playerScore, 10, 20);
        } //otherwise, the player is still alive and the game works as normal
        else {

            //check for movement key presses
            if (keys[KeyEvent.VK_LEFT]) {
                xLocation -= speed;
            }
            if (keys[KeyEvent.VK_RIGHT]) {
                xLocation += speed;
            }
            if (keys[KeyEvent.VK_UP]) {
                yLocation -= speed;
            }
            if (keys[KeyEvent.VK_DOWN]) {
                yLocation += speed;
            }
            //make sure our main character stays within the window bounds
            if (xLocation < 0) {
                xLocation = 0;
            } else if (xLocation > (windowWidth - circleDiameter)) {
                xLocation = windowWidth - circleDiameter;
            }
            if (yLocation < 0) {
                yLocation = 0;
            } else if (yLocation > (windowHeight - circleDiameter)) {
                yLocation = windowHeight - circleDiameter;
            }

            //check for the attack key press
            if ((keys[KeyEvent.VK_SPACE]) && (attackTimer == 0) && (spaceReleasedSinceLastAttack)) {
                spaceReleasedSinceLastAttack = false;
                attackTimer = attackTime;
            }
            //check for space bar reset
            if ((!keys[KeyEvent.VK_SPACE]) && (attackTimer == 0)) {
                spaceReleasedSinceLastAttack = true;
            }

            //clear the screen
            doubleBufferGraphics.setColor(Color.WHITE);
            doubleBufferGraphics.setStroke(new BasicStroke(1));
            doubleBufferGraphics.fillRect(0, 0, windowWidth, windowHeight);

            //draw our main character
            doubleBufferGraphics.setColor(Color.BLACK);
            doubleBufferGraphics.drawOval(xLocation, yLocation, circleDiameter, circleDiameter);

            //check if the main character is attacking
            if (attackTimer > 0) {
                attackTimer--;
                //calculate the angle of the rotation attack
                double circleAngle = 2 * Math.PI * (attackTime - attackTimer) / attackTime;

                //position to center the attack
                int xOffset = xLocation + circleDiameter / 2;
                int yOffset = yLocation + circleDiameter / 2;

                //draw attack lines up to the current angle
                for (double angle = 0; angle < circleAngle; angle += 0.5) {
                    double cosPosition = Math.cos(angle);
                    double sinPosition = Math.sin(angle) * (-1);
                    doubleBufferGraphics.drawLine((int) ((circleDiameter / 2 + 1) * cosPosition + xOffset), (int) ((circleDiameter / 2 + 1) * sinPosition + yOffset), (int) ((circleDiameter + attackSize) * cosPosition + xOffset), (int) ((circleDiameter + attackSize) * sinPosition + yOffset));
                }

                //draw an attack line for the current angle
                double cosPosition = Math.cos(circleAngle);
                double sinPosition = Math.sin(circleAngle) * (-1);
                doubleBufferGraphics.drawLine((int) ((circleDiameter / 2 + 1) * cosPosition + xOffset), (int) ((circleDiameter / 2 + 1) * sinPosition + yOffset), (int) ((circleDiameter + attackSize) * cosPosition + xOffset), (int) ((circleDiameter + attackSize) * sinPosition + yOffset));
            }

            //check if we need to create a new enemy
            if ((Math.random() < enemySpawnRate) && (enemyList.size() < maxEnemies)) {
                //create a new enemy
                Enemy newEnemy = new Enemy();
                newEnemy.power = 5;
                //see if we should make it a stronger enemy
                if (Math.random() < enemySpawnRate * 5) {
                    newEnemy.power += Math.random() * 20 + 5;
                }
                newEnemy.sightDistance = 50 + newEnemy.power;

                //make sure the enemy spawns far enough away from the main character
                do {
                    newEnemy.xPosition = (float) (Math.random() * (windowWidth - circleDiameter));
                    newEnemy.yPosition = (float) (Math.random() * (windowHeight - circleDiameter));
                } while (DistanceBetween(newEnemy.xPosition, newEnemy.yPosition, xLocation, yLocation) < circleDiameter * 5);

                //add this new enemy to the list
                enemyList.add(newEnemy);
            }

            //draw and update the enemies
            doubleBufferGraphics.setColor(Color.RED);
            int centerXLocation = xLocation + circleDiameter / 2;
            int centerYLocation = yLocation + circleDiameter / 2;
            for (int i = 0; i < enemyList.size(); i++) {
                Enemy enemy = enemyList.get(i);

                float centerEnemyX = enemy.xPosition + ((float) enemy.power) / 2;
                float centerEnemyY = enemy.yPosition + ((float) enemy.power) / 2;

                //if the enemy is dying, animate the death
                if (enemy.state == Enemy.ENEMY_DYING) {
                    enemy.deathTimer--;
                    if (enemy.deathTimer <= 0) {
                        enemyList.remove(i);
                        i--;
                    } else {
                        int size = (int) (((double) enemy.deathTimer) / 10 * enemy.power);
                        doubleBufferGraphics.drawOval((int) enemy.xPosition, (int) enemy.yPosition, size, size);
                    }
                //otherwise, the enemy is still alive and possibly attacking
                } else {

                    //determine how close the enemy is to the main character
                    double enemyDistanceToMainCharacter = DistanceBetween(centerEnemyX, centerEnemyY, centerXLocation, centerYLocation);
                    //see if the player has moved within the enemy's sight
                    if (enemyDistanceToMainCharacter < enemy.sightDistance) {
                        enemy.state = Enemy.ENEMY_ATTACKING;
                    } else {
                        enemy.state = Enemy.ENEMY_STOPPED;
                    }
                    //do collision detection based on whether the main character is attacking or not
                    if (attackTimer > 0) {
                        //if the enemy has hit the player's attack, kill it
                        if (enemyDistanceToMainCharacter < (((double) enemy.power) / 2 + ((double) circleDiameter) / 2 + attackSize)) {
                            enemy.state = Enemy.ENEMY_DYING;
                            //increase player health as a bonus for defeating enemies
                            playerHP++;
                            if (playerHP > playerHPMax) {
                                playerHP = playerHPMax;
                            }
                            //increase the player's score by the killed enemy's power
                            playerScore += enemy.power;
                        }
                    } else {
                        //if the enemy has hit a non-attacking player, reduce HP
                        if (enemyDistanceToMainCharacter < (enemy.power / 2 + circleDiameter / 2)) {
                            playerHP--;
                        }
                    }

                    //possible states of enemy movement
                    if (enemy.state == Enemy.ENEMY_STOPPED) {
                        doubleBufferGraphics.drawOval((int) enemy.xPosition, (int) enemy.yPosition, enemy.power, enemy.power);
                    } else if (enemy.state == Enemy.ENEMY_ATTACKING) {
                        doubleBufferGraphics.fillOval((int) enemy.xPosition, (int) enemy.yPosition, enemy.power, enemy.power);

                        //use trigonometry to determine enemy movement towards the player
                        float horizDirec = centerXLocation - centerEnemyX;
                        float vertDirec = centerYLocation - centerEnemyY;
                        float angle = (float) Math.atan2(vertDirec, horizDirec);
                        float yComponent = (float) (((float) enemy.power) / 8 * Math.sin(angle));
                        float xComponent = (float) (((float) enemy.power) / 8 * Math.cos(angle));

                        //update the enemy's position
                        enemy.xPosition += xComponent;
                        enemy.yPosition += yComponent;
                    }
                }
            }

            //draw the green HP bar
            doubleBufferGraphics.setColor(Color.GREEN);
            doubleBufferGraphics.setStroke(new BasicStroke(4));
            doubleBufferGraphics.drawLine(0, 0, (int) (((float) windowWidth * playerHP) / playerHPMax), 0);

            //see if we should increase the difficulty
            long currentTime = System.currentTimeMillis();
            if ((currentTime - startTime) >= 15000) {
                startTime = currentTime;

                enemySpawnRate += 0.01;
                maxEnemies += 5;
                attackSize += 5;

                if (enemySpawnRate > 0.4) {
                    enemySpawnRate = 0.4;
                }
                if (maxEnemies > 150) {
                    maxEnemies = 150;
                }
                if (attackSize > windowWidth / 2) {
                    attackSize = windowWidth / 2;
                }
            }
        }

        //blit the back buffer to the screen (implements double buffering)
        g.drawImage(doubleBufferImage, 0, 0, this);
    }

    //call this before closing whatever window contains this game panel
    public void GameClosing() {
        //stop the timer that draws the screen on an interval
        timer.stop();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                formKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        int keyCode = evt.getKeyCode();
        if ((keyCode >= 0) && (keyCode < 500)) {
            keys[keyCode] = true;
        }
    }//GEN-LAST:event_formKeyPressed

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        int keyCode = evt.getKeyCode();
        if ((keyCode >= 0) && (keyCode < 500)) {
            keys[keyCode] = false;
        }
    }//GEN-LAST:event_formKeyReleased

    private void formKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyTyped
        return;
    }//GEN-LAST:event_formKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
