package space.client;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import space.entity.AirshipEntity;
import space.entity.RocketEntity;
import space.mixin.client.KeyBindingInvokerMixin;
import space.network.c2s.AirshipInputC2SPacket;
import space.network.c2s.RocketInputC2SPacket;

public class StarflightControls
{	
	public static void vehicleControls(MinecraftClient client)
	{
		if(client.player != null && client.player.hasVehicle())
		{
			if(client.player.getVehicle() instanceof RocketEntity)
			{
				int throttleState = 0;
				int rollState = 0;
				int pitchState = 0;
				int yawState = 0;
				int stopState = 0;
				int xState = 0;
				int yState = 0;
				int zState = 0;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT))
					throttleState++;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL))
					throttleState--;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_Z))
					throttleState = 2;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_X))
					throttleState = -2;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_A))
					rollState++;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_D))
					rollState--;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_W))
					pitchState++;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_S))
					pitchState--;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_Q))
					yawState++;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_E))
					yawState--;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_J))
					xState++;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_L))
					xState--;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_I))
					zState++;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_K))
					zState--;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_U))
					yState++;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_O))
					yState--;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_SPACE))
					stopState++;
				
				for(KeyBinding key : client.options.allKeys)
				{
					if(!key.equals(client.options.fullscreenKey) && !key.equals(client.options.togglePerspectiveKey) && !key.equals(client.options.screenshotKey))
						((KeyBindingInvokerMixin) key).callReset();
				}
				
				ClientPlayNetworking.send(new RocketInputC2SPacket(new int[] {throttleState, rollState, pitchState, yawState, xState, yState, zState, stopState}));
			}
			else if(client.player.getVehicle() instanceof AirshipEntity)
			{
				int elevationState = 0;
				int forwardReverseState = 0;
				int lateralState = 0;
				int rotationState = 0;
				int stopState = 0;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT))
					elevationState++;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL))
					elevationState--;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_A))
					rotationState++;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_D))
					rotationState--;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_W))
					forwardReverseState++;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_S))
					forwardReverseState--;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_Q))
					lateralState++;
	
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_E))
					lateralState--;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_SPACE))
					stopState++;
				
				for(KeyBinding key : client.options.allKeys)
				{
					if(!key.equals(client.options.fullscreenKey) && !key.equals(client.options.togglePerspectiveKey) && !key.equals(client.options.screenshotKey))
						((KeyBindingInvokerMixin) key).callReset();
				}
				
				ClientPlayNetworking.send(new AirshipInputC2SPacket(new int[] {elevationState, forwardReverseState, lateralState, rotationState, stopState}));
			}
		}
	}
}