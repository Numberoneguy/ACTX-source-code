package net.lax1dude.eaglercraft.v1_8.opengl.ext.deferred;

import net.minecraft.client.Minecraft;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;

public class EaglerDeferredPipeline {
    public static net.lax1dude.eaglercraft.v1_8.internal.buffer.FloatBuffer matrixCopyBuffer = null;
    public static final Logger logger = LogManager.getLogger("EaglerDeferredPipeline");
    public static boolean isSuspended = true;
    public static EaglerDeferredPipeline instance = new EaglerDeferredPipeline(null);
    public final EaglerDeferredConfig config = new EaglerDeferredConfig();

    public int sunOcclusionValueTexture = -1;
    public int exposureBlendTexture = -1;
    public GBufferAcceleratedEffectRenderer gbufferEffectRenderer = new GBufferAcceleratedEffectRenderer();
    public ForwardAcceleratedEffectRenderer forwardEffectRenderer = new ForwardAcceleratedEffectRenderer();

    public EaglerDeferredPipeline() {}
    public EaglerDeferredPipeline(Minecraft mc) {}

    public static boolean isSupported() { return false; }
    public float getPartialTicks() { return 0.0f; }
    public void setPartialTicks(float f) {}
    public void bindLightSourceBucket(int a, int b, int c, int d) {}
    public static <U, M> void uniformMatrixHelper(U u, M m) {}
    public void rebuild(EaglerDeferredConfig c) {}
    public void destroy() {}
    public void resetContextStateAfterException() {}
    public void setForwardRenderLightFactors(float a, float b, float c, float d) {}
    
    public void beginDrawDeferred() {}
    public void endDrawDeferred() {}
    public void beginDrawMainGBuffer() {}
    public void endDrawMainGBuffer() {}
    public void beginDrawMainGBufferTerrain() {}
    public void beginDrawMainGBufferEntities() {}
    public void beginDrawMainShadowMap() {}
    public void endDrawMainShadowMap() {}
    public void beginDrawMainShadowMapLOD(int i) {}
    public void flushLights() {}
    public void beginDrawColoredShadows() {}
    public void endDrawColoredShadows() {}
    public void combineGBuffersAndIlluminate() {}
    public void beginDrawEnvMap() {}
    public void endDrawEnvMap() {}
    public void beginDrawEnvMapTop(float f) {}
    public void beginDrawEnvMapSolid() {}
    public void beginDrawEnvMapTranslucent() {}
    public void beginDrawEnvMapBottom(float f) {}
    public void beginDrawRealisticWaterMask() {}
    public void endDrawRealisticWaterMask() {}
    public void beginDrawHDRTranslucent() {}
    public void endDrawHDRTranslucent() {}
    public void beginDrawRealisticWaterSurface() {}
    public void endDrawRealisticWaterSurface() {}
    public void applyGBufferFog() {}
    public void beginDrawTranslucentEntities() {}
    public void beginDrawTranslucentBlocks() {}
    public void beginDrawMainGBufferDestroyProgress() {}
    public void endDrawMainGBufferDestroyProgress() {}
    public void beginDrawGlassHighlights() {}
    public void endDrawGlassHighlights() {}
    public void saveReprojData() {}
    public void beginDrawHandOverlay() {}
    public void endDrawHandOverlay() {}
    public void loadViewMatrix() {}
    public void setRenderPosGlobal(double a, double b, double c) {}
    public void updateReprojectionCoordinates(double a, double b, double c) {}

    public int atmosphereIrradianceTexture = -1;
    public int[] gBufferDrawBuffers = new int[0];
    public static boolean testAabSphere(int a, int b, int c, int d, int e, int f, float g, float h, float i, float j) { return false; }
    public static void renderSuspended() { }
}
