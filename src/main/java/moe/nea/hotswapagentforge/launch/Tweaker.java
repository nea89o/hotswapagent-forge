package moe.nea.hotswapagentforge.launch;

import lombok.SneakyThrows;
import lombok.val;
import moe.nea.hotswapagentforge.forge.ClassDefinitionEvent;
import moe.nea.hotswapagentforge.forge.HotswapFinishedEvent;
import moe.nea.hotswapagentforge.plugin.HotswapAgentPlugin;
import moe.nea.hotswapagentforge.plugin.HotswapAwarenessCommand;
import moe.nea.hotswapagentforge.plugin.HotswapDefinitionCommand;
import moe.nea.hotswapagentforge.plugin.HotswapFinishedCommand;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tweaker to function as a bridge between hotswap agents meta classloader, and the {@link LaunchClassLoader} used by
 * the running minecraft instance. This tweaker will be loaded by the application class loader that {@link Launch}
 * is loaded with. {@link HotswapAgentPlugin} will cause this classes constructor to create a new {@link HotswapAgentPlugin}
 * instance and call {@link HotswapAgentPlugin#registerTweaker} which in turn will call this tweaker whenever minecraft
 * code should be called. This tweaker does not actually implement any of the launchwrapper tweaker mechanics itself.
 * <p><b>This tweaker should not be called by the end user.</b>
 */
public class Tweaker implements ITweaker {

    static Tweaker instance;

    public Tweaker() {
        instance = this;
    }

    /**
     * The minecraft class loader. This is the class loader loading most mods and all minecraft code. Is <b>not</b>
     * the application class loader used by {@link Launch} or the JVM main entry point.
     */
    private static LaunchClassLoader classLoader;

    private final Map<String, Constructor<Object>> constructors = new HashMap<>();
    private Method eventBusPost;
    private Object forgeEventBus;

    static boolean forgeLoaded = false;
    static boolean eventsLoaded = false;
    private static boolean isHotswapAttached = false;

    /**
     * @return whether the hotswap plugin has been attached
     */
    public static boolean isHotswapAttached() {
        return isHotswapAttached;
    }

    /**
     * Called by {@link HotswapAwarenessCommand} after hotswap agent is loaded and has loaded the tweaker connection.
     */
    public void onHotswapperAttach() {
        isHotswapAttached = true;
    }

    /**
     * Indicates if all required classes are present and loaded in the {@link #classLoader minecraft class loader}
     */
    public static boolean isReady() {
        return forgeLoaded && eventsLoaded && classLoader != null;
    }

    /**
     * Fire an event on the forge event bus loaded using the {@link #classLoader minecraft class loader}.
     *
     * @param className the fully qualified class name of the event
     * @param args      the arguments to the events constructor. must be always the same types.
     */
    @SneakyThrows
    private void fireEvent(String className, Object... args) {
        if (!isReady()) return;
        val handle = constructors.computeIfAbsent(className, name -> loadClassConstructor(name, args));
        val eventInstance = handle.newInstance(args);
        fireEventInstance(eventInstance);
    }

    @SneakyThrows
    private void ensurePostMethodBound() {
        if (eventBusPost != null) return;
        val forgeClass = Class.forName("net.minecraftforge.common.MinecraftForge", false, classLoader);
        val eventBusField = forgeClass.getField("EVENT_BUS");
        val eventBus = eventBusField.get(null);
        val eventClass = Class.forName("net.minecraftforge.fml.common.eventhandler.Event", false, classLoader);
        eventBusPost = eventBus.getClass().getMethod("post", eventClass);
        this.forgeEventBus = eventBus;
    }

    /**
     * Fire an event instance via {@link MinecraftForge#EVENT_BUS}
     *
     * @param eventInstance must be of type {@link Event} or a subclass, but we cannot load this class from the tweaker
     */
    @SneakyThrows
    private void fireEventInstance(Object eventInstance) {
        if (!isReady()) return;
        ensurePostMethodBound();
        eventBusPost.invoke(forgeEventBus, eventInstance);
    }

    /**
     * Load a class constructor with the given arguments and convert it to a method handle in the {@link #classLoader
     * minecraft class loader}
     *
     * @param name the name of the class to look for
     * @param args the argument instances (not argument types) of the wanted constructor
     */
    @SneakyThrows
    private Constructor<Object> loadClassConstructor(String name, Object[] args) {
        val clazz = Class.forName(name, true, classLoader);
        val argTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        val constructor = clazz.getConstructor(argTypes);
        return (Constructor<Object>) constructor;
    }

    /**
     * Tweaker trampoline for {@link HotswapDefinitionCommand} to {@link ClassDefinitionEvent}
     */
    public void onDefineClass(String fullyQualifiedName, boolean isRedefinition) {
        fireEvent(
                isRedefinition
                        ? "moe.nea.hotswapagentforge.forge.ClassDefinitionEvent$Redefinition"
                        : "moe.nea.hotswapagentforge.forge.ClassDefinitionEvent$Definition",
                fullyQualifiedName);
    }


    /**
     * Tweaker trampoline for {@link HotswapFinishedCommand} to {@link HotswapFinishedEvent}
     */
    public void onHotswapFinished() {
        fireEvent("moe.nea.hotswapagentforge.forge.HotswapFinishedEvent");
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        this.classLoader = classLoader;
        // Register a class transformer so we can be made aware of which classes are being loaded.
        classLoader.registerTransformer("moe.nea.hotswapagentforge.launch.Transformer");
    }


    // Below is stub to make this a valid tweaker

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    }


    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
