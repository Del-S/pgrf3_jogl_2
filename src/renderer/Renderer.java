package renderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import geom.*;
import app.Settings;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;
import utils.OGLBuffers;
import utils.OGLRenderTarget;
import utils.OGLTexture;
import utils.ShaderUtils;
import utils.ToFloatArray;

public class Renderer implements GLEventListener, MouseListener,
		MouseMotionListener, KeyListener {

	int width, height, ox, oy;
	int shaderProgram, shaderProgramSec, locMat, viewSettings, viewIntensity;
        
	Camera cam = new Camera();
        
        GeomGenerator gen;
        OGLBuffers grid, bufferQuad;
	Mat4 proj;
        Settings s;
        
        OGLTexture texture;
        OGLRenderTarget renderTarget;
        GL2 gl;
        
        public Renderer(Settings s, int width, int height) {
            this.s = s;
            this.width = width;
            this.height = height;
        }
        
	public void init(GLAutoDrawable glDrawable) {
		gl = glDrawable.getGL().getGL2();
		
                gen = new GeomGeneratorGrid(gl);
                grid = gen.generateImageGrid();

		System.out.println("Init GL is " + gl.getClass().getName());
		System.out.println("OpenGL version " + gl.glGetString(GL2.GL_VERSION));
		System.out.println("OpenGL vendor " + gl.glGetString(GL2.GL_VENDOR));
		System.out.println("OpenGL renderer " + gl.glGetString(GL2.GL_RENDERER));
		System.out.println("OpenGL extensions " + gl.glGetString(GL2.GL_EXTENSIONS));

                shaderProgram = ShaderUtils.loadProgram(gl, "./shader/texture");
                shaderProgramSec = ShaderUtils.loadProgram(gl, "./shader/grid");
       
                /* parametrizace shaderu */
		viewSettings = gl.glGetUniformLocation(shaderProgramSec,"viewSettings");
		viewIntensity = gl.glGetUniformLocation(shaderProgramSec,"viewIntensity");
                
                texture = new OGLTexture(gl, new File("./images/river.jpg").getPath());
                
                locMat = gl.glGetUniformLocation(shaderProgram, "mat");
                	
                renderTarget = new OGLRenderTarget(gl, width, height);
                
		cam.setPosition(new Vec3D(5, 5, 2.5));
		cam.setAzimuth(Math.PI * 1.25);
		cam.setZenith(Math.PI * -0.125);

		gl.glEnable(GL2.GL_DEPTH_TEST);
	}
	
	public void display(GLAutoDrawable glDrawable) {
		gl = glDrawable.getGL().getGL2();
                
                /*** First run ***/
		gl.glUseProgram(shaderProgram);
                    
                // Bind render target
		renderTarget.bind();
                
		gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

                /* Load and bind Image */
                if(s.isReaload()) {
                    texture = new OGLTexture(gl, new File(s.getImageS()).getPath());
                    s.setReaload(false);
                }
                texture.bind(shaderProgram, "texture", 0);
		
		gl.glUniformMatrix4fv(locMat, 1, false,
				ToFloatArray.convert(cam.getViewMatrix().mul(proj)), 0);
		grid.draw(GL2.GL_QUADS, shaderProgram);

		
		/*** Second run ***/
		gl.glUseProgram(shaderProgramSec);
                
		// Default render target
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		gl.glViewport(0, 0, width, height);

		gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
                
                // Parameters for effects
                gl.glUniform1f(viewIntensity, (float) s.getViewI());	
		gl.glUniform1f(viewSettings, (float) s.getViewS());

		// Use first run render in this run
		renderTarget.getColorTexture().bind(shaderProgramSec, "texture", 0);
                
		// Draw second run
		grid.draw(GL2.GL_QUADS, shaderProgramSec);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		this.width = width;
		this.height = height;
		proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		ox = e.getX();
		oy = e.getY();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		cam.addAzimuth((double) Math.PI * (ox - e.getX())
				/ width);
		cam.addZenith((double) Math.PI * (e.getY() - oy)
				/ width);
		ox = e.getX();
		oy = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W:
			cam.forward(1);
			break;
		case KeyEvent.VK_D:
			cam.right(1);
			break;
		case KeyEvent.VK_S:
			cam.backward(1);
			break;
		case KeyEvent.VK_A:
			cam.left(1);
			break;
		case KeyEvent.VK_SHIFT:
			cam.down(1);
			break;
		case KeyEvent.VK_CONTROL:
			cam.up(1);
			break;
		case KeyEvent.VK_SPACE:
			cam.setFirstPerson(!cam.getFirstPerson());
			break;
		case KeyEvent.VK_R:
			cam.mulRadius(0.9f);
			break;
		case KeyEvent.VK_F:
			cam.mulRadius(1.1f);
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void dispose(GLAutoDrawable arg0) {
	}
        
        public void saveCanvas(String file, String format) {
            AWTGLReadBufferUtil bufferUtils = new AWTGLReadBufferUtil(gl.getGLProfile(), true);
            gl.getContext().makeCurrent();
            BufferedImage image = bufferUtils.readPixelsToBufferedImage(gl, true);

            // save it
            try {
                ImageIO.write(image, format, new File(file));
                System.out.println("File saved to: " + file + " format: " + format);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}