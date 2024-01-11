package moe.nea.hotswapagentforge.plugin;

import lombok.val;
import moe.nea.hotswapagentforge.launch.Tweaker;
import org.hotswap.agent.annotation.Init;
import org.hotswap.agent.annotation.LoadEvent;
import org.hotswap.agent.annotation.OnClassLoadEvent;
import org.hotswap.agent.annotation.Plugin;
import org.hotswap.agent.command.Scheduler;
import org.hotswap.agent.javassist.CannotCompileException;
import org.hotswap.agent.javassist.CtClass;
import org.hotswap.agent.javassist.NotFoundException;
import org.hotswap.agent.logging.AgentLogger;
import org.hotswap.agent.util.PluginManagerInvoker;


/**
 * Two part hotswap agent plugin. The static component loads itself into the tweaker using {@link #onTweakerLoaded},
 * and the non-static component is loaded by the tweaker.
 */
@Plugin(name = "Forge", testedVersions = {"1.4.0"})
public class HotswapAgentPlugin {
    private static AgentLogger LOGGER = AgentLogger.getLogger(HotswapAgentPlugin.class);


    /**
     * Listen to the loading of {@link Tweaker} and inject a hotswap plugin construction so that we can call methods in
     * the application class loader.
     */
    @OnClassLoadEvent(
            classNameRegexp = "moe.nea.hotswapagentforge.launch.Tweaker",
            events = LoadEvent.DEFINE
    )
    public static void onTweakerLoaded(CtClass ctClass) throws NotFoundException, CannotCompileException {
        // Inject ourselves into the tweaker. The tweaker will then create a new plugin with and provide the tweaker
        // instance, which we can then use to manipulate functionality inside the application class loader
        String src = PluginManagerInvoker.buildInitializePlugin(HotswapAgentPlugin.class);
        src += PluginManagerInvoker.buildCallPluginMethod(HotswapAgentPlugin.class, "registerTweaker", "this", "java.lang.Object");
        val declaredConstructor = ctClass.getDeclaredMethod("getLaunchArguments");
        declaredConstructor.insertAfter(src);
        LOGGER.info("Hotswapagent Forge Tweaker enhanced.");
    }

    @Init
    ClassLoader appClassLoader;

    @Init
    Scheduler scheduler;

    Object tweaker;

    /**
     * Called by the constructor of {@link Tweaker}. This call is injected using {@link #onTweakerLoaded} and allows us
     * to obtain a reference to the instantiated {@link Tweaker}
     */
    public void registerTweaker(Object tweaker) {
        LOGGER.info("Tweaker registered");
        this.tweaker = tweaker;
        scheduler.scheduleCommand(new HotswapAwarenessCommand(tweaker));
    }

    /**
     * Listen to class redefinitions in order to schedule {@link HotswapDefinitionCommand} and {@link HotswapFinishedCommand}
     */
    @OnClassLoadEvent(
            classNameRegexp = ".*",
            events = LoadEvent.REDEFINE
    )
    public void onClassRedefinition(CtClass ctClass) {
        scheduler.scheduleCommand(new HotswapDefinitionCommand(tweaker, ctClass.getName(), true));
        scheduler.scheduleCommand(new HotswapFinishedCommand(tweaker), 500, Scheduler.DuplicateSheduleBehaviour.SKIP);
    }

    /**
     * Listen to class definitions in order to schedule {@link HotswapDefinitionCommand}
     */
    @OnClassLoadEvent(
            classNameRegexp = ".*",
            events = LoadEvent.DEFINE
    )
    public void onClassDefinition(CtClass ctClass) {
        scheduler.scheduleCommand(new HotswapDefinitionCommand(tweaker, ctClass.getName(), false));
    }

}
