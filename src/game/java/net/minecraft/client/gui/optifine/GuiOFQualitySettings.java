package net.minecraft.client.gui.optifine;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class GuiOFQualitySettings extends GuiOFSettingsScreen {
	public GuiOFQualitySettings(GuiScreen parent) {
		super(parent, "of.quality.title");
	}

	protected void afterInit() {
		if (EaglercraftGPU.checkOpenGLESVersion() < 300) {
			GuiButton btn = buttonFor(GameSettings.Options.MIPMAP_LEVELS);
			if (btn != null) {
				btn.displayString = I18n.format(GameSettings.Options.MIPMAP_LEVELS.getEnumString(), new Object[0]) + ": N/A";
				btn.enabled = false;
			}
		}
	}

	protected void buildEntries() {
		option(GameSettings.Options.OF_RESOLUTION_SCALE);
		option(GameSettings.Options.MIPMAP_LEVELS);
		option(GameSettings.Options.OF_CLEAR_WATER);
		option(GameSettings.Options.OF_BETTER_GRASS);
		option(GameSettings.Options.OF_CUSTOM_FONTS);
		option(GameSettings.Options.OF_CONNECTED_TEXTURES);
		option(GameSettings.Options.OF_CUSTOM_SKIES);
		option(GameSettings.Options.OF_CUSTOM_ITEMS);
	}
}