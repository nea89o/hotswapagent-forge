package moe.nea.hotswapagentforge.launch;

import lombok.val;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.List;
import java.util.Map;

public class HotswapAgentLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        val tweakClasses = (List<String>) Launch.blackboard.get("TweakClasses");
        tweakClasses.add("moe.nea.hotswapagentforge.launch.Tweaker");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
