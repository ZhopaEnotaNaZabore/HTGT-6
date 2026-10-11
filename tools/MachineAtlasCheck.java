import com.mod.htgt6.client.render.ItemAtlasData;
import java.nio.file.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Checks the same decoder used by Forge against the source sheet and opaque block edges. */
public final class MachineAtlasCheck {
    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args.length == 0 ? "." : args[0]);
        Path dir = root.resolve("src/main/resources/assets/htgt6/textures/blocks/atlases");
        ItemAtlasData data;
        try (InputStream png = Files.newInputStream(dir.resolve("singleblock.png"));
             InputStream index = Files.newInputStream(dir.resolve("singleblock.properties"))) {
            data = new ItemAtlasData(png, index);
        }
        BufferedImage source = ImageIO.read(root.resolve("docs/assets/singleblock-machines/atlas-source.png").toFile());
        String[] names = {"assembler_side", "assembler_top", "assembler_front", "machine_front", "em_processor_front", "metal_former_front", "MEG_Front", "RIG_Front", "UGS_Front", "UEfront"};
        if (data.size() != names.length) throw new AssertionError("Count");
        for (int i=0; i<names.length; i++) {
            if (data.iconSize(names[i]) != 64) throw new AssertionError(names[i]);
            int[] pixels = data.pixels(names[i]);
            for (int y=0; y<64; y++) for (int x=0; x<64; x++) {
                int sx=(int)Math.floor((i%5+(x+0.5)/64)*source.getWidth()/5);
                int sy=(int)Math.floor((i/5+(y+0.5)/64)*source.getHeight()/2);
                if (pixels[y*64+x] != source.getRGB(sx,sy) || (pixels[y*64+x] >>> 24) != 255)
                    throw new AssertionError("Wrong pixels or transparent edge: " + names[i]);
            }
        }
        System.out.println("PASS: 10 machine faces, exact 64px slices, source pixels and opaque edges.");
    }
}
