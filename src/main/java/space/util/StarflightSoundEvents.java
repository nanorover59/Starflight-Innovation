package space.util;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public class StarflightSoundEvents
{
	public static SoundEvent CURRENT_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "current"));
	public static SoundEvent WRENCH_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "wrench"));
	public static SoundEvent STORAGE_CUBE_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "storage_cube"));
	public static SoundEvent ROCKET_ENGINE_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "rocket_engine"));
	public static SoundEvent AIRSHIP_MOTOR_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "airship_motor"));
	public static SoundEvent LEAK_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "leak"));
	public static SoundEvent ELECTRIC_MOTOR_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "electric_motor"));
	public static SoundEvent MARS_WIND_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "mars_wind"));
	public static SoundEvent NOISE_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "noise"));
	
	public static void initializeSounds()
	{
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "current"), CURRENT_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "wrench"), WRENCH_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "storage_cube"), STORAGE_CUBE_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "rocket_engine"), ROCKET_ENGINE_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "airship_motor"), AIRSHIP_MOTOR_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "leak"), LEAK_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "electric_motor"), ELECTRIC_MOTOR_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "mars_wind"), MARS_WIND_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "noise"), NOISE_SOUND_EVENT);
	}
}