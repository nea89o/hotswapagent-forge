package moe.nea.hotswapagentforge.plugin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.nea.hotswapagentforge.forge.HotswapFinishedEvent;
import net.minecraftforge.common.MinecraftForge;
import org.hotswap.agent.command.MergeableCommand;

import java.lang.reflect.Method;

/**
 * A command to fire a {@link HotswapFinishedEvent} on the {@link MinecraftForge#EVENT_BUS forge event bus} after a
 * finished hotswap was detected by {@link HotswapAgentPlugin#onClassRedefinition}. This event is {@link MergeableCommand
 * mergeable} and should be scheduled with a delay of {@code 500ms} so it can be properly merged and only one {@link
 * HotswapFinishedEvent} is fired.
 */
@Getter
@AllArgsConstructor
@ToString
public class HotswapFinishedCommand extends MergeableCommand {
    private Object tweaker;

    @Override
    public void executeCommand() {
        try {
            Method method = tweaker.getClass().getMethod("onHotswapFinished");
            method.invoke(tweaker);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HotswapFinishedCommand && ((HotswapFinishedCommand) obj).tweaker.equals(tweaker);
    }

    @Override
    public int hashCode() {
        return tweaker.hashCode();
    }
}
