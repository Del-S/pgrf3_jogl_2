package app;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import renderer.*;

public class App {
    private static final int FPS = 60; // animator's target frames per second

    private static final int width = 512;
    private static final int height = 384;
    
    private static GLCanvas canvas;
    
    public static void main(String[] args) {
        try {
            Frame appFrame = new Frame("PGRF3 - David Sucharda");
            appFrame.setSize(width, height);
            
            // setup OpenGL Version 2
            GLProfile profile = GLProfile.get(GLProfile.GL2);
            GLCapabilities capabilities = new GLCapabilities(profile);

            // Create Settings Frame
            // Could be as MenuBar or embeded in appFrame but this seems better
            Settings s = new Settings();
            
            // The canvas is the widget that's drawn in the JFrame
            canvas = new GLCanvas(capabilities);
            Renderer ren = new Renderer(s, width, height);
                canvas.addGLEventListener(ren);
                canvas.addMouseListener(ren);
                canvas.addMouseMotionListener(ren);
                canvas.addKeyListener(ren);
                canvas.setSize( width, height );

            appFrame.add(canvas);
            s.setRen(ren);

            final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

            appFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    new Thread() {
                        @Override
                        public void run() {
                           if (animator.isStarted()) animator.stop();
                           System.exit(0);
                        }
                    }.start();
                }
            });
            
            appFrame.pack();
            appFrame.setVisible(true);
            animator.start(); // start the animation loop

        } catch (Exception e) {
                e.printStackTrace();
        }
    }
}