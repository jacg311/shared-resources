package nl.enjarai.shared_resources.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nl.enjarai.shared_resources.SharedResources;
import nl.enjarai.shared_resources.api.DirectoryResource;
import nl.enjarai.shared_resources.api.DirectoryResourceHelper;
import nl.enjarai.shared_resources.api.DirectoryResourceRegistry;
import nl.enjarai.shared_resources.config.serialization.GameDirectoryProviderAdapter;
import nl.enjarai.shared_resources.config.serialization.IdentifierAdapter;
import nl.enjarai.shared_resources.gui.DirectoryConfigEntry;
import nl.enjarai.shared_resources.registry.DirectoryResources;
import nl.enjarai.shared_resources.util.directory.EmptyGameDirectoryProvider;
import nl.enjarai.shared_resources.util.directory.GameDirectoryProvider;
import nl.enjarai.shared_resources.util.directory.RootedGameDirectoryProvider;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;

public class ModConfig {
    // Make sure we use the default config location instead of our modified one.
    public static final File CONFIG_FILE = DirectoryResources.CONFIG.getDefaultDirectory().resolve(SharedResources.MODID + ".json").toFile();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new IdentifierAdapter())
            .registerTypeAdapter(GameDirectoryProvider.class, new GameDirectoryProviderAdapter())
            .setPrettyPrinting() // Makes the json use new lines instead of being a "one-liner"
            .disableHtmlEscaping() // We'll be able to use custom chars without them being saved differently
            .create();

    public static ModConfig INSTANCE;

    static {
        INSTANCE = loadConfigFile(CONFIG_FILE);

        var allDirs = DirectoryResourceRegistry.REGISTRY.iterator();
        while (allDirs.hasNext()) {
            var dir = allDirs.next();
            var id = dir.getId();

            if (!INSTANCE.enabled.containsKey(id)) {
                INSTANCE.setEnabled(id, dir.defaultEnabled());
            }
        }

        INSTANCE.save();
    }

    private GameDirectoryProvider globalDirectory = new RootedGameDirectoryProvider(Path.of("global_resources"));
    private final HashMap<Identifier, Boolean> enabled = new HashMap<>();


    public GameDirectoryProvider getGlobalDirectory() {
        return globalDirectory;
    }

    public void setGlobalDirectory(@Nullable Path path) {
        if (path == null) {
            globalDirectory = new EmptyGameDirectoryProvider();
        } else {
            globalDirectory = new RootedGameDirectoryProvider(path);
        }

        DirectoryResourceRegistry.REGISTRY.iterator().forEachRemaining(directory -> {
            var dirPath = DirectoryResourceHelper.getPathFor(directory);
            directory.getUpdateCallback().onDirectoryUpdate(dirPath);
        });
    }


    public boolean isEnabled(Identifier id) {
        if (globalDirectory instanceof EmptyGameDirectoryProvider) return false;

        return enabled.getOrDefault(id, false);
    }

    public void setEnabled(Identifier id, boolean enabled) {
        this.enabled.put(id, enabled);
    }

    public boolean isEnabled(DirectoryResource directory) {
        return isEnabled(directory.getId());
    }

    public void setEnabled(DirectoryResource directory, boolean enabled) {
        setEnabled(directory.getId(), enabled);
    }



    public Screen getScreen(Screen parent) {

        var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.shared_resources.title"))
                .setSavingRunnable(this::save);

        var entryBuilder = builder.entryBuilder();

        var generalCategory = builder
                .getOrCreateCategory(Text.translatable("config.shared_resources.general"))
                .addEntry(
                        entryBuilder.startTextDescription(
                                Text.translatable("config.shared_resources.general.directory")
                        ).build()
                )
                .addEntry(new DirectoryConfigEntry(
                        Text.translatable("config.shared_resources.general.directory"),
                        getGlobalDirectory() instanceof RootedGameDirectoryProvider rooted ? rooted.getRoot() : null,
                        Path.of("global_resources"),
                        this::setGlobalDirectory
                ))
                .addEntry(
                        entryBuilder.startTextDescription(
                                Text.translatable("config.shared_resources.general.enabled")
                        ).build()
                );

        enabled.forEach((id, enabled) -> {
            var directory = DirectoryResourceRegistry.REGISTRY.get(id);
            if (directory == null) return;

            var entry = entryBuilder.startBooleanToggle(directory.getDisplayName(), enabled)
                    .setDefaultValue(directory.defaultEnabled())
                    .setSaveConsumer(newEnabled -> {
                        setEnabled(id, newEnabled);
                        directory.getUpdateCallback().onDirectoryUpdate(DirectoryResourceHelper.getPathFor(directory));
                    })
                    .build();
            entry.setRequiresRestart(directory.requiresRestart());
            generalCategory.addEntry(entry);
        });

        return builder.build();
    }


    public void save() {
        saveConfigFile(CONFIG_FILE);
    }

    /**
     * Loads config file.
     *
     * @param file file to load the config file from.
     * @return ModConfig object
     */
    private static ModConfig loadConfigFile(File file) {
        ModConfig config = null;

        if (file.exists()) {
            // An existing config is present, we should use its values
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                // Parses the config file and puts the values into config object
                config = GSON.fromJson(fileReader, ModConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Problem occurred when trying to load config: ", e);
            }
        }
        // gson.fromJson() can return null if file is empty
        if (config == null) {
            config = new ModConfig();
        }

        // Saves the file in order to write new fields if they were added
        config.saveConfigFile(file);
        return config;
    }

    /**
     * Saves the config to the given file.
     *
     * @param file file to save config to
     */
    private void saveConfigFile(File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
