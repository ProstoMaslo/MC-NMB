package io.prostomaslo.nmb;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Mod(NewModelsBackport.MOD_ID)
public class NewModelsBackport {
    public static final String MOD_ID = "nmb";
    public static final Logger LOGGER = LoggerFactory.getLogger("NMB");
    public NewModelsBackport() {
        LOGGER.info("[NMB] Initializing 1.21.11 model backport system...");
    }
}
