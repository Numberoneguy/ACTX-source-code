package net.minecraft.client.gui.optifine;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

public class GuiOFPerformanceSettings extends GuiOFSettingsScreen {
	public GuiOFPerformanceSettings(GuiScreen parent) {
		super(parent, "of.performance.title");
	}

	protected void buildEntries() {
		option(GameSettings.Options.CHUNK_FIX);
		option(GameSettings.Options.AMBIENT_OCCLUSION);
	}
}
