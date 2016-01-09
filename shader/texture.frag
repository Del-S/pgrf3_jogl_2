#version 330
in vec2 vertInPosition;
out vec4 outColor;
uniform sampler2D texture;

void main() {
    vec2 newPosition = (vertInPosition+1)/2; //prepocet do texturovacich souradnic
    outColor=texture2D(texture, newPosition);
} 
