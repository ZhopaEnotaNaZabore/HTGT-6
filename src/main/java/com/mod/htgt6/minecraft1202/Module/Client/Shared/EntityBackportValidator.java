package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.BackportEntityModel;

/** Fail-fast client validation for texture/model contracts of backported entities. */
public final class EntityBackportValidator {
    public static void texture(String path, int expectedWidth, int expectedHeight) {
        ResourceLocation location = new ResourceLocation("htgt6", path);
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            InputStream stream = resource.getInputStream();
            BufferedImage image;
            try { image = ImageIO.read(stream); } finally { stream.close(); }
            if (image == null || image.getWidth() != expectedWidth || image.getHeight() != expectedHeight) {
                throw new IllegalStateException("Expected " + expectedWidth + "x" + expectedHeight
                        + " entity texture at " + location + " but found "
                        + (image == null ? "an unreadable image" : image.getWidth() + "x" + image.getHeight()));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Backported entity texture validation failed for " + location, exception);
        }
    }

    /** Second-stage validation for modern named-part models after texture validation. */
    public static void modelParts(BackportEntityModel<?> model, String... requiredParts) {
        if (model == null) throw new IllegalStateException("Backported entity model is null");
        for (String name : requiredParts) model.getPart(name);
    }

    private EntityBackportValidator() { }
}
