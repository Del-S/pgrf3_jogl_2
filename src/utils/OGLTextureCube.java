package utils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.GL2;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;


public class OGLTextureCube {
	protected GL2 gl;
	public Texture texture;
	public static final String[] SUFFICES_POS_NEG = { "posx", "negx", "posy", "negy", "posz", "negz" };
	public static final String[] SUFFICES_POS_NEG_FLIP_Y = { "posx", "negx", "negy", "posy", "posz", "negz" };
	public static final String[] SUFFICES_POSITIVE_NEGATIVE = { "positive_x", "negative_x", "positive_y", "negative_y", "positive_z", "negative_z" };
	public static final String[] SUFFICES_POSITIVE_NEGATIVE_FLIP_Y = { "positive_x", "negative_x", "negative_y", "positive_y", "positive_z", "negative_z" };
	public static final String[] SUFFICES_RIGHT_LEFT = { "right", "left", "bottom", "top", "front", "back" };
	public static final String[] SUFFICES_RIGHT_LEFT_FLIP_Y  = { "right", "left", "top", "bottom", "front", "back" };
	private static final int[] targets = { GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
	                                         GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
	                                         GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
	                                         GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
	                                         GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
	                                         GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z };
	
	public OGLTextureCube(GL2 gl, String[] fileNames) {
		this.gl = gl;
		texture = TextureIO.newTexture(GL2.GL_TEXTURE_CUBE_MAP);
		readFiles(texture,fileNames);
		if (texture != null)
			System.out.println("OK");
	}

	public OGLTextureCube(GL2 gl, String fileName, String[] suffixes) {
		this.gl = gl;
		texture = TextureIO.newTexture(GL2.GL_TEXTURE_CUBE_MAP);
		String[] fileNames = new String[suffixes.length];    
		String baseName=fileName.substring(0,fileName.lastIndexOf('.'));
    	String suffix=fileName.substring(fileName.lastIndexOf('.')+1,fileName.length());
    	for (int i = 0; i < suffixes.length; i++) {
    		fileNames[i] = new String(baseName + suffixes[i] + "." + suffix);
	    }
    	readFiles(texture,fileNames);
		if (texture != null)
			System.out.println("OK");
	}

		private void readFiles(Texture texture, String[] fileNames){
		for (int i = 0; i < fileNames.length; i++) {
	    	TextureData data;
	        System.out.println("reading texture " + fileNames[i]);
			try {
				data = TextureIO.newTextureData(gl.getGLProfile(), 
							new File(fileNames[i]),
				            true,
				            null);
				texture.updateImage(gl, data, targets[i]);	
				
			} catch (IOException e) {
				System.out.println("failed");
				System.out.println(e.getMessage());
			}
	       }
	}
	
	public void bind(int shaderProgram, String name, int slot) {
		if (texture == null) return;
		gl.glActiveTexture(GL2.GL_TEXTURE0 + slot);
		texture.bind(gl);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, name), slot);
	}
	
	public void setTextureBuffer(int pixelFormat, int pixelType, Buffer buffer, int indexCube) {
		texture.bind(gl);
		gl.glTexSubImage2D(targets[indexCube], 0, 0, 0, texture.getWidth(),
				texture.getHeight(), pixelFormat, pixelType, buffer);
	}

	public Buffer getTextureBuffer(int pixelFormat, int pixelType, int indexCube) {
		texture.bind(gl);
		Buffer buffer = Buffers.newDirectByteBuffer(texture.getWidth() * texture.getHeight() * 4);
		gl.glGetTexImage(targets[indexCube], 0, pixelFormat, pixelType, buffer);
		return buffer;
	}

	
	public void view(float x, float y, int pixelFormat, int pixelType){
		int[] shaderProgram=new int[1]; 
		gl.glGetIntegerv(GL2.GL_CURRENT_PROGRAM,shaderProgram,0);
		gl.glUseProgram(0);
		gl.glPushAttrib(GL2.GL_ENABLE_BIT);
		gl.glPushAttrib(GL2.GL_DEPTH_BUFFER_BIT);
		gl.glFrontFace(GL2.GL_CCW);
		gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINE);
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDepthMask(false); 
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glEnable(GL2.GL_TEXTURE_CUBE_MAP);
		//gl.glBindTexture (targets[0],texture.getTextureObject());
		texture.bind(gl);
		gl.glTranslated(x, y, 0);
		gl.glScaled(1/4.0, 1/4.0, 1/4.0);
		gl.glTranslated(0, 1.5, 0);
		//-x
		gl.glBegin (GL2.GL_QUADS);
		gl.glTexCoord3f (1, 1,1);		gl.glVertex2f (0, 0);
		gl.glTexCoord3f (1, 1,-1);		gl.glVertex2f (1, 0);
		gl.glTexCoord3f (1,-1,-1);		gl.glVertex2f (1, 1);
		gl.glTexCoord3f (1, -1,1);		gl.glVertex2f (0, 1);
		gl.glEnd();
		gl.glTranslated(2, 0, 0);
		//+x
		gl.glBegin (GL2.GL_QUADS);
		gl.glTexCoord3f (-1, 1,-1);		gl.glVertex2f (0, 0);
		gl.glTexCoord3f (-1, 1,1);		gl.glVertex2f (1, 0);
		gl.glTexCoord3f (-1,-1,1);		gl.glVertex2f (1, 1);
		gl.glTexCoord3f (-1, -1,-1);		gl.glVertex2f (0, 1);
		gl.glEnd();
		gl.glTranslated(-1, 0, 0);
		//-z
		gl.glBegin (GL2.GL_QUADS);
		gl.glTexCoord3f (1, 1,-1);		gl.glVertex2f (0, 0);
		gl.glTexCoord3f (-1,1,-1);		gl.glVertex2f (1, 0);
		gl.glTexCoord3f (-1,-1,-1);		gl.glVertex2f (1, 1);
		gl.glTexCoord3f (1, -1,-1);		gl.glVertex2f (0, 1);
		gl.glEnd();
		gl.glTranslated(2, 0, 0);
		//+z
		gl.glBegin (GL2.GL_QUADS);
		gl.glTexCoord3f (-1, 1,1);		gl.glVertex2f (0, 0);
		gl.glTexCoord3f (1,1,1);		gl.glVertex2f (1, 0);
		gl.glTexCoord3f (1,-1,1);		gl.glVertex2f (1, 1);
		gl.glTexCoord3f (-1, -1, 1);		gl.glVertex2f (0, 1);
		gl.glEnd();
		gl.glTranslated(-2, 1, 0);
		//y
		gl.glBegin (GL2.GL_QUADS);
		gl.glTexCoord3f (1,-1,-1);		gl.glVertex2f (0, 0);
		gl.glTexCoord3f (-1,-1,-1);		gl.glVertex2f (1, 0);
		gl.glTexCoord3f (-1,-1,1);		gl.glVertex2f (1, 1);
		gl.glTexCoord3f (1,-1,1);		gl.glVertex2f (0, 1);
		gl.glEnd();
		gl.glTranslated(0, -2, 0);
			//-y
		gl.glBegin (GL2.GL_QUADS);
		gl.glTexCoord3f (1,1,1);		gl.glVertex2f (0, 0);
		gl.glTexCoord3f (-1,1,1);		gl.glVertex2f (1, 0);
		gl.glTexCoord3f (-1,1,-1);		gl.glVertex2f (1, 1);
		gl.glTexCoord3f (1,1,-1);		gl.glVertex2f (0, 1);
		gl.glEnd();
		
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glPopAttrib();
		gl.glPopAttrib();
		gl.glUseProgram(shaderProgram[0]);
		
	}
	
	public void view(float x, float y){
		view(x, y, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE);
	}
		
	public void view(){
		view(0, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE);
	}
}
