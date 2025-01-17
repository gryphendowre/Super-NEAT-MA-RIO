package com.mojang.mario;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;




import javax.imageio.ImageIO;
//import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;

import com.mojang.mario.level.LevelGenerator;
import com.mojang.mario.sprites.*;
import com.mojang.sonar.FakeSoundEngine;
import com.mojang.sonar.SonarSoundEngine;


public class MarioComponent extends JComponent implements Runnable, KeyListener, FocusListener
{
    private static  long serialVersionUID;
    public static final int TICKS_PER_SECOND = 48;

    private boolean running = false;
    private int width, height;
    private GraphicsConfiguration graphicsConfiguration;
    public Scene scene;
    private SonarSoundEngine sound;
    private boolean focused = false;
    private boolean useScale2x = false;
    private MapScene mapScene;

    private Scale2x scale2x = new Scale2x(320, 240);
    
    public static int type = LevelGenerator.TYPE_OVERGROUND;
    public int difficulty;
    public static int seed = new Random().nextInt(); 
    public static int x1 = new Random().nextInt(); 
    public static int y1 = new Random().nextInt(); 
    public boolean endofNewTick = false; 
    private int generation; 
    private int randomThreadNum;
    
    public MarioComponent(int width, int height, long serialUID, int seed, int generation, int genomeNum, int difficulty)
    {
    	this.seed = seed; 
        this.setFocusable(true);
        this.setEnabled(true);
        this.width = width;
        this.height = height;
        this.generation = generation ;
        this.randomThreadNum = genomeNum;
        this.difficulty = difficulty; 
        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);


        setFocusable(true);
    }
    public MarioComponent(int width, int height, long serialUID)
    {
        this.setFocusable(true);
        this.setEnabled(true);
        this.width = width;
        this.height = height;

        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        /*try
        {
            sound = new SonarSoundEngine(64);
        }
        catch (LineUnavailableException e)
        {
            e.printStackTrace();
            sound = new FakeSoundEngine();
        }*/

        setFocusable(true);
    }

    public void toggleKey(int keyCode, boolean isPressed)
    {
        if (keyCode == KeyEvent.VK_LEFT)
        {
            scene.toggleKey(Mario.KEY_LEFT, isPressed);
        }
        if (keyCode == KeyEvent.VK_RIGHT)
        {
            scene.toggleKey(Mario.KEY_RIGHT, isPressed);
        }
        if (keyCode == KeyEvent.VK_DOWN)
        {
            scene.toggleKey(Mario.KEY_DOWN, isPressed);
        }
        if (keyCode == KeyEvent.VK_UP)
        {
            scene.toggleKey(Mario.KEY_UP, isPressed);
        }
        if (keyCode == KeyEvent.VK_A)
        {
            scene.toggleKey(Mario.KEY_SPEED, isPressed);
        }
        if (keyCode == KeyEvent.VK_S)
        {
            scene.toggleKey(Mario.KEY_JUMP, isPressed);
        }
        if (isPressed && keyCode == KeyEvent.VK_F1)
        {
            useScale2x = !useScale2x;
        }
    }

    public void paint(Graphics g)
    {
    }

    public void update(Graphics g)
    {
    }

    public void start()
    {
    	//System.out.println("MarioComponent Start " + running); 
        if (!running)
        {
        	Random rand = new Random(10);
            running = true;
            new Thread(this, "Game Thread" + randomThreadNum).start();
        }
    }

    public void stop()
    {
        Art.stopMusic();
        running = false;
    }

    public void run()
    {
        graphicsConfiguration = getGraphicsConfiguration();

        //      scene = new LevelScene(graphicsConfiguration);
        mapScene = new MapScene(graphicsConfiguration, this, new Random().nextLong());
        //scene.setSound(sound);

        Art.init(graphicsConfiguration, null);
        Art.stopMusic();
        VolatileImage image = createVolatileImage(320, 240);
        Graphics g = getGraphics();
        Graphics og = image.getGraphics();

        int lastTick = -1;
        //        double lastNow = 0;
        int renderedFrames = 0;
        int fps = 0;

        //        double now = 0;
        //        double startTime = System.nanoTime() / 1000000000.0; 
        //        double timePerFrame = 0; 
        double time = System.nanoTime() / 1000000000.0;
        double now = time;
        double averagePassedTime = 0;

        addKeyListener(this);
        addFocusListener(this);

        boolean naiveTiming = true;

        //Jumps right into a randomized level
                
        startLevel(seed * x1 * y1 + x1 * 31871 + y1 * 21871, difficulty, type);
        int everyTick = 0; 
        while (running)
        {
        	endofNewTick = false; 
            double lastTime = time;
            time = System.nanoTime() / 1000000000.0;
            double passedTime = time - lastTime;

            if (passedTime < 0) naiveTiming = false; // Stop relying on nanotime if it starts skipping around in time (ie running backwards at least once). This sometimes happens on dual core amds.
            averagePassedTime = averagePassedTime * 0.9 + passedTime * 0.1;

            if (naiveTiming)
            {
                now = time;
            }
            else
            {
                now += averagePassedTime;
            }

            int tick = (int) (now * TICKS_PER_SECOND);
            if (lastTick == -1) lastTick = tick;
            while (lastTick < tick)
            {
                scene.tick();
                lastTick++;

                if (lastTick % TICKS_PER_SECOND == 0)
                {
                    fps = renderedFrames;
                    renderedFrames = 0;
                }
            }

            float alpha = (float) (now * TICKS_PER_SECOND - tick);
            //sound.clientTick(alpha);

            int x = (int) (Math.sin(now) * 16 + 160);
            int y = (int) (Math.cos(now) * 16 + 120);

            og.setColor(Color.WHITE);
            og.fillRect(0, 0, 320, 240);

            scene.render(og, alpha);
            
            if (!this.hasFocus() && tick/4%2==0)
            {
                //String msg = "CLICK TO PLAY";

                //drawString(og, msg, 160 - msg.length() * 4 + 1, 110 + 1, 0);
                //drawString(og, msg, 160 - msg.length() * 4, 110, 7);
            }
            og.setColor(Color.BLACK);
            /*          drawString(og, "FPS: " + fps, 5, 5, 0);
             drawString(og, "FPS: " + fps, 4, 4, 7);*/

            if (width != 320 || height != 240)
            {
                if (useScale2x)
                {
                    g.drawImage(scale2x.scale(image), 0, 0, null);
                }
                else
                {
                    g.drawImage(image, 0, 0, 640, 480, null);
                }
            }
            else
            {
                g.drawImage(image, 0, 0, null);
            }

            renderedFrames++;
            everyTick++; 
            
            //For Rendering
            /*try {
                // retrieve image
                BufferedImage bi = image.getSnapshot(); 
                
                String path = "E:"+File.separator+"Joe"+File.separator+"Documents"+File.separator+"School"+File.separator+"Grad School"+File.separator+"Spring 2014"+File.separator+"Project"+File.separator+"Videos"+File.separator+"img"+everyTick+".png";
                File outputfile = new File(path);
                ImageIO.write(bi, "png", outputfile);
            } catch (IOException e) {
            }*/
            try
            {
                Thread.sleep(5);
            }
            catch (InterruptedException e)
            {
            }
            endofNewTick = true;
        }

       // Art.stopMusic();
    }
    
    private void drawString(Graphics g, String text, int x, int y, int c)
    {
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++)
        {
            g.drawImage(Art.font[ch[i] - 32][c], x + i * 8, y, null);
        }
    }

    public void keyPressed(KeyEvent arg0)
    {
        toggleKey(arg0.getKeyCode(), true);
    }

    public void keyReleased(KeyEvent arg0)
    {
        toggleKey(arg0.getKeyCode(), false);
    }

    public void startLevel(long seed, int difficulty, int type)
    {
        scene = new LevelScene(graphicsConfiguration, this, seed, difficulty, type, generation, this.randomThreadNum);
        //scene.setSound(sound);
        scene.init();
    }

    public void levelFailed()
    {
    	//System.out.println("Lossed"); 	
    	//startLevel(seed * x1 * y1 + x1 * 31871 + y1 * 21871, difficulty, type);
        //scene = mapScene;
        //mapScene.startMusic();
        //Mario.lives--;
        //if (Mario.lives == 0)
        //{
    	//TODO trigger distance when failed
        //    lose();
        //}
    }

    public void keyTyped(KeyEvent arg0)
    {
    }

    public void focusGained(FocusEvent arg0)
    {
        focused = true;
    }

    public void focusLost(FocusEvent arg0)
    {
        focused = false;
    }

    public void levelWon()
    {    	
    	//System.out.println("Won"); 
    	//startLevel(seed * x1 * y1 + x1 * 31871 + y1 * 21871, difficulty, type);
    	//TODO get distance for winning 
    }
    
    public void win()
    {
        scene = new WinScene(this);
        //scene.setSound(sound);
        scene.init();
    }
    
    public void toTitle()
    {
        Mario.resetStatic();
        scene = new TitleScene(this, graphicsConfiguration);
        //scene.setSound(sound);
        scene.init();
    }
    
    public void lose()
    {
    	
        scene = new LoseScene(this);
        //scene.setSound(sound);
        scene.init();
    }

    public void startGame()
    {
        scene = mapScene;
        mapScene.init();
   }

}