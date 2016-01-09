package utils;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;

public class OGLTexture {
	private final GL2 gl;
	public final Texture texture;

	public OGLTexture(GL2 gl, String fileName) {
		this.gl = gl;
		Texture texture = null;
		try {
			System.out.print("Loading texture " + fileName + " ... ");
			texture = TextureIO.newTexture(new File(fileName), true);
		} catch (IOException e) {
			System.out.println("failed");
			System.out.println(e.getMessage());
		}
		if (texture != null)
			System.out.println("OK");
		this.texture = texture;
	}

	public OGLTexture(GL2 gl, Texture texture) {
		this.gl = gl;
		this.texture = texture;
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> OGLTexture(GL2 gl, OGLTexImageType image) {
		this.gl = gl;
		Buffer buffer = image.getDataBuffer();
		int[] textureID = new int[1];
		gl.glGenTextures(1, textureID, 0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, textureID[0]);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, image.getFormat().getInternalFormat(), image.getWidth(),
				image.getHeight(), 0, image.getFormat().getPixelFormat(), image.getFormat().getPixelType(), buffer);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		texture = new Texture(textureID[0], GL2.GL_TEXTURE_2D, image.getWidth(), image.getHeight(), image.getWidth(),
				image.getHeight(), false);
	}

	public void save(String fileName) {
		try {
			System.out.print("Saving texture " + fileName + " ... ");
			TextureIO.write(texture, new File(fileName));
		} catch (GLException | IOException e) {
			System.out.println("failed");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		System.out.println("OK");
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> void setTextureBuffer(
			OGLTexImage.Format<OGLTexImageType> format, Buffer buffer) {
		texture.bind(gl);
		gl.glTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, texture.getWidth(), texture.getHeight(), format.getPixelFormat(),
				format.getPixelType(), buffer);
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> Buffer getTextureBuffer(
			OGLTexImage.Format<OGLTexImageType> format) {
		texture.bind(gl);
		Buffer buffer = format.newBuffer(texture.getWidth(), texture.getHeight());
		gl.glGetTexImage(GL2.GL_TEXTURE_2D, 0, format.getPixelFormat(), format.getPixelType(), buffer);
		return buffer;
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> void setTexImage(OGLTexImageType image) {
		setTextureBuffer(image.getFormat(), image.getDataBuffer());
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> OGLTexImageType getTexImage(
			OGLTexImage.Format<OGLTexImageType> format) {
		OGLTexImageType image = format.newTexImage(texture.getWidth(), texture.getHeight());
		image.setDataBuffer(getTextureBuffer(format));
		return image;
	}

	public void bind(int shaderProgram, String name, int slot) {
		if (texture == null)
			return;
		gl.glActiveTexture(GL2.GL_TEXTURE0 + slot);
		texture.bind(gl);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, name), slot);
	}

	public BufferedImage toBufferedImage() {
		int[] array = new int[texture.getWidth() * texture.getHeight()]; // buffer.array();
		texture.bind(gl);
		gl.glGetTexImage(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, IntBuffer.wrap(array));
		BufferedImage image = new BufferedImage(texture.getWidth(), texture.getHeight(), BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, texture.getWidth(), texture.getHeight(), array, 0, texture.getWidth());
		return image;
	}

	public void fromBufferedImage(BufferedImage img) {
		texture.bind(gl);
		int[] array = new int[texture.getWidth() * texture.getHeight()];
		img.getRGB(0, 0, texture.getWidth(), texture.getHeight(), array, 0, texture.getWidth());
		gl.glTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, texture.getWidth(), texture.getHeight(), GL2.GL_RGBA,
				GL2.GL_UNSIGNED_INT_8_8_8_8_REV, IntBuffer.wrap(array));
	}

	public void view(double x, double y, double scale, double aspect) {
		int[] shaderProgram = new int[1];
		gl.glGetIntegerv(GL2.GL_CURRENT_PROGRAM, shaderProgram, 0);
		gl.glUseProgram(0);
		gl.glPushAttrib(GL2.GL_ENABLE_BIT);
		gl.glPushAttrib(GL2.GL_DEPTH_BUFFER_BIT);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDepthMask(false);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glBindTexture(GL2.GL_TEXTURE_2D, texture.getTextureObject());
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTranslated(x, y, 0);
		gl.glScaled(scale*aspect, scale, scale);
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2f(0, 0);
		gl.glTexCoord2f(1, 0);
		gl.glVertex2f(1, 0);
		gl.glTexCoord2f(1, 1);
		gl.glVertex2f(1, 1);
		gl.glTexCoord2f(0, 1);
		gl.glVertex2f(0, 1);
		gl.glEnd();
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glPopAttrib();
		gl.glPopAttrib();
		gl.glUseProgram(shaderProgram[0]);
	}
	public void view(double x, double y, double scale) {
		view(x, y, scale, 1.0);
	}	
	
	public void view(double x, double y) {
		view(x, y, 1.0);
	}

	public void view() {
		view(-1, -1);
	}

}
