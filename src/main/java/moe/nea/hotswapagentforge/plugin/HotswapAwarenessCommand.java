package moe.nea.hotswapagentforge.plugin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.val;
import moe.nea.hotswapagentforge.launch.Tweaker;
import org.hotswap.agent.command.Command;

/**
 * A command to make the {@link Tweaker} aware of the hotswap plugin.
 */
@AllArgsConstructor
@ToString
@Getter
public class HotswapAwarenessCommand implements Command {
    Object tweaker;

    @Override
    public void executeCommand() {
        try {
            val onHotswapperAttach = tweaker.getClass().getMethod("onHotswapperAttach");
            onHotswapperAttach.invoke(tweaker);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
