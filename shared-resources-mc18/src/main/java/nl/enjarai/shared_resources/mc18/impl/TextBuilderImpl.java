package nl.enjarai.shared_resources.mc18.impl;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import nl.enjarai.shared_resources.versioned.TextBuilder;

public class TextBuilderImpl implements TextBuilder {
    @Override
    public Text translatable(String key, Object... objects) {
        return new TranslatableText(key, objects);
    }
}