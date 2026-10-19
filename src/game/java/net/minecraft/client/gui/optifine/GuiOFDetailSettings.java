package net.minecraft.client.gui.optifine;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

public class GuiOFDetailSettings extends GuiOFSettingsScreen {
	public GuiOFDetailSettings(GuiScreen parent) {
		super(parent, "of.detail.title");
	}

	protected void buildEntries() {
		option(GameSettings.Options.OF_CLOUD_HEIGHT);
		option(GameSettings.Options.OF_FOG_START);
		option(GameSettings.Options.RENDER_CLOUDS);
		option(GameSettings.Options.OF_SMART_LEAVES);
		option(GameSettings.Options.RENDER_WEATHER);
		option(GameSettings.Options.OF_SKY);
		option(GameSettings.Options.OF_STARS);
		option(GameSettings.Options.OF_SUN_MOON);
		option(GameSettings.Options.FNAW_SKINS);
		option(GameSettings.Options.FOG);
		option(GameSettings.Options.OF_TRANSLUCENT);
		option(GameSettings.Options.OF_HELD_TOOLTIPS);
		option(GameSettings.Options.OF_DROPPED_ITEMS);
		option(GameSettings.Options.ENTITY_SHADOWS);
		option(GameSettings.Options.OF_SWAMP_COLORS);
		option(GameSettings.Options.OF_SMOOTH_BIOMES);
		option(GameSettings.Options.OF_VIGNETTE);
		option(GameSettings.Options.ENCHANTMENT_GLINT);
	}
}