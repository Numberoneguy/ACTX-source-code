package net.minecraft.actx.utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class customresourceregistry {
    private static final List<customblockresource> resources = new ArrayList<>();
    public static void register(customblockresource resource) {
        if (resourcenamespaces.isVanilla(resource.blockstatepath)
                || resourcenamespaces.isVanilla(resource.modelpath)
                || resourcenamespaces.isVanilla(resource.texturepath)
                || (resource.mcmetapath != null && resourcenamespaces.isVanilla(resource.mcmetapath))) {
            System.err.println("[CustomResourceRegistry] Refused to register a resource under the vanilla "
                    + "'minecraft' namespace - prefix all of its paths with your own mod namespace "
                    + "(e.g. 'actx:blocks/thing'). Nothing was registered for: "
                    + resource.blockstatepath);
            return;
        }
        resources.add(resource);
    }
    public static List<customblockresource> getAll() {
        return Collections.unmodifiableList(resources);
    }
}
