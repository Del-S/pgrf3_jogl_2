package utils;

import com.jogamp.opengl.GL2;
import com.jogamp.common.nio.Buffers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class OGLModelOBJ {
	private int geometry;
	
	private OGLBuffers buffer;
	
	public OGLBuffers getBuffers() {
		return buffer;
	}

	public int getGeometry() {
		return geometry;
	}

	
	public OGLModelOBJ(GL2 gl, String modelPath) {
	
		class OBJLoader{
			List<float[]> vData = new ArrayList<float[]>(); // List of Vertex Coordinates
			List<float[]> vtData = new ArrayList<float[]>(); // List of Texture Coordinates
			List<float[]> vnData = new ArrayList<float[]>(); // List of Normal Coordinates
			List<int[]> fv = new ArrayList<int[]>(); // Face Vertex Indices;
			List<int[]> ft = new ArrayList<int[]>(); // Face Texture Indices
			List<int[]> fn = new ArrayList<int[]>(); // Face Normal Indices
			
			OBJLoader(String modelPath){
				loadOBJModel(modelPath);
				setFaceRenderType();
			}
			
			private void loadOBJModel(String modelPath) {
				try {
					// Open a file handle and read the models data
					FileReader fileReader = new FileReader(modelPath);

					BufferedReader br = new BufferedReader(fileReader);
					String line = null;
					while ((line = br.readLine()) != null) {
						if (line.startsWith("#")) { 
							// Read Any Descriptor Data in the File
							// System.out.println("Descriptor: "+line);
							// Uncomment to print out file descriptor data
						} else if (line.equals("")) {
							// Ignore whitespace data
						} else if (line.startsWith("v ")) { // Read in Vertex Data
							vData.add(processData(line));
						} else if (line.startsWith("vt ")) { // Read Texture Coordinates
							vtData.add(processData(line));
						} else if (line.startsWith("vn ")) { // Read Normal Coordinates
							vnData.add(processData(line));
						} else if (line.startsWith("f ")) { // Read Face (index) Data
							processFaceData(line);
						}
					}
					br.close();
					fileReader.close();
					System.out.println("OBJ model: " + modelPath + "... read");
				} catch (IOException e) {
					System.out.println("Failed to find or read OBJ: " + modelPath);
					System.err.println(e);
				}
			}

			private float[] processData(String read) {
				String s[] = read.split("\\s+");
				return (processFloatData(s)); 
			}

			private float[] processFloatData(String sdata[]) {
				float data[] = new float[sdata.length - 1];
				for (int loop = 0; loop < data.length; loop++) {
					data[loop] = Float.parseFloat(sdata[loop + 1]);
				}
				return data; 
			}

			private void processFaceData(String fread) {
				String s[] = fread.split("\\s+");
				if (fread.contains("//")) { 
					// Pattern is present if obj has only v and vn in face data
					for (int loop = 1; loop < s.length; loop++) {
						s[loop] = s[loop].replaceAll("//", "/0/"); 
						// insert a zero for missing vt data
					}
				}
				processfIntData(s); // Pass in face data
			}

			private void processfIntData(String sdata[]) {
				int vdata[] = new int[sdata.length - 1];
				int vtdata[] = new int[sdata.length - 1];
				int vndata[] = new int[sdata.length - 1];
				for (int loop = 1; loop < sdata.length; loop++) {
					String s = sdata[loop];
					String[] temp = s.split("/");
					vdata[loop - 1] = Integer.valueOf(temp[0]); 
					// always add vertex indices

					if (temp.length > 1) // if true, we have v and vt data
					{
						vtdata[loop - 1] = Integer.valueOf(temp[1]); 
						// add in vt indices
					} else {
						vtdata[loop - 1] = 0; // if no vt data is present fill in zeros
					}
					if (temp.length > 2) // if true, we have v, vt, and vn data
					{
						vndata[loop - 1] = Integer.valueOf(temp[2]); 
						// add in vn indices
					} else {
						vndata[loop - 1] = 0; 
						// if no vn data is present fill in zeros
					}
				}
				fv.add(vdata);
				ft.add(vtdata);
				fn.add(vndata);
			}

			private void setFaceRenderType() {
				final int temp[] = (int[]) fv.get(0);

				if (temp.length == 3) {
					geometry = GL2.GL_TRIANGLES; 
					// The faces come in sets of 3 so we have triangular faces
				} else if (temp.length == 4) {
					geometry = GL2.GL_QUADS; 
					// The faces come in sets of 4 so we have quadrilateral faces
				} else {
					geometry = GL2.GL_POLYGON; 
					// Fall back to render as free form polygons
				}
			}

		}
		
		FloatBuffer tmpVerticesBuf = null, tmpNormalsBuf=null, tmpTexCoordsBuf= null;
		
		OBJLoader loader = new OBJLoader(modelPath); 
		
		float coords4[] = new float[4];
		if (loader.fv.get(0)[0] > 0) {
			tmpVerticesBuf = Buffers.newDirectFloatBuffer(loader.fv.size() * 4
					* (loader.fv.get(0)).length);
			tmpVerticesBuf.position(0);

			coords4[3] = 1;
			for (int i = 0; i < loader.fv.size(); i++) {
				for (int j = 0; j < ((int[]) loader.fv.get(i)).length; j++) {
					coords4[0] = (float) loader.vData.get(loader.fv.get(i)[j] - 1)[0]; // x
					coords4[1] = (float) loader.vData.get(loader.fv.get(i)[j] - 1)[1]; // y
					coords4[2] = (float) loader.vData.get(loader.fv.get(i)[j] - 1)[2]; // z
					tmpVerticesBuf.put(coords4);
				}

			}
			tmpVerticesBuf.position(0);
		}

		if (loader.ft.get(0)[0] > 0) {
			tmpTexCoordsBuf = Buffers.newDirectFloatBuffer(loader.ft.size() * 2
					* (loader.ft.get(0)).length);
			tmpTexCoordsBuf.position(0);

			for (int i = 0; i < loader.ft.size(); i++) {
				for (int j = 0; j < ((int[]) loader.ft.get(i)).length; j++) {
					tmpTexCoordsBuf
							.put((float) loader.vtData.get(loader.ft.get(i)[j] - 1)[0]);
					tmpTexCoordsBuf
							.put((float) loader.vtData.get(loader.ft.get(i)[j] - 1)[1]);
				}
			}
			tmpTexCoordsBuf.position(0);
		}

		float coords3[] = new float[3];
		if (loader.fn.get(0)[0] > 0) {
			tmpNormalsBuf = Buffers.newDirectFloatBuffer(loader.fn.size() * 3
					* (loader.fn.get(0)).length);
			tmpNormalsBuf.position(0);

			for (int i = 0; i < loader.fn.size(); i++) {
				for (int j = 0; j < ((int[]) loader.fn.get(i)).length; j++) {
					coords3[0] = (float) loader.vnData.get(loader.fn.get(i)[j] - 1)[0]; // x
					coords3[1] = (float) loader.vnData.get(loader.fn.get(i)[j] - 1)[1]; // y
					coords3[2] = (float) loader.vnData.get(loader.fn.get(i)[j] - 1)[2]; // z
					tmpNormalsBuf.put(coords3);
				}
			}
			tmpNormalsBuf.position(0);
		}
		
		buffer = toOGLBuuffers(gl, tmpVerticesBuf, tmpNormalsBuf, tmpTexCoordsBuf);
	}

	
	private OGLBuffers toOGLBuuffers(GL2 gl, FloatBuffer verticesBuf, FloatBuffer normalsBuf, FloatBuffer texCoordsBuf){
		OGLBuffers buffers;
		
		if (verticesBuf != null) {
			OGLBuffers.Attrib[] attributesPos = {
					new OGLBuffers.Attrib("inPosition", 4),
			};
			float[] floatArray = new float[verticesBuf.limit()];
			verticesBuf.get(floatArray);
	        buffers = new OGLBuffers(gl,floatArray, attributesPos, null);
		}
		else
			return null;

		if (texCoordsBuf != null) {
			OGLBuffers.Attrib[] attributesTexCoord = {
					new OGLBuffers.Attrib("inTexCoord", 2)
			};
			float[] floatArray = new float[texCoordsBuf.limit()];
			texCoordsBuf.get(floatArray);
			buffers.addVertexBuffer(floatArray, attributesTexCoord);
		}
			
		if (normalsBuf != null) {
			OGLBuffers.Attrib[] attributesNormal = {
					new OGLBuffers.Attrib("inNormal", 3)
			};
			float[] floatArray = new float[normalsBuf.limit()];
			normalsBuf.get(floatArray);
			buffers.addVertexBuffer(floatArray, attributesNormal);
		}
			
		return buffers;
	}

}