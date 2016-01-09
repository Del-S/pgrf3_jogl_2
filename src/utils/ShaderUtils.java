package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;

public final class ShaderUtils {

	public static final String VERTEX_SHADER_EXTENSION = ".vert";
	public static final String TESS_CONTROL_SHADER_EXTENSION = ".tesc";
	public static final String TESS_EVALUATION_SHADER_EXTENSION = ".tese";
	public static final String GEOMETRY_SHADER_EXTENSION = ".geom";
	public static final String FRAGMENT_SHADER_EXTENSION = ".frag";
	public static final String COMPUTE_SHADER_EXTENSION = ".comp";

	public static int loadProgram(GL2 gl, String shaderFileName) {
		return loadProgram(gl, shaderFileName, shaderFileName, shaderFileName,
				shaderFileName, shaderFileName);
	}

	public static int loadProgram(GL2 gl, String vertexShaderFileName,
			String fragmentShaderFileName, String geometryShaderFileName,
			String tessControlShaderFileName,
			String tessEvaluationShaderFileName) {

		String extensions = gl.glGetString(GL3.GL_EXTENSIONS);
		int shaderProgram = gl.glCreateProgram();

		// vertex shader
		int vs = readAndCompileShaderProgram(gl, vertexShaderFileName,
				VERTEX_SHADER_EXTENSION, GL2.GL_VERTEX_SHADER);
		if (vs > 0){
			if (extensions.indexOf("GL_ARB_vertex_shader") == -1)
			throw new RuntimeException(
					"Vertex shader not supported by OpenGL driver.");
			gl.glAttachShader(shaderProgram, vs);
		}
		
		// fragment shader
		int fs = readAndCompileShaderProgram(gl, fragmentShaderFileName,
				FRAGMENT_SHADER_EXTENSION, GL2.GL_FRAGMENT_SHADER);
		if (fs > 0) {
			if (extensions.indexOf("GL_ARB_fragment_shader") == -1)
			throw new RuntimeException(
					"Fragment shader not supported by OpenGL driver.");
			gl.glAttachShader(shaderProgram, fs);
		}
		
		// geometry shader
		int gs = readAndCompileShaderProgram(gl, geometryShaderFileName,
				GEOMETRY_SHADER_EXTENSION, GL3.GL_GEOMETRY_SHADER);
		if (gs > 0){
			if (extensions.indexOf("GL_ARB_geometry_shader4") == -1)
			throw new RuntimeException(
					"Geometry shader not supported by OpenGL driver.");
			gl.glAttachShader(shaderProgram, gs);
		}
		
		// control (hull) shader
		int cs = readAndCompileShaderProgram(gl, tessControlShaderFileName,
				TESS_CONTROL_SHADER_EXTENSION, GL4.GL_TESS_CONTROL_SHADER);
		if (cs > 0){
			if (extensions.indexOf("GL_ARB_tessellation_shader ") == -1)
			throw new RuntimeException(
					"Control shader not supported by OpenGL driver.");
			gl.glAttachShader(shaderProgram, cs);
		}
		
		// evaluation (domain) shader
		int es = readAndCompileShaderProgram(gl, tessEvaluationShaderFileName,
				TESS_EVALUATION_SHADER_EXTENSION, GL4.GL_TESS_EVALUATION_SHADER);
		if (es > 0){
			if (extensions.indexOf("GL_ARB_tessellation_shader ") == -1)
			throw new RuntimeException(
					"Control shader not supported by OpenGL driver.");
			gl.glAttachShader(shaderProgram, es);
		}
		linkProgram(gl, shaderProgram);
		return shaderProgram;
	}

	static public int readAndCompileShaderProgram(GL2 gl, String shaderFileName,
			String shaderFileExtension, int type) {
		String error;
		if (shaderFileName == null) {
			return -1;
		}
		String name = shaderFileName + shaderFileExtension;
		File shaderFile = new File(name);
        if(!shaderFile.exists()) 
            return -1;
        
        System.out.print("Shader: " + name + " ");
		
        String[] shaderSrc = readShaderFromFile(name);

		if (shaderSrc == null)
			return -1;

		int shader = gl.glCreateShader(type);
		if (shader == 0) {
			System.out.println("Shader not supported");
			return -1;
		}
		gl.glShaderSource(shader, shaderSrc.length, shaderSrc, (int[]) null, 0);

		System.out.print("Compiling ... ");
		gl.glCompileShader(shader);
		error = checkLogInfo(gl, shader, GL2.GL_COMPILE_STATUS);
		if (error == null) {
			System.out.println("OK");
			return shader;
		} else {
			System.out.println("failed");
			System.out.println("\n" + error);
			return -1;
		}

	}

	static public void linkProgram(GL2 gl, int shaderProgram) {
		String error;
		System.out.print("Linking shader program ... ");
		gl.glLinkProgram(shaderProgram);
		error = checkLogInfo(gl, shaderProgram, GL2.GL_LINK_STATUS);
		if (error == null)
			System.out.println("OK");
		else {
			System.out.println("failed");
			System.out.println("\n" + error);
		}
	}

	static private String[] readShaderFromFile(String shaderFileName) {
		BufferedReader brv = null;
		try {
			brv = new BufferedReader(new FileReader(shaderFileName));
		} catch (FileNotFoundException e) {
			System.out.println("File not found " + shaderFileName);
			// e.printStackTrace();
			return null;
		}

		String line;
		int index;
		ArrayList<String> shader = new ArrayList<>();
		try {
			while ((line = brv.readLine()) != null) {
				index = line.indexOf("//");
				if (index > 0)
					line = line.substring(0, index);
				shader.add(new String(line + "\n"));
			}
			brv.close();
		} catch (IOException e) {
			System.out.println("Read error in " + shaderFileName);
			e.printStackTrace();
		}
		
		String[] result = new String[shader.size()];
		return shader.toArray(result);
	}

	static private String checkLogInfo(GL2 gl, int programObject, int mode) {
		switch (mode) {
		case GL2.GL_COMPILE_STATUS:
			return checkLogInfoShader(gl, programObject, mode);
		case GL2.GL_LINK_STATUS:
		case GL2.GL_VALIDATE_STATUS:
			return checkLogInfoProgram(gl, programObject, mode);
		default:
			return "Unsupported mode.";
		}
	}

	static private String checkLogInfoShader(GL2 gl, int programObject, int mode) {
		int[] error = new int[] { -1 };
		gl.glGetShaderiv(programObject, mode, error, 0);
		if (error[0] != GL3.GL_TRUE) {
			int[] len = new int[1];
			gl.glGetShaderiv(programObject, GL2.GL_INFO_LOG_LENGTH, len, 0);
			if (len[0] == 0) {
				return null;
			}
			byte[] errorMessage = new byte[len[0]];
			gl.glGetShaderInfoLog(programObject, len[0], len, 0, errorMessage,
					0);
			return new String(errorMessage, 0, len[0]);
		}
		return null;
	}

	static private String checkLogInfoProgram(GL2 gl, int programObject,
			int mode) {
		int[] error = new int[] { -1 };
		gl.glGetProgramiv(programObject, mode, error, 0);
		if (error[0] != GL3.GL_TRUE) {
			int[] len = new int[1];
			gl.glGetProgramiv(programObject, GL2.GL_INFO_LOG_LENGTH, len, 0);
			if (len[0] == 0) {
				return null;
			}
			byte[] errorMessage = new byte[len[0]];
			gl.glGetProgramInfoLog(programObject, len[0], len, 0, errorMessage,
					0);
			return new String(errorMessage, 0, len[0]);
		}
		return null;
	}

}
