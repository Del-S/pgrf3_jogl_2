package utils;

import java.nio.Buffer;

public interface OGLTexImage<OGLTexImageType> {
	static interface Format<OGLTexImageType> {
		int getInternalFormat();
		int getPixelFormat();
		int getPixelType();
		int getComponentCount();
		Buffer newBuffer(int width, int height);
		OGLTexImageType newTexImage(int width, int height);
	}
	int getWidth();
	int getHeight();
	void setDataBuffer(Buffer buffer);
	Buffer getDataBuffer();
	Format<OGLTexImageType> getFormat();
}
