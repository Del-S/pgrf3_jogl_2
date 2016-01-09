#version 330
in vec2 inPosition; // vstup z vertex bufferu
in vec3 inColor; // vstup z vertex bufferu
out vec3 vertColor; // vystup do dalsich casti retezce
out vec2 vertInPosition;

void main() {
	vertInPosition = inPosition;
	gl_Position = vec4(inPosition, 0.0, 1.0); 
} 
