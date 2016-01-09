package geom;

import com.jogamp.opengl.GL2;

import utils.OGLBuffers;

public interface GeomGenerator {
	OGLBuffers generate(int m, int n);
        OGLBuffers generateImageGrid();
}