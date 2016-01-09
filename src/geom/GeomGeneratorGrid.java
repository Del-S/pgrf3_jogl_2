package geom;

import com.jogamp.opengl.GL2;
import java.util.Arrays;
import utils.OGLBuffers;

/* Generates grid via triangles */

public class GeomGeneratorGrid implements GeomGenerator {
	private int m, n; // m - ��dky, n - sloupe�ky

	private float[] vertexBuffer;
	private int[] indexbuffer;
	private GL2 gl;
                
        public GeomGeneratorGrid(GL2 gl) {
            this.gl = gl;
        }
	
        @Override
        public OGLBuffers generateImageGrid() {
            float[] vertexBufferIm = {
                -1, 1,
                1, 1,
                1,-1,
                -1,-1
            };

            int[] indexbufferIm = { 0, 1, 2, 3 };		 

            OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
            };

            return new OGLBuffers(gl, vertexBufferIm, attributes, indexbufferIm);
	}
        
	@Override
	public OGLBuffers generate(int m, int n) {
            
            // TODO: fix for m*n now only works for n*n

                this.vertexBuffer = new float[2*m*n];
                int count = 0;
		for (int i = 0; i < m; i++){
			for (int j = 0; j < n; j++){
				vertexBuffer[count] =  (float) i / (m-1);
				vertexBuffer[count+1] =  (float) j / (n-1);
                                count = count + 2;
			}
		}
                
		int p = 0;
		this.indexbuffer = new int[6*(m-1)*(n-1)];
		for (int k = 0; k < m-1; k++){
			for (int j = 0; j < n-1; j++){
				int i = k*m + j;
				
				indexbuffer[p] = i;
				indexbuffer[p+1] = i + n;
				indexbuffer[p+2] = i + n + 1;
				indexbuffer[p+3] = i;
				indexbuffer[p+4] = i + n + 1;
				indexbuffer[p+5] = i + 1;
				
				p += 6; 
			}	
		}
		
		OGLBuffers.Attrib[] attributes = {
                    new OGLBuffers.Attrib("inPosition", 2) 
                };

		return new OGLBuffers(gl, vertexBuffer, attributes,
				indexbuffer);
	}

	public int getM() {
		return m;
	}

	public void setM(int m) {
		this.m = m;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}
	
	
}
