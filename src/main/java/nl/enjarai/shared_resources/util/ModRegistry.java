package nl.enjarai.shared_resources.util;

import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ModRegistry<T> {
    private final HashMap<Identifier, T> entries = new HashMap<>();

    public T register(Identifier id, T entry) {
        entries.put(id, entry);
        return entry;
    }

    public T get(Identifier id) {
        return entries.get(id);
    }

    @Nullable
    public Identifier getId(T entry) {
        for (Identifier id : entries.keySet()) {
            if (entries.get(id) == entry) {
                return id;
            }
        }
        return null;
    }
}
