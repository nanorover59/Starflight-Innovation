#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D BloomSampler;

uniform float BloomIntensity;

in vec2 texCoord;

out vec4 fragColor;

void main() 
{
	vec3 color = texture(DiffuseSampler, texCoord).rgb;
	vec3 bloom = texture(BloomSampler, texCoord).rgb * BloomIntensity;
	
	if(bloom.x == 0.0 && bloom.y == 0.0 && bloom.z == 0.0)
		fragColor = vec4(color, 1.0);
	else
	{
		float colorIntensity = length(color);
		vec3 finalColor = color + (bloom * pow(0.08, colorIntensity));
		fragColor = vec4(finalColor, 1.0);
	}
}