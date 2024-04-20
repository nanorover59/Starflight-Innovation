#version 150

uniform sampler2D DiffuseSampler;

uniform vec2 FocusPoint;
uniform float Strength;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main()
{
	vec2 direction = texCoord - FocusPoint;
	float red = texture(DiffuseSampler, texCoord + (direction * vec2(0.009 * Strength))).r;
	float green = texture(DiffuseSampler, texCoord + (direction * vec2(0.006 * Strength))).g;
	float blue = texture(DiffuseSampler, texCoord + (direction * vec2(-0.006 * Strength))).b;
	fragColor = vec4(red, green, blue, 1.0);
}