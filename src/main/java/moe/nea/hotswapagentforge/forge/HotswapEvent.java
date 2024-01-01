package moe.nea.hotswapagentforge.forge;

import moe.nea.hotswapagentforge.launch.Tweaker;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Parent object for all hotswap related events. You probably will not want to subscribe to this event directly. There
 * are no guarantees for which thread these events get fired on.
 */
public class HotswapEvent extends Event {
    protected HotswapEvent() {
    }

    /**
     * @return whether the hotswap plugin was loaded.
     */
    public static boolean isHotswapPluginLoaded() {
        return Tweaker.isHotswapAttached();
    }

    /**
     * @return whether the hotswap plugin was loaded and events can be received
     */
    public static boolean isReady() {
        return isHotswapPluginLoaded() && areForgeEventsReady();
    }

    /**
     * @return whether the tweaker has access to the forge event loop and the hotswap event classes.
     */
    public static boolean areForgeEventsReady() {
        return Tweaker.isReady();
    }
}
