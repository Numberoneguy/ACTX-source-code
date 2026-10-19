package net.minecraft.client.renderer.texture;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

public class OFFontTexture extends SimpleTexture {

	private final GameSettings gameSettings;

	public OFFontTexture(ResourceLocation location, GameSettings gameSettings) {
		super(location);
		this.gameSettings = gameSettings;
	}

	public void loadTexture(IResourceManager resourceManager) throws IOException {
		super.loadTexture(this.gameSettings.customFontsOF ? resourceManager : new DefaultPackOnly(resourceManager));
	}

	private static class DefaultPackOnly implements IResourceManager {

		private final IResourceManager parent;

		DefaultPackOnly(IResourceManager parent) {
			this.parent = parent;
		}

		public Set<String> getResourceDomains() {
			return parent.getResourceDomains();
		}

		public IResource getResource(ResourceLocation location) throws IOException {
			return parent.getAllResources(location).get(0);
		}

		public List<IResource> getAllResources(ResourceLocation location) throws IOException {
			return parent.getAllResources(location);
		}
	}
}
