package utils;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

public class OGLTexImageFloat implements OGLTexImage<OGLTexImageFloat> {
	private final float[] data;
	private final int width, height;
	private final OGLTexImage.Format<OGLTexImageFloat> format;

	public static class Format implements OGLTexImage.Format<OGLTexImageFloat> {
		private final int componentCount;

		public Format(int componentCount) {
			this.componentCount = componentCount;
		}

		@Override
		public int getInternalFormat() {
			switch (componentCount) {
			case 1:
				return GL2.GL_R32F;
			case 2:
				return GL2.GL_RG32F;
			case 3:
				return GL2.GL_RGB32F;
			case 4:
				return GL2.GL_RGBA32F;
			default:
				return -1;
			}
		}

		@Override
		public int getPixelFormat() {
			switch (componentCount) {
			case 1:
				return GL2.GL_RED;
			case 2:
				return GL2.GL_RG;
			case 3:
				return GL2.GL_RGB;
			case 4:
				return GL2.GL_RGBA;
			default:
				return -1;
			}
		}

		@Override
		public int getPixelType() {
			return GL2.GL_FLOAT;
		}

		@Override
		public int getComponentCount() {
			return componentCount;
		}

		@Override
		public Buffer newBuffer(int width, int height) {
			return Buffers.newDirectFloatBuffer(width * height * componentCount);
		}

		@Override
		public OGLTexImageFloat newTexImage(int width, int height) {
			return new OGLTexImageFloat(width, height, this);
		}
	}

	public static class FormatDepth implements OGLTexImage.Format<OGLTexImageFloat> {
		@Override
		public int getInternalFormat() {
			return GL2.GL_DEPTH_COMPONENT;
		}

		@Override
		public int getPixelFormat() {
			return GL2.GL_DEPTH_COMPONENT;
		}

		@Override
		public int getPixelType() {
			return GL2.GL_FLOAT;
		}

		@Override
		public int getComponentCount() {
			return 1;
		}

		@Override
		public Buffer newBuffer(int width, int height) {
			return Buffers.newDirectFloatBuffer(width * height);
		}

		@Override
		public OGLTexImageFloat newTexImage(int width, int height) {
			return new OGLTexImageFloat(width, height, this);
		}
	}

	public OGLTexImageFloat(int width, int height, int componentCount) {
		this(width, height, new OGLTexImageFloat.Format(componentCount));
	}

	public OGLTexImageFloat(int width, int height, int componentCount, float[] data) {
		this(width, height, new OGLTexImageFloat.Format(componentCount), data);
	}

	public OGLTexImageFloat(int width, int height, OGLTexImage.Format<OGLTexImageFloat> format) {
		this.width = width;
		this.height = height;
		this.format = format;
		data = new float[width * height * format.getComponentCount()];
	}

	public OGLTexImageFloat(int width, int height, OGLTexImage.Format<OGLTexImageFloat> format, float[] data) {
		this.width = width;
		this.height = height;
		this.format = format;
		this.data = data;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setDataBuffer(Buffer buffer) {
		if (buffer instanceof FloatBuffer && buffer.capacity() == width * height * format.getComponentCount()) {
			buffer.rewind();
			((FloatBuffer) buffer).get(data);
		}
	}

	@Override
	public Buffer getDataBuffer() {
		Buffer buffer = FloatBuffer.wrap(data);
		buffer.rewind();
		return buffer;
	}

	@Override
	public OGLTexImage.Format<OGLTexImageFloat> getFormat() {
		return format;
	}

	public float[] getData() {
		return data;
	}

	public OGLTexImageFloat(int width, int height) {
		this(width, height, new Format(1));
	}

	public OGLTexImageByte toOGLTexImageByte() {
		return toOGLTexImageByte(format.getComponentCount()) ;
		
	}

	public OGLTexImageByte toOGLTexImageByte(int componentCount) {
		byte[] array = new byte[width * height * componentCount];
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				for (int i = 0; i < componentCount; i++)
					array[y * width * componentCount + x * componentCount
							+ i] = (byte) (Math.min(data[y * width * format.getComponentCount()
									+ x * format.getComponentCount() + i % format.getComponentCount()], 1.0) * 255.0);

		return new OGLTexImageByte(width, height, new OGLTexImageByte.Format(componentCount), array);
	}

	public void setPixel(int x, int y, int component, float value) {
		if (x >= 0 && x < width && y >= 0 && y < height && component >= 0 && component < format.getComponentCount()) {
			data[y * width * format.getComponentCount() + x * format.getComponentCount() + component] = value;
		}
	}

	public void setPixel(int x, int y, float value) {
		setPixel(x, y, 0, value);
	}

	public float getPixel(int x, int y, int component) {
		float value = 0;
		if (x >= 0 && x < width && y >= 0 && y < height && component >= 0 && component < format.getComponentCount())
			value = data[y * width * format.getComponentCount() + x * format.getComponentCount() + component];
		return value;
	}

	public float getPixel(int x, int y) {
		return getPixel(x, y, 0);
	}
}
