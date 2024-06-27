#version 150

uniform sampler2D DiffuseSampler;

uniform float Seed;
uniform float Threshold;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

float GOLDEN_RATIO = 1.6180339887498948482045868343656381177203091798057628621354486227052604628189; 

float gold_noise(in vec2 xy, in float seed)
{
	return fract(100000.0 * tan(distance(xy * GOLDEN_RATIO, xy) * seed) * distance(xy, vec2(0.5, 0.5)));
}

void main()
{
	vec4 color = texture(DiffuseSampler, texCoord);
	float seed0 = fract(Seed);
	float seed1 = fract(gold_noise(texCoord, seed0));
	float seed2 = fract(gold_noise(texCoord, seed1));
	float noise = gold_noise(texCoord, seed2);
	
	if(noise > Threshold)
		fragColor = color + vec4(noise, noise, noise, 1.0);
	else
		fragColor = color;
}