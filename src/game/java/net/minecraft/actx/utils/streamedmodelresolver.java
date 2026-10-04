package net.minecraft.actx.utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class streamedmodelresolver {
    private static final Map<ResourceLocation, String> pendingmodels = new HashMap<>();
    private static final Map<ResourceLocation, Set<ResourceLocation>> modelwaitingontextures = new HashMap<>();
    private static final Map<ResourceLocation, String> pendingblockstates = new HashMap<>();
    private static final Map<ResourceLocation, Set<ResourceLocation>> blockstatewaitingonmodels = new HashMap<>();
    private static final Set<ResourceLocation> readytextures = new HashSet<>();
    private static final Set<ResourceLocation> readymodels = new HashSet<>();
    public static void onTextureReady(ResourceLocation textureloc) {
        readytextures.add(textureloc);
        for (ResourceLocation modelloc : new HashSet<>(modelwaitingontextures.keySet())) {
            Set<ResourceLocation> needed = modelwaitingontextures.get(modelloc);
            needed.remove(textureloc);
            if (needed.isEmpty()) {
                modelwaitingontextures.remove(modelloc);
                finalizeModel(modelloc, pendingmodels.remove(modelloc));
            }
        }
    }
    @SuppressWarnings("unchecked")
    public static void onModelReceived(ResourceLocation modelloc, String modeljson) {
        Set<ResourceLocation> needed = new HashSet<>();
        try {
            Object parsed = minijson.parse(modeljson);
            if (!(parsed instanceof Map)) {
                return;
            }
            Map<String, Object> root = (Map<String, Object>) parsed;
            Object texturesobj = root.get("textures");
            if (texturesobj instanceof Map) {
                Map<String, Object> textures = (Map<String, Object>) texturesobj;
                for (Object valueobj : textures.values()) {
                    if (!(valueobj instanceof String)) continue;
                    String value = (String) valueobj;
                    if (value.startsWith("#")) {
                        continue;
                    }
                    ResourceLocation texloc = normalize(value);
                    if (!readytextures.contains(texloc)) {
                        needed.add(texloc);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[StreamedModelResolver] Failed to parse model " + modelloc + ": " + e.getMessage());
            return;
        }
        if (needed.isEmpty()) {
            finalizeModel(modelloc, modeljson);
        } else {
            pendingmodels.put(modelloc, modeljson);
            modelwaitingontextures.put(modelloc, needed);
        }
    }
    @SuppressWarnings("unchecked")
    public static void onBlockstateReceived(ResourceLocation blockstateloc, String blockstatejson) {
        Set<ResourceLocation> needed = new HashSet<>();
        try {
            Object parsed = minijson.parse(blockstatejson);
            if (!(parsed instanceof Map)) {
                return;
            }
            Map<String, Object> root = (Map<String, Object>) parsed;
            Object variantsobj = root.get("variants");
            if (variantsobj instanceof Map) {
                Map<String, Object> variants = (Map<String, Object>) variantsobj;
                for (Object variantvalue : variants.values()) {
                    for (Map<String, Object> variant : asVariantObjects(variantvalue)) {
                        Object modelobj = variant.get("model");
                        if (!(modelobj instanceof String)) continue;
                        ResourceLocation modelloc = normalize((String) modelobj);
                        if (!readymodels.contains(modelloc)) {
                            needed.add(modelloc);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[StreamedModelResolver] Failed to parse blockstate " + blockstateloc + ": " + e.getMessage());
            return;
        }
        if (needed.isEmpty()) {
            finalizeBlockstate(blockstateloc, blockstatejson);
        } else {
            pendingblockstates.put(blockstateloc, blockstatejson);
            blockstatewaitingonmodels.put(blockstateloc, needed);
        }
    }
    private static void finalizeModel(ResourceLocation modelloc, String modeljson) {
        readymodels.add(modelloc);
        for (ResourceLocation stateloc : new HashSet<>(blockstatewaitingonmodels.keySet())) {
            Set<ResourceLocation> needed = blockstatewaitingonmodels.get(stateloc);
            needed.remove(modelloc);
            if (needed.isEmpty()) {
                blockstatewaitingonmodels.remove(stateloc);
                finalizeBlockstate(stateloc, pendingblockstates.remove(stateloc));
            }
        }
    }
    private static void finalizeBlockstate(ResourceLocation blockstateloc, String blockstatejson) {
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asVariantObjects(Object element) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (element instanceof List) {
            for (Object e : (List<Object>) element) {
                if (e instanceof Map) {
                    result.add((Map<String, Object>) e);
                }
            }
        } else if (element instanceof Map) {
            result.add((Map<String, Object>) element);
        }
        return result;
    }
    private static ResourceLocation normalize(String path) {
        return new ResourceLocation(path.contains(":") ? path : "minecraft:" + path);
    }
}
