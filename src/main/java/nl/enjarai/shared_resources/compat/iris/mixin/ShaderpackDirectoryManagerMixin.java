package nl.enjarai.shared_resources.compat.iris.mixin;

import nl.enjarai.shared_resources.SharedResources;
import nl.enjarai.shared_resources.api.DirectoryResourceHelper;
import nl.enjarai.shared_resources.registry.DirectoryResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Pseudo
@Mixin(targets = "net.coderbot.iris.shaderpack.discovery.ShaderpackDirectoryManager")
public abstract class ShaderpackDirectoryManagerMixin {
    @Redirect(
            method = "enumerate()L;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/file/Files;list(Ljava/nio/file/Path;)Ljava/util/stream/Stream;"
            )
    )
    private Stream<Path> injectShaderpacks(Path originalPath) throws IOException {
        var original = Files.list(originalPath);

        var source = DirectoryResourceHelper.getPathFor(DirectoryResources.SHADERPACKS);
        if (source == null) return original;


        try {

            var extraPaths = Files.list(source);
            return Stream.concat(original, extraPaths);

        } catch (IOException e) {

            SharedResources.LOGGER.error("Failed to list shaderpacks from custom directory", e);
            return original;
        }
    }
}
