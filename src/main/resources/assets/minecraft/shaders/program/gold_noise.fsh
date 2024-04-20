#version 150

uniform sampler2D DiffuseSampler;

uniform float Strength;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

float GOLDEN_RATIO = 1.61803398874989484820459; 

float gold_noise(in vec2 xy, in float seed)
{
	return fract(tan(distance(xy * GOLDEN_RATIO, xy) * seed) * xy.x);
}

void main()
{
	vec2 direction = texCoord - vec2(0.5, 0.5);
	float red = texture(DiffuseSampler, texCoord + (direction * vec2(0.009 * Strength))).r;
	float green = texture(DiffuseSampler, texCoord + (direction * vec2(0.006 * Strength))).g;
	float blue = texture(DiffuseSampler, texCoord + (direction * vec2(-0.006 * Strength))).b;
	fragColor = vec4(red, green, blue, 1.0);
}