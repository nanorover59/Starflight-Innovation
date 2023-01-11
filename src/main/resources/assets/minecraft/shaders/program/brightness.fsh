#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() 
{
	float threshold = 0.1;
	vec3 color = texture(DiffuseSampler, texCoord).rgb;
    float brightness = dot(color, vec3(0.2126, 0.7152, 0.0722));
    float intensity = max(0.0, pow((brightness - 0.25) / threshold, 2.0));
    float clampedIntensity = min(1.0, intensity);
	
	if(brightness >= threshold)
		fragColor = vec4(color * clampedIntensity, 1.0);
	else
		fragColor = vec4(0.0, 0.0, 0.0, 1.0);
}