package moe.nea.hotswapagentforge.plugin;

import lombok.Value;
import moe.nea.hotswapagentforge.forge.ClassDefinitionEvent;
import net.minecraftforge.common.MinecraftForge;
import org.hotswap.agent.command.Command;

import java.lang.reflect.Method;

/**
 * A command to fire a {@link ClassDefinitionEvent} on the {@link MinecraftForge#EVENT_BUS forge event bus} after a
 * class (re)definition was detected by {@link HotswapAgentPlugin#onClassRedefinition} or {@link HotswapAgentPlugin#onClassDefinition}.
 */
@Value
public class HotswapDefinitionCommand implements Command {
    Object tweaker;
    String fqName;
    boolean isRedefinition;

    @Override
    public void executeCommand() {
        try {
            Method onDefineClass = tweaker.getClass().getMethod("onDefineClass", String.class, boolean.class);
            onDefineClass.invoke(tweaker, fqName, isRedefinition);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
