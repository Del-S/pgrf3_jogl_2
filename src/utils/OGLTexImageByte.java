package utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

public class OGLTexImageByte implements OGLTexImage<OGLTexImageByte> {
	private final byte[] data;
	private final int width, height;
	private final OGLTexImage.Format<OGLTexImageByte> format;

	public static class Format implements OGLTexImage.Format<OGLTexImageByte> {
		private final int componentCount;

		public Format(int componentCount) {
			this.componentCount = componentCount;
		}

		@Override
		public int getInternalFormat() {
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
			return GL2.GL_UNSIGNED_BYTE;
		}

		@Override
		public int getComponentCount() {
			return componentCount;
		}

		@Override
		public Buffer newBuffer(int width, int height) {
			return Buffers.newDirectByteBuffer(width * height * componentCount);
		}

		@Override
		public OGLTexImageByte newTexImage(int width, int height) {
			return new OGLTexImageByte(width, height, this);
		}
	}

	public OGLTexImageByte(int width, int height, int componentCount) {
		this(width, height, new OGLTexImageByte.Format(componentCount));
	}

	public OGLTexImageByte(int width, int height, int componentCount, byte[] data) {
		this(width, height, new OGLTexImageByte.Format(componentCount), data);
	}

	public OGLTexImageByte(int width, int height, OGLTexImage.Format<OGLTexImageByte> format) {
		this.width = width;
		this.height = height;
		this.format = format;
		data = new byte[width * height * format.getComponentCount()];
	}

	public OGLTexImageByte(int width, int height, OGLTexImage.Format<OGLTexImageByte> format, byte[] data) {
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
		if (buffer instanceof ByteBuffer && buffer.capacity() == width * height * format.getComponentCount()) {
			buffer.rewind();
			((ByteBuffer) buffer).get(data);
		}
	}

	@Override
	public Buffer getDataBuffer() {
		Buffer buffer = ByteBuffer.wrap(data);
		buffer.rewind();
		return buffer;
	}

	@Override
	public OGLTexImage.Format<OGLTexImageByte> getFormat() {
		return format;
	}

	public byte[] getData() {
		return data;
	}

	public OGLTexImageByte(int width, int height) {
		this(width, height, new Format(1));
	}

	public OGLTexImageFloat toOGLTexImageFloat() {
		return toOGLTexImageFloat(format.getComponentCount()) ;
	}

	public OGLTexImageFloat toOGLTexImageFloat(int componentCount) {
		float[] array = new float[width * height * componentCount];
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				for (int i = 0; i < componentCount; i++)
					array[y * width * componentCount + x * componentCount + i] 
							= (0xff & data[y * width * format.getComponentCount() + x * format.getComponentCount()
									+ i % format.getComponentCount()] )/ 255.0f;
						//0xff z duvodu pouziti bytu jako bezznaminkoveho

		return new OGLTexImageFloat(width, height, new OGLTexImageFloat.Format(componentCount), array);
	}

	public void setPixel(int x, int y, int component, byte value) {
		if (x >= 0 && x < width && y >= 0 && y < height && component >= 0 && component < format.getComponentCount()) {
			data[y * width * format.getComponentCount() + x * format.getComponentCount() + component] = value;
		}
	}

	public void setPixel(int x, int y, byte value) {
		setPixel(x, y, 0, value);
	}

	public byte getPixel(int x, int y, int component) {
		byte value = 0;
		if (x >= 0 && x < width && y >= 0 && y < height && component >= 0 && component < format.getComponentCount())
			value = data[y * width * format.getComponentCount() + x * format.getComponentCount() + component];
		return value;
	}

	public byte getPixel(int x, int y) {
		return getPixel(x, y, 0);
	}
}
