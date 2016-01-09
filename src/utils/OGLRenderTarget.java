package utils;

import java.nio.Buffer;
import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;

public class OGLRenderTarget {
	protected final GL2 gl;
	protected final int width, height, count;
	protected final int[] colorBuffers, drawBuffers;
	protected final int[] depthBuffer = new int[1];
	protected final int[] frameBuffer = new int[1];

	public OGLRenderTarget(GL2 gl, int width, int height) {
		this(gl, width, height, 1);
	}

	public OGLRenderTarget(GL2 gl, int width, int height, int count) {
		this(gl, width, height, count, new OGLTexImageFloat.Format(4));
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> OGLRenderTarget(GL2 gl, int width, int height,
			int count, OGLTexImage.Format<OGLTexImageType> format) {
		this(gl, width, height, count, null, format);
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> OGLRenderTarget(GL2 gl, int count,
			OGLTexImageType texImage) {
		this(gl, texImage.getWidth(), texImage.getHeight(), count, Arrays.asList(texImage), texImage.getFormat());
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> OGLRenderTarget(GL2 gl, OGLTexImageType[] texImage) {
		this(gl, texImage[0].getWidth(), texImage[0].getHeight(), texImage.length, Arrays.asList(texImage),
				texImage[0].getFormat());
	}

	public <OGLTexImageType extends OGLTexImage<OGLTexImageType>> OGLRenderTarget(GL2 gl,
			List<OGLTexImageType> texImage) {
		this(gl, texImage.get(0).getWidth(), texImage.get(0).getHeight(), texImage.size(), texImage,
				texImage.get(0).getFormat());
	}

	private <OGLTexImageType extends OGLTexImage<OGLTexImageType>> OGLRenderTarget(GL2 gl, int width, int height,
			int count, List<OGLTexImageType> texImage, OGLTexImage.Format<OGLTexImageType> format) {
		this.gl = gl;
		this.width = width;
		this.height = height;
		this.count = count;
		this.colorBuffers = new int[count];
		this.drawBuffers = new int[count];
		gl.glGenTextures(count, colorBuffers, 0);
		for (int i = 0; i < count; i++) {
			Buffer imageData = texImage == null ? null : texImage.get(i).getDataBuffer();
			gl.glBindTexture(GL2.GL_TEXTURE_2D, colorBuffers[i]);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, format.getInternalFormat(), width, height, 0, format.getPixelFormat(),
					format.getPixelType(), imageData);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			drawBuffers[i] = GL2.GL_COLOR_ATTACHMENT0 + i;
		}
		gl.glGenTextures(1, depthBuffer, 0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, depthBuffer[0]);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT, width, height, 0, GL2.GL_DEPTH_COMPONENT,
				GL2.GL_FLOAT, null);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);

		gl.glGenFramebuffers(1, frameBuffer, 0);
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer[0]);
		for (int i = 0; i < count; i++)
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0 + i, GL2.GL_TEXTURE_2D,
					colorBuffers[i], 0);
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_TEXTURE_2D, depthBuffer[0], 0);

		if (gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER) != GL2.GL_FRAMEBUFFER_COMPLETE) {
			System.out.println("There is a problem with the FBO");
		}
	}

	public void bind() {
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer[0]);
		gl.glDrawBuffers(count, drawBuffers, 0);
		gl.glViewport(0, 0, width, height);
	}

	public void bindColorTexture(int shaderProgram, String name, int slot) {
		bindColorTexture(shaderProgram, name, slot, 0);
	}

	public void bindColorTexture(int shaderProgram, String name, int slot, int bufferIndex) {
		gl.glActiveTexture(GL2.GL_TEXTURE0 + slot);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, colorBuffers[bufferIndex]);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, name), slot);
	}

	public void bindDepthTexture(int shaderProgram, String name, int slot) {
		gl.glActiveTexture(GL2.GL_TEXTURE0 + slot);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, depthBuffer[0]);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, name), slot);
	}

	public OGLTexture getColorTexture() {
		return getColorTexture(0);
	}

	public OGLTexture getColorTexture(int bufferIndex) {
		Texture texture = new Texture(colorBuffers[bufferIndex], GL2.GL_TEXTURE_2D, width, height, width, height,
				false);
		return new OGLTexture(gl, texture);
	}

	public OGLTexture getDepthTexture() {
		Texture texture = new Texture(depthBuffer[0], GL2.GL_TEXTURE_2D, width, height, width, height, false);
		return new OGLTexture(gl, texture);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public void finalize() {
		gl.glDeleteFramebuffers(1, frameBuffer, 0);
		gl.glDeleteTextures(count, colorBuffers, 0);
		gl.glDeleteTextures(1, depthBuffer, 0);
	}

}
