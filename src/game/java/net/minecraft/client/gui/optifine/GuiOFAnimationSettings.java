package net.minecraft.client.gui.optifine;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

public class GuiOFAnimationSettings extends GuiOFSettingsScreen {
	public GuiOFAnimationSettings(GuiScreen parent) {
		super(parent, "of.animation.title");
	}

	protected boolean hasAllButtons() {
		return true;
	}

	protected void onAllButton(boolean on) {
		this.mc.gameSettings.setAllAnimatedOF(on);
	}

	protected void buildEntries() {
		option(GameSettings.Options.OF_WATER_ANIMATED);
		option(GameSettings.Options.OF_LAVA_ANIMATED);
		option(GameSettings.Options.OF_FIRE_ANIMATED);
		option(GameSettings.Options.OF_PORTAL_ANIMATED);
		option(GameSettings.Options.OF_REDSTONE_ANIMATED);
		option(GameSettings.Options.OF_EXPLOSION_ANIMATED);
		option(GameSettings.Options.OF_FLAME_ANIMATED);
		option(GameSettings.Options.OF_SMOKE_ANIMATED);
		option(GameSettings.Options.OF_VOID_PARTICLES);
		option(GameSettings.Options.OF_WATER_PARTICLES);
		option(GameSettings.Options.OF_RAIN_SPLASH);
		option(GameSettings.Options.OF_PORTAL_PARTICLES);
		option(GameSettings.Options.OF_POTION_PARTICLES);
		option(GameSettings.Options.OF_DRIPPING);
		option(GameSettings.Options.OF_TERRAIN_ANIMATED);
		option(GameSettings.Options.OF_TEXTURES_ANIMATED);
		option(GameSettings.Options.OF_FIREWORK_PARTICLES);
		option(GameSettings.Options.ENCHANTMENT_GLINT);
		option(GameSettings.Options.PARTICLES);
	}
}