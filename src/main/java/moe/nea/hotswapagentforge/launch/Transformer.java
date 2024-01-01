package moe.nea.hotswapagentforge.launch;

import net.minecraft.launchwrapper.IClassTransformer;

/**
 * Noop transformer that allows us to be informed when a class is loaded. This allows us to not cause crashes by firing
 * events before forge is initialized.
 */
public class Transformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if ("moe.nea.hotswapagentforge.forge.HotswapEvent".equals(name)) {
            Tweaker.eventsLoaded = true;
        }
        if ("net.minecraftforge.common.MinecraftForge".equals(name)) {
            Tweaker.forgeLoaded = true;
        }
        return basicClass;
    }
}
