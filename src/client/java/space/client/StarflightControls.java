package space.client;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import space.entity.RocketEntity;
import space.mixin.client.KeyBindingInvokerMixin;
import space.network.c2s.RocketInputC2SPacket;

public class StarflightControls
{
	/*private static KeyBinding throttleUp;
	private static KeyBinding throttleDown;
	private static KeyBinding throttleMax;
	private static KeyBinding throttleMin;
	private static KeyBinding rollCCW;
	private static KeyBinding rollCW;
	private static KeyBinding pitchUp;
	private static KeyBinding pitchDown;
	private static KeyBinding yawLeft;
	private static KeyBinding yawRight;
	private static KeyBinding xPositive;
	private static KeyBinding xNegative;
	private static KeyBinding yPositive;
	private static KeyBinding yNegative;
	private static KeyBinding zPositive;
	private static KeyBinding zNegative;
	private static KeyBinding stop;

	public static void registerKeyBindings()
	{
		throttleUp = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, "key.category.space"));
		throttleDown = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, "key.category.space"));
		throttleMax = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_max", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.category.space"));
		throttleMin = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_min", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.category.space"));
		rollCCW = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.roll_ccw", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Q, "key.category.space"));
		rollCW = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.roll_cw", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_E, "key.category.space"));
		pitchUp = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.pitch_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_W, "key.category.space"));
		pitchDown = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.pitch_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_S, "key.category.space"));
		yawLeft = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.yaw_left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_A, "key.category.space"));
		yawRight = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.yaw_right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_D, "key.category.space"));
		xPositive = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.x_positive", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.category.space"));
		xNegative = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.x_negative", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, "key.category.space"));
		yPositive = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.y_positive", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "key.category.space"));
		yNegative = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.y_negative", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.category.space"));
		zPositive = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.z_positive", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.category.space"));
		zNegative = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.z_negative", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.category.space"));
		stop = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.stop", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, "key.category.space"));
	}*/
	
	public static void vehicleControls(MinecraftClient client)
	{
		if(client.player != null && client.player.hasVehicle() && client.player.getVehicle() instanceof RocketEntity)
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
	}
}