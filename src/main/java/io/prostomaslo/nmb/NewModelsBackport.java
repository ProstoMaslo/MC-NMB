package io.prostomaslo.nmb;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Mod(NewModelsBackport.MOD_ID)
public class NewModelsBackport {
    public static final String MOD_ID = "nmb";
    public static final Logger LOGGER = LoggerFactory.getLogger("NMB");
    public NewModelsBackport() {
        LOGGER.info("[NMB] Initializing Forge 1.20.1 model backport system...");
    }
}
