package nl.enjarai.shared_resources.mc19_2.mixin.datapacks;

import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.server.integrated.IntegratedServerLoader;
import nl.enjarai.shared_resources.api.GameResourceHelper;
import nl.enjarai.shared_resources.common.registry.GameResources;
import nl.enjarai.shared_resources.mc19_2.util.ExternalFileResourcePackProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(IntegratedServerLoader.class)
public abstract class IntegratedServerLoaderMixin {
    @ModifyArg(
            method = "createDataPackManager",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resource/ResourcePackManager;<init>(Lnet/minecraft/resource/ResourceType;[Lnet/minecraft/resource/ResourcePackProvider;)V"
            )
    )
    private static ResourcePackProvider[] sharedresources$addDataPackProvider(ResourcePackProvider[] providers) {
        return ArrayUtils.add(providers, new ExternalFileResourcePackProvider(
                () -> GameResourceHelper.getPathOrDefaultFor(GameResources.DATAPACKS)
        ));
    }
}