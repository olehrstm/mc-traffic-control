package de.ole101.mctrafficcontrol.gui.font;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import de.ole101.mctrafficcontrol.mixin.FontSetAccessor;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class CustomGlyphs {

    public static final String VANILLA_PACK_ID = "vanilla";

    private static final Set<GlyphProvider> CUSTOM_PROVIDERS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Setter
    private static Map<Identifier, FontSet> fontSets = Map.of();

    public static void markCustom(GlyphProvider provider) {
        CUSTOM_PROVIDERS.add(provider);
    }

    public static boolean isCustom(GlyphProvider provider) {
        return CUSTOM_PROVIDERS.contains(provider);
    }

    public static @Nullable FontDescription customTextureFont(int codepoint, FontDescription font) {
        if (font instanceof FontDescription.Resource(Identifier fontId) && !fontId.equals(Minecraft.DEFAULT_FONT)) {
            GlyphProvider provider = glyphProvider(fontId, codepoint);
            if (provider != null) {
                return isCustom(provider) ? font : null;
            }
        }

        GlyphProvider provider = glyphProvider(Minecraft.DEFAULT_FONT, codepoint);
        return provider != null && isCustom(provider) ? FontDescription.DEFAULT : null;
    }

    public static boolean hasGlyph(Identifier fontId, int codepoint) {
        return glyphProvider(fontId, codepoint) != null;
    }

    private static @Nullable GlyphProvider glyphProvider(Identifier fontId, int codepoint) {
        FontSet fontSet = fontSets.get(fontId);
        if (fontSet == null) {
            return null;
        }

        for (GlyphProvider provider : ((FontSetAccessor) fontSet).mtc$getActiveProviders()) {
            UnbakedGlyph glyph = provider.getGlyph(codepoint);
            if (glyph != null) {
                return glyph.info() == SpecialGlyphs.MISSING ? null : provider;
            }
        }

        return null;
    }
}
