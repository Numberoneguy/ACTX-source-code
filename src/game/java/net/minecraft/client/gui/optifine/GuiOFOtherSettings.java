package net.minecraft.client.gui.optifine;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

public class GuiOFOtherSettings extends GuiOFSettingsScreen {
	public GuiOFOtherSettings(GuiScreen parent) {
		super(parent, "of.other.title");
	}

	protected void buildEntries() {
		option(GameSettings.Options.AMBIENT_OCCLUSION);
		option(GameSettings.Options.HUD_FPS);
		option(GameSettings.Options.HUD_COORDS);
		option(GameSettings.Options.HUD_PLAYER);
		option(GameSettings.Options.HUD_STATS);
		option(GameSettings.Options.HUD_WORLD);
		option(GameSettings.Options.HUD_24H);
		option(GameSettings.Options.ANAGLYPH);
		option(GameSettings.Options.OF_DYNAMIC_FOV);
	}
}