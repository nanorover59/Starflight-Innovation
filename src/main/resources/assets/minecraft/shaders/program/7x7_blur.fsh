#version 150

#define KERNEL_SIZE 7

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 BlurDir;

out vec4 fragColor;

const float kernel[KERNEL_SIZE] = float[](0.1216, 0.1420, 0.1559, 0.1609, 0.1559, 0.1420, 0.1216);

void main()
{
	vec3 color = texture(DiffuseSampler, texCoord).rgb;
	vec3 finalColor = vec3(0.0);
  
	for(int i = 0; i < KERNEL_SIZE; i++)
	{
		vec2 offset = oneTexel * (i - (KERNEL_SIZE - 1) / 2) * BlurDir;
		finalColor += texture(DiffuseSampler, texCoord + offset).rgb * kernel[i];
	}
  
	fragColor = vec4(finalColor, 1.0);
}