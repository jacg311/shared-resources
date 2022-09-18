package nl.enjarai.shared_resources.mc19;

import nl.enjarai.shared_resources.common.util.SRConfigEntryPoint;
import nl.enjarai.shared_resources.common.util.TextBuilder;
import nl.enjarai.shared_resources.mc19.util.TextBuilderImpl;

public class SREntryPoint implements SRConfigEntryPoint {

    @Override
    public TextBuilder getTextBuilder() {
        return new TextBuilderImpl();
    }
}