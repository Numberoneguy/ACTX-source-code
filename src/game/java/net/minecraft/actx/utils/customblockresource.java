package net.minecraft.actx.utils;
public class customblockresource {
    public final String blockstatepath;
    public final byte[] blockstatejson;
    public final String modelpath;
    public final byte[] modeljson;
    public final String texturepath;
    public final byte[] texturebytes;
    public final String mcmetapath;
    public final byte[] mcmetabytes;
    public customblockresource(String blockstatepath, byte[] blockstatejson,
                                String modelpath, byte[] modeljson,
                                String texturepath, byte[] texturebytes,
                                String mcmetapath, byte[] mcmetabytes) {
        this.blockstatepath = blockstatepath;
        this.blockstatejson = blockstatejson;
        this.modelpath = modelpath;
        this.modeljson = modeljson;
        this.texturepath = texturepath;
        this.texturebytes = texturebytes;
        this.mcmetapath = mcmetapath;
        this.mcmetabytes = mcmetabytes;
    }
    public customblockresource(String blockstatepath, byte[] blockstatejson,
                                String modelpath, byte[] modeljson,
                                String texturepath, byte[] texturebytes) {
        this(blockstatepath, blockstatejson, modelpath, modeljson, texturepath, texturebytes, null, null);
    }
}
