package com.mod.htgt6.client.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Opt-in bridge for the common Minecraft 1.8+ blockstate/model JSON format.
 *
 * It resolves blockstate variants, recursive parents, texture aliases, cuboid elements and
 * 90-degree X/Y variant rotations, then feeds that data into the Forge 1.7.10 block renderer.
 * Unsupported or malformed resources fail per block and fall back to its ordinary renderer.
 */
public final class JsonBlockModelLoader implements ISimpleBlockRenderingHandler {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-JsonModels18");
    private static JsonBlockModelLoader instance;

    private final Map<net.minecraft.item.Item, ItemBinding> itemBindings = new LinkedHashMap<net.minecraft.item.Item, ItemBinding>();
    private final int renderId;
    private final Map<Block, Binding> bindings = new LinkedHashMap<Block, Binding>();
    private final Map<ResourceLocation, ModelDefinition> modelCache = new HashMap<ResourceLocation, ModelDefinition>();

    public interface StateMapper {
        String variant(IBlockAccess world, int x, int y, int z, int metadata);
    }

    public interface TextureResolver {
        ResourceLocation resolve(String modelTexture);
    }

    /** Optional second texture layer, resolved from live block/tile state. */
    public interface OverlayResolver {
        IIcon resolve(Block block, IBlockAccess world, int x, int y, int z, int side, int metadata);
    }

    public static synchronized JsonBlockModelLoader install() {
        if (instance == null) {
            instance = new JsonBlockModelLoader(RenderingRegistry.getNextAvailableRenderId());
            RenderingRegistry.registerBlockHandler(instance);
            MinecraftForge.EVENT_BUS.register(instance);
        }
        return instance;
    }

    private JsonBlockModelLoader(int renderId) {
        this.renderId = renderId;
    }

    public int getRenderId() {
        return renderId;
    }

    public void register(Block block, ResourceLocation blockstate, StateMapper mapper,
                         TextureResolver textureResolver) {
        register(block, blockstate, mapper, textureResolver, null);
    }

    public void register(Block block, ResourceLocation blockstate, StateMapper mapper,
                         TextureResolver textureResolver, OverlayResolver overlayResolver) {
        if (block == null || blockstate == null || mapper == null || textureResolver == null) {
            throw new IllegalArgumentException("JSON block-model registration contains null");
        }
        bindings.put(block, new Binding(blockstate, mapper, textureResolver, overlayResolver));
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 0) return;
        modelCache.clear();
        IResourceManager resources = Minecraft.getMinecraft().getResourceManager();
        int loaded = 0;
        for (Map.Entry<Block, Binding> entry : bindings.entrySet()) {
            try {
                entry.getValue().load(resources, event.map);
                ++loaded;
            } catch (Exception error) {
                entry.getValue().error = error;
                LOGGER.error("Unable to load 1.8+ JSON model for {} from {}",
                        entry.getKey().getUnlocalizedName(), entry.getValue().blockstate, error);
            }
        }
        for(ItemBinding item:itemBindings.values()) {
            item.models.clear();
            try { item.load(Minecraft.getMinecraft().getResourceManager(),event.map,item.location); }
            catch(Exception error){item.models.clear();LOGGER.error("Unable to load item JSON {}",item.location,error);}
        }
        LOGGER.info("Loaded {}/{} registered 1.8+ JSON block-model bindings", loaded, bindings.size());
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
                                    int modelId, RenderBlocks renderer) {
        if(com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.supports(block)
                &&com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.waterPass())
            return com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.render(world,x,y,z,block,renderer);
        Binding binding = bindings.get(block);
        List<Variant> selected = binding == null ? null : binding.variants(world, x, y, z,
                world.getBlockMetadata(x, y, z));
        if (selected == null || selected.isEmpty() || binding.error != null)
            return renderer.renderStandardBlock(block, x, y, z);

        Tessellator tessellator = Tessellator.instance;
        for (Variant variant : selected) for (Element element : variant.model.elements) {
            for (Face face : element.faces.values()) {
                Direction cull = face.cull == null ? null : rotate(face.cull, variant.rotationX, variant.rotationY);
                if (cull != null && !block.shouldSideBeRendered(world, x + cull.x, y + cull.y, z + cull.z, cull.side)) {
                    continue;
                }
                IIcon icon = variant.icons.get(resolveTexture(variant.model.textures, face.texture));
                if (icon == null) icon = block.getIcon(face.direction.side, world.getBlockMetadata(x, y, z));
                renderModelFace(tessellator, element, face, variant, icon, x, y, z,
                        block, world, world.getBlockMetadata(x, y, z), false, -1);
                if (binding.overlayResolver != null) {
                    Direction renderedFace = rotate(face.direction, variant.rotationX, variant.rotationY);
                    IIcon overlay = binding.overlayResolver.resolve(block, world, x, y, z,
                            renderedFace.side, world.getBlockMetadata(x, y, z));
                    if (overlay != null) renderModelFace(tessellator, element, face, variant, overlay,
                            x, y, z, block, world, world.getBlockMetadata(x, y, z), false,
                            block.colorMultiplier(world, x, y, z));
                }
            }
        }
        renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
        return true;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        Binding binding = bindings.get(block);
        List<Variant> selected = binding == null ? null : binding.inventoryVariants(metadata);
        if (selected == null || selected.isEmpty() || binding.error != null) {
            renderFallbackInventory(block, metadata, renderer);
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        for (Variant variant : selected) for (Element element : variant.model.elements) {
            for (Face face : element.faces.values()) {
                IIcon icon = variant.icons.get(resolveTexture(variant.model.textures, face.texture));
                if (icon == null) icon = block.getIcon(face.direction.side, metadata);
                Tessellator tessellator = Tessellator.instance;
                tessellator.startDrawingQuads();
                renderModelFace(tessellator, element, face, variant, icon, 0, 0, 0,
                        block, null, metadata, true, -1);
                if (binding.overlayResolver != null) {
                    Direction renderedFace = rotate(face.direction, variant.rotationX, variant.rotationY);
                    IIcon overlay = binding.overlayResolver.resolve(block, null, 0, 0, 0,
                            renderedFace.side, metadata);
                    if (overlay != null) renderModelFace(tessellator, element, face, variant, overlay,
                            0, 0, 0, block, null, metadata, true, block.getRenderColor(metadata));
                }
                tessellator.draw();
            }
        }
        renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
        GL11.glPopMatrix();
    }

    private static void renderFallbackInventory(Block block, int metadata, RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        renderer.setRenderBoundsFromBlock(block);
        for (Direction direction : Direction.values()) {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setNormal(direction.x, direction.y, direction.z);
            renderFace(renderer, block, 0, 0, 0, direction, block.getIcon(direction.side, metadata));
            tessellator.draw();
        }
        GL11.glPopMatrix();
    }

    private static void renderFace(RenderBlocks renderer, Block block, double x, double y, double z,
                                   Direction direction, IIcon icon) {
        switch (direction) {
            case DOWN: renderer.renderFaceYNeg(block, x, y, z, icon); break;
            case UP: renderer.renderFaceYPos(block, x, y, z, icon); break;
            case NORTH: renderer.renderFaceZNeg(block, x, y, z, icon); break;
            case SOUTH: renderer.renderFaceZPos(block, x, y, z, icon); break;
            case WEST: renderer.renderFaceXNeg(block, x, y, z, icon); break;
            case EAST: renderer.renderFaceXPos(block, x, y, z, icon); break;
        }
    }

    /** Emits exact model quads, including partial/reversed UV and arbitrary element rotation. */
    private static void renderModelFace(Tessellator tessellator, Element element, Face face,
                                          Variant variant, IIcon icon, double x, double y, double z,
                                          Block block, IBlockAccess world, int metadata,
                                          boolean inventory, int colourOverride) {
        double[][] vertices = faceVertices(element, face.direction);
        for (int i = 0; i < vertices.length; ++i) {
            if (element.rotation != null) vertices[i] = rotateElementPoint(vertices[i], element.rotation);
            vertices[i] = rotatePoint(vertices[i][0], vertices[i][1], vertices[i][2],
                    variant.rotationX, variant.rotationY);
        }
        // faceVertices keeps the source UV corner order. Its historical winding points inward,
        // so calculate the outward normal and emit the same vertex/UV pairs in reverse order.
        // Reversing only emission preserves the exact (including reversed) JSON UV rectangle.
        double[] normal = normal(vertices);
        normal[0] = -normal[0];
        normal[1] = -normal[1];
        normal[2] = -normal[2];
        tessellator.setNormal((float)normal[0], (float)normal[1], (float)normal[2]);
        if (!inventory) {
            // Opaque blocks often report a zero lightmap from their own cell in 1.7.10.
            // Vanilla RenderBlocks samples the cell outside each face instead.
            int lightX = (int)x, lightY = (int)y, lightZ = (int)z;
            double ax = Math.abs(normal[0]), ay = Math.abs(normal[1]), az = Math.abs(normal[2]);
            if (ax >= ay && ax >= az) lightX += normal[0] < 0.0D ? -1 : 1;
            else if (ay >= az) lightY += normal[1] < 0.0D ? -1 : 1;
            else lightZ += normal[2] < 0.0D ? -1 : 1;
            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, lightX, lightY, lightZ));
        }
        int colour = colourOverride >= 0 ? colourOverride : face.tintIndex >= 0
                ? (inventory ? block.getRenderColor(metadata) : block.colorMultiplier(world, (int)x, (int)y, (int)z))
                : 0xFFFFFF;
        // Inventory rendering already applies directional OpenGL lighting from the normal.
        // Multiplying the vanilla face shade here as well darkened side faces twice.
        float shade = element.shade && !inventory ? shade(normal) : 1.0F;
        tessellator.setColorOpaque_F(((colour >> 16) & 255) / 255.0F * shade,
                ((colour >> 8) & 255) / 255.0F * shade, (colour & 255) / 255.0F * shade);

        double u0 = icon.getInterpolatedU(face.uv[0]);
        double v0 = icon.getInterpolatedV(face.uv[1]);
        double u1 = icon.getInterpolatedU(face.uv[2]);
        double v1 = icon.getInterpolatedV(face.uv[3]);
        double[][] baseUv = {{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
        double[][] uv = new double[4][2];
        int uvTurns = ((face.uvRotation % 360) + 360) % 360 / 90;
        if (variant.uvLock && (variant.rotationX != 0 || variant.rotationY != 0)) {
            Direction rotatedFace = rotate(face.direction, variant.rotationX, variant.rotationY);
            Bounds bounds = bounds(vertices);
            Element rotatedElement = new Element(
                    new double[]{bounds.minX, bounds.minY, bounds.minZ},
                    new double[]{bounds.maxX, bounds.maxY, bounds.maxZ});
            double[][] canonical = faceVertices(rotatedElement, rotatedFace);
            for (int i = 0; i < 4; ++i)
                uv[i] = baseUv[(matchingVertex(vertices[i], canonical) + uvTurns) & 3];
        } else for (int i = 0; i < 4; ++i) uv[i] = baseUv[(i + uvTurns) & 3];
        for (int i = 3; i >= 0; --i) {
            tessellator.addVertexWithUV(x + vertices[i][0] / 16.0D,
                    y + vertices[i][1] / 16.0D, z + vertices[i][2] / 16.0D, uv[i][0], uv[i][1]);
        }
    }

    private static double[][] faceVertices(Element element, Direction direction) {
        double x0 = element.from[0], y0 = element.from[1], z0 = element.from[2];
        double x1 = element.to[0], y1 = element.to[1], z1 = element.to[2];
        switch (direction) {
            case DOWN: return new double[][]{{x0,y0,z1},{x1,y0,z1},{x1,y0,z0},{x0,y0,z0}};
            case UP: return new double[][]{{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1}};
            case NORTH: return new double[][]{{x1,y1,z0},{x0,y1,z0},{x0,y0,z0},{x1,y0,z0}};
            case SOUTH: return new double[][]{{x0,y1,z1},{x1,y1,z1},{x1,y0,z1},{x0,y0,z1}};
            case WEST: return new double[][]{{x0,y1,z0},{x0,y1,z1},{x0,y0,z1},{x0,y0,z0}};
            default: return new double[][]{{x1,y1,z1},{x1,y1,z0},{x1,y0,z0},{x1,y0,z1}};
        }
    }

    private static double[] rotateElementPoint(double[] point, ElementRotation rotation) {
        double x = point[0] - rotation.origin[0];
        double y = point[1] - rotation.origin[1];
        double z = point[2] - rotation.origin[2];
        double radians = Math.toRadians(rotation.angle);
        double sin = Math.sin(radians), cos = Math.cos(radians);
        if (rotation.rescale) {
            double scale = 1.0D / Math.cos(Math.abs(radians));
            if (rotation.axis != 'x') x *= scale;
            if (rotation.axis != 'y') y *= scale;
            if (rotation.axis != 'z') z *= scale;
        }
        double next;
        switch (rotation.axis) {
            case 'x': next = y * cos - z * sin; z = y * sin + z * cos; y = next; break;
            case 'y': next = x * cos + z * sin; z = -x * sin + z * cos; x = next; break;
            default: next = x * cos - y * sin; y = x * sin + y * cos; x = next;
        }
        return new double[]{x + rotation.origin[0], y + rotation.origin[1], z + rotation.origin[2]};
    }

    private static double[] normal(double[][] vertices) {
        double ax = vertices[1][0] - vertices[0][0], ay = vertices[1][1] - vertices[0][1], az = vertices[1][2] - vertices[0][2];
        double bx = vertices[2][0] - vertices[0][0], by = vertices[2][1] - vertices[0][1], bz = vertices[2][2] - vertices[0][2];
        double nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
        double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
        return length == 0.0D ? new double[]{0, 1, 0} : new double[]{nx / length, ny / length, nz / length};
    }

    private static Bounds bounds(double[][] vertices) {
        Bounds result = new Bounds();
        for (double[] vertex : vertices) result.include(vertex[0], vertex[1], vertex[2]);
        return result;
    }

    private static int matchingVertex(double[] vertex, double[][] candidates) {
        int best = 0;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < candidates.length; ++i) {
            double dx = vertex[0] - candidates[i][0];
            double dy = vertex[1] - candidates[i][1];
            double dz = vertex[2] - candidates[i][2];
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance < bestDistance) { bestDistance = distance; best = i; }
        }
        return best;
    }

    private static float shade(double[] normal) {
        double vertical = Math.abs(normal[1]);
        if (vertical > 0.9D) return normal[1] > 0 ? 1.0F : 0.5F;
        return Math.abs(normal[2]) > Math.abs(normal[0]) ? 0.8F : 0.6F;
    }

    private static void setWorldColour(Tessellator tessellator, Block block, IBlockAccess world,
                                       int x, int y, int z, Direction direction, boolean tinted) {
        int colour = tinted ? block.colorMultiplier(world, x, y, z) : 0xFFFFFF;
        setColour(tessellator, colour, direction);
    }

    private static void setInventoryColour(Tessellator tessellator, Block block, int metadata,
                                           Direction direction, boolean tinted) {
        setColour(tessellator, tinted ? block.getRenderColor(metadata) : 0xFFFFFF, direction);
    }

    private static void setColour(Tessellator tessellator, int colour, Direction direction) {
        float red = ((colour >> 16) & 255) / 255.0F;
        float green = ((colour >> 8) & 255) / 255.0F;
        float blue = (colour & 255) / 255.0F;
        float shade = direction == Direction.DOWN ? 0.5F : direction == Direction.UP ? 1.0F
                : (direction == Direction.NORTH || direction == Direction.SOUTH ? 0.8F : 0.6F);
        tessellator.setColorOpaque_F(red * shade, green * shade, blue * shade);
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }

    private final class Binding {
        final ResourceLocation blockstate;
        final StateMapper mapper;
        final TextureResolver textureResolver;
        final OverlayResolver overlayResolver;
        final Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
        final List<MultipartPart> multipart = new ArrayList<MultipartPart>();
        Variant inventoryVariant;
        Exception error;

        Binding(ResourceLocation blockstate, StateMapper mapper, TextureResolver textureResolver,
                OverlayResolver overlayResolver) {
            this.blockstate = blockstate;
            this.mapper = mapper;
            this.textureResolver = textureResolver;
            this.overlayResolver = overlayResolver;
        }

        void load(IResourceManager resources, IIconRegister icons) throws Exception {
            variants.clear();
            multipart.clear();
            inventoryVariant = null;
            error = null;
            JsonObject root = readJson(resources, resource(blockstate, "blockstates/", ".json"));
            JsonObject sourceVariants = root.getAsJsonObject("variants");
            if (sourceVariants != null) {
                for (Map.Entry<String, JsonElement> entry : sourceVariants.entrySet()) {
                    JsonElement value = entry.getValue();
                    VariantSet set = loadVariantSet(resources, icons, value);
                    variants.put(canonicalState(entry.getKey()), set);
                }
            }
            if (root.has("multipart")) {
                for (JsonElement entry : root.getAsJsonArray("multipart")) {
                    JsonObject part = entry.getAsJsonObject();
                    Condition condition = part.has("when") ? parseCondition(part.get("when")) : Condition.ALWAYS;
                    multipart.add(new MultipartPart(condition,
                            loadVariantSet(resources, icons, part.get("apply"))));
                }
            }
            if (variants.isEmpty() && multipart.isEmpty())
                throw new IllegalArgumentException("Missing variants and multipart objects");

            // Modern block items can have a dedicated inventory model (fences, buttons, stairs,
            // slabs, and similar shapes). 1.7.10 normally renders the placed block state here,
            // which is visibly wrong for these models. Keep this optional because a number of
            // source blocks intentionally use generated/native item rendering instead.
            try {
                ResourceLocation itemModel = new ResourceLocation(blockstate.getResourceDomain(),
                        "item/" + blockstate.getResourcePath());
                ModelDefinition model = loadModel(resources, itemModel,
                        new ArrayList<ResourceLocation>());
                if (!model.elements.isEmpty()) inventoryVariant = createVariant(model, 0, 0, 1,
                        false, icons);
            } catch (Exception itemError) {
                inventoryVariant = null;
                if(!(itemError instanceof java.io.FileNotFoundException))LOGGER.warn("Invalid optional inventory model for {}",blockstate,itemError);
            }
        }

        private VariantSet loadVariantSet(IResourceManager resources, IIconRegister icons,
                                          JsonElement value) throws Exception {
            VariantSet set = new VariantSet();
            if (value.isJsonArray()) {
                for (JsonElement candidate : value.getAsJsonArray()) {
                    set.add(loadVariant(resources, icons, candidate.getAsJsonObject()));
                }
            } else {
                set.add(loadVariant(resources, icons, value.getAsJsonObject()));
            }
            return set;
        }

        private Condition parseCondition(JsonElement source) {
            if (source.isJsonArray()) {
                final List<Condition> entries = new ArrayList<Condition>();
                for (JsonElement child : source.getAsJsonArray()) entries.add(parseCondition(child));
                return new Condition() {
                    @Override public boolean matches(Map<String, String> properties) {
                        for (Condition entry : entries) if (!entry.matches(properties)) return false;
                        return true;
                    }
                };
            }
            JsonObject object = source.getAsJsonObject();
            if (object.has("OR") || object.has("AND")) {
                final boolean any = object.has("OR");
                JsonArray array = object.getAsJsonArray(any ? "OR" : "AND");
                final List<Condition> entries = new ArrayList<Condition>();
                for (JsonElement child : array) entries.add(parseCondition(child));
                return new Condition() {
                    @Override public boolean matches(Map<String, String> properties) {
                        for (Condition entry : entries)
                            if (entry.matches(properties) == any) return any;
                        return !any;
                    }
                };
            }
            final Map<String, String[]> expected = new HashMap<String, String[]>();
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
                expected.put(entry.getKey(), entry.getValue().getAsString().split("\\|"));
            return new Condition() {
                @Override public boolean matches(Map<String, String> properties) {
                    for (Map.Entry<String, String[]> entry : expected.entrySet()) {
                        String actual = properties.get(entry.getKey());
                        boolean hasPositive = false, positiveMatch = false, forbidden = false;
                        for (String value : entry.getValue()) {
                            boolean negated = value.startsWith("!");
                            boolean equal = actual != null && actual.equals(negated ? value.substring(1) : value);
                            if (negated) forbidden |= equal;
                            else { hasPositive = true; positiveMatch |= equal; }
                        }
                        if (forbidden || hasPositive && !positiveMatch) return false;
                    }
                    return true;
                }
            };
        }

        private Variant loadVariant(IResourceManager resources, IIconRegister icons,
                                    JsonObject json) throws Exception {
            if(integer(json,"x")%90!=0||integer(json,"y")%90!=0)throw new IllegalArgumentException("Blockstate rotation must be a multiple of 90");
            ResourceLocation modelLocation = blockModel(blockstate, json.get("model").getAsString());
            ModelDefinition model = loadModel(resources, modelLocation, new ArrayList<ResourceLocation>());
            return createVariant(model, integer(json, "x"), integer(json, "y"),
                    positiveInteger(json, "weight", 1),
                    json.has("uvlock") && json.get("uvlock").getAsBoolean(), icons);
        }

        private Variant createVariant(ModelDefinition model, int rotationX, int rotationY,
                                      int weight, boolean uvLock, IIconRegister icons) {
            Variant variant = new Variant(model, rotationX, rotationY, weight, uvLock);
            for (String texture : model.textures.values()) {
                String resolved = resolveTexture(model.textures, texture);
                if (resolved == null || resolved.startsWith("#")) continue;
                ResourceLocation textureLocation = textureResolver.resolve(resolved);
                variant.icons.put(resolved, icons.registerIcon(textureLocation.toString()));
            }
            return variant;
        }

        List<Variant> inventoryVariants(int metadata) {
            if (inventoryVariant == null) return variants(null, 0, 0, 0, metadata);
            List<Variant> selected = new ArrayList<Variant>();
            selected.add(inventoryVariant);
            return selected;
        }

        List<Variant> variants(IBlockAccess world, int x, int y, int z, int metadata) {
            String state = mapper.variant(world, x, y, z, metadata);
            VariantSet result = variants.get(canonicalState(state));
            if (result == null) result = variants.get("normal");
            if (result == null && !variants.isEmpty()) result = variants.values().iterator().next();
            long seed = world == null ? 0L : positionSeed(x, y, z);
            List<Variant> selected = new ArrayList<Variant>();
            if (result != null) selected.add(result.select(seed));
            if (!multipart.isEmpty()) {
                Map<String, String> properties = parseProperties(state);
                int salt = 1;
                for (MultipartPart part : multipart)
                    if (part.condition.matches(properties)) selected.add(part.variants.select(seed + 31L * salt++));
            }
            return selected;
        }
    }

    /** Block state properties are a set, independent of JSON/member order. */
    public static String canonicalState(String state) {
        if(state==null||state.length()==0||"normal".equals(state))return state;
        String[] properties=state.split(",");
        java.util.Arrays.sort(properties);
        StringBuilder result=new StringBuilder();
        for(String property:properties){if(result.length()>0)result.append(',');result.append(property);}
        return result.toString();
    }

    private static Map<String, String> parseProperties(String state) {
        Map<String, String> result = new HashMap<String, String>();
        if (state == null || state.length() == 0 || "normal".equals(state)) return result;
        for (String property : state.split(",")) {
            int equals = property.indexOf('=');
            if (equals > 0) result.put(property.substring(0, equals), property.substring(equals + 1));
        }
        return result;
    }

    private ModelDefinition loadModel(IResourceManager resources, ResourceLocation location,
                                      List<ResourceLocation> chain) throws Exception {
        ModelDefinition cached = modelCache.get(location);
        if (cached != null) return cached;
        if (chain.contains(location)) throw new IllegalArgumentException("Model parent cycle: " + chain);
        if (chain.size() >= 64) throw new IllegalArgumentException("Model inheritance exceeds 64 parents: " + location);
        chain.add(location);
        JsonObject json = readJson(resources, resource(location, "models/", ".json"));
        ModelDefinition model = new ModelDefinition();
        if (json.has("parent")) {
            String parentName = json.get("parent").getAsString();
            if(parentName.equals("builtin/generated")||parentName.equals("minecraft:builtin/generated"))model.generated=true;
            if (!parentName.startsWith("builtin/")&&!parentName.startsWith("minecraft:builtin/")) {
                ModelDefinition parent = loadModel(resources, relative(location, parentName), chain);
                model.textures.putAll(parent.textures);
                model.elements.addAll(parent.elements);
                model.generated=parent.generated;
                model.guiLight=parent.guiLight;
                model.display.putAll(parent.display);
                model.overrides.addAll(parent.overrides);
            }
        }
        if (json.has("textures")) {
            for (Map.Entry<String, JsonElement> texture : json.getAsJsonObject("textures").entrySet()) {
                model.textures.put(texture.getKey(), texture.getValue().getAsString());
            }
        }
        if (json.has("elements")) {
            model.elements.clear();
            for (JsonElement element : json.getAsJsonArray("elements")) model.elements.add(parseElement(element.getAsJsonObject()));
        }
        if(json.has("display"))for(Map.Entry<String,JsonElement> entry:json.getAsJsonObject("display").entrySet()) {
            JsonObject transform=entry.getValue().getAsJsonObject();
            for(String part:new String[]{"translation","rotation","scale"})if(transform.has(part))vector(transform.getAsJsonArray(part));
            model.display.put(entry.getKey(),transform);
        }
        if(json.has("gui_light")) {
            model.guiLight=json.get("gui_light").getAsString();
            if(!model.guiLight.equals("front")&&!model.guiLight.equals("side"))throw new IllegalArgumentException("Invalid gui_light: "+model.guiLight);
        }
        if(json.has("overrides")){model.overrides.clear();for(JsonElement entry:json.getAsJsonArray("overrides"))model.overrides.add(entry.getAsJsonObject());}
        chain.remove(chain.size() - 1);
        modelCache.put(location, model);
        return model;
    }

    private static Element parseElement(JsonObject json) {
        Element element = new Element(vector(json.getAsJsonArray("from")), vector(json.getAsJsonArray("to")));
        element.shade = !json.has("shade") || json.get("shade").getAsBoolean();
        if (json.has("rotation")) {
            JsonObject rotation = json.getAsJsonObject("rotation");
            String axis = rotation.get("axis").getAsString();
            if(!axis.equals("x")&&!axis.equals("y")&&!axis.equals("z"))throw new IllegalArgumentException("Invalid element axis: "+axis);
            element.rotation = new ElementRotation(vector(rotation.getAsJsonArray("origin")),
                    axis.charAt(0), rotation.get("angle").getAsDouble(),
                    rotation.has("rescale") && rotation.get("rescale").getAsBoolean());
        }
        JsonObject faces = json.getAsJsonObject("faces");
        for (Map.Entry<String, JsonElement> entry : faces.entrySet()) {
            Direction direction = Direction.from(entry.getKey());
            JsonObject faceJson = entry.getValue().getAsJsonObject();
            if(integer(faceJson,"rotation",0)%90!=0)throw new IllegalArgumentException("Face UV rotation must be a multiple of 90");
            Direction cull = faceJson.has("cullface") ? Direction.optional(faceJson.get("cullface").getAsString()) : null;
            double[] uv = faceJson.has("uv") ? vector4(faceJson.getAsJsonArray("uv")) : defaultUv(element, direction);
            element.faces.put(direction, new Face(direction, cull, faceJson.get("texture").getAsString(),
                    integer(faceJson, "tintindex", -1), uv, integer(faceJson, "rotation", 0)));
        }
        return element;
    }

    private static JsonObject readJson(IResourceManager resources, ResourceLocation location) throws Exception {
        IResource resource = resources.getResource(location);
        Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        try {
            return new JsonParser().parse(reader).getAsJsonObject();
        } finally {
            reader.close();
        }
    }

    private static ResourceLocation resource(ResourceLocation location, String prefix, String suffix) {
        String path = location.getResourcePath();
        return new ResourceLocation(location.getResourceDomain(), prefix + path + suffix);
    }

    private static ResourceLocation relative(ResourceLocation owner, String value) {
        int colon = value.indexOf(':');
        return colon >= 0 ? new ResourceLocation(value.substring(0, colon), value.substring(colon + 1))
                : new ResourceLocation(owner.getResourceDomain(), value);
    }

    /**
     * Blockstate model names are rooted in models/block even when vanilla omits the block/ prefix.
     * Parent references are already model-rooted and continue to use {@link #relative} directly.
     */
    private static ResourceLocation blockModel(ResourceLocation owner, String value) {
        ResourceLocation location = relative(owner, value);
        String path = location.getResourcePath();
        if (path.indexOf('/') < 0) path = "block/" + path;
        return new ResourceLocation(location.getResourceDomain(), path);
    }

    private static int integer(JsonObject json, String key) {
        return json.has(key) ? json.get(key).getAsInt() : 0;
    }

    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    private static int positiveInteger(JsonObject json, String key, int fallback) {
        int value = integer(json, key, fallback);
        if (value <= 0) throw new IllegalArgumentException(key + " must be positive");
        return value;
    }

    /** Same coordinate-stable contract as the baked-model random used by modern Minecraft. */
    private static long positionSeed(int x, int y, int z) {
        long seed = (long)(x * 3129871) ^ (long)z * 116129781L ^ (long)y;
        return seed * seed * 42317861L + seed * 11L;
    }

    private static double[] vector(JsonArray json) {
        if(json==null||json.size()!=3)throw new IllegalArgumentException("Expected a three-component vector");
        for(JsonElement value:json)if(Double.isNaN(value.getAsDouble())||Double.isInfinite(value.getAsDouble()))throw new IllegalArgumentException("Non-finite model vector");
        return new double[]{json.get(0).getAsDouble(), json.get(1).getAsDouble(), json.get(2).getAsDouble()};
    }

    private static double[] vector4(JsonArray json) {
        if(json==null||json.size()!=4)throw new IllegalArgumentException("Expected four UV coordinates");
        for(JsonElement value:json)if(Double.isNaN(value.getAsDouble())||Double.isInfinite(value.getAsDouble()))throw new IllegalArgumentException("Non-finite model UV");
        return new double[]{json.get(0).getAsDouble(), json.get(1).getAsDouble(),
                json.get(2).getAsDouble(), json.get(3).getAsDouble()};
    }

    private static double[] defaultUv(Element element, Direction direction) {
        if (direction == Direction.DOWN)
            return new double[]{element.from[0],16-element.to[2],element.to[0],16-element.from[2]};
        if (direction == Direction.UP)
            return new double[]{element.from[0], element.from[2], element.to[0], element.to[2]};
        if (direction == Direction.NORTH)
            return new double[]{16-element.to[0],16-element.to[1],16-element.from[0],16-element.from[1]};
        if (direction == Direction.SOUTH)
            return new double[]{element.from[0], 16.0D - element.to[1], element.to[0], 16.0D - element.from[1]};
        if(direction==Direction.WEST)return new double[]{element.from[2], 16.0D - element.to[1], element.to[2], 16.0D - element.from[1]};
        return new double[]{16-element.to[2],16-element.to[1],16-element.from[2],16-element.from[1]};
    }

    private static String resolveTexture(Map<String, String> textures, String reference) {
        String value = reference;
        java.util.Set<String> visited=new java.util.HashSet<String>();
        while(value!=null&&value.startsWith("#")) {
            if(!visited.add(value))throw new IllegalArgumentException("Texture alias cycle: "+reference);
            value=textures.get(value.substring(1));
        }
        if(value==null)throw new IllegalArgumentException("Missing texture alias: "+reference);
        return value;
    }

    private static Bounds rotateBounds(Element element, int rotationX, int rotationY) {
        Bounds bounds = new Bounds();
        for (int xi = 0; xi < 2; ++xi) for (int yi = 0; yi < 2; ++yi) for (int zi = 0; zi < 2; ++zi) {
            double[] point = rotatePoint(xi == 0 ? element.from[0] : element.to[0],
                    yi == 0 ? element.from[1] : element.to[1], zi == 0 ? element.from[2] : element.to[2],
                    rotationX, rotationY);
            bounds.include(point[0], point[1], point[2]);
        }
        return bounds;
    }

    private static double[] rotatePoint(double x, double y, double z, int rotationX, int rotationY) {
        int turnsX = ((rotationX % 360) + 360) % 360 / 90;
        int turnsY = ((rotationY % 360) + 360) % 360 / 90;
        // Blockstate angles are clockwise (negative mathematical rotations).
        for (int i = 0; i < turnsX; ++i) { double oldY = y; y = z; z = 16.0D - oldY; }
        for (int i = 0; i < turnsY; ++i) { double oldX = x; x = 16.0D - z; z = oldX; }
        return new double[]{x, y, z};
    }

    private static Direction rotate(Direction direction, int rotationX, int rotationY) {
        int x = direction.x, y = direction.y, z = direction.z;
        int turnsX = ((rotationX % 360) + 360) % 360 / 90;
        int turnsY = ((rotationY % 360) + 360) % 360 / 90;
        for (int i = 0; i < turnsX; ++i) { int oldY = y; y = z; z = -oldY; }
        for (int i = 0; i < turnsY; ++i) { int oldX = x; x = -z; z = oldX; }
        return Direction.fromVector(x, y, z);
    }


    /** Opt-in item bridge. Builtin/entity items retain their dedicated TESR adapters. */
    public net.minecraftforge.client.IItemRenderer registerItem(net.minecraft.item.Item item,ResourceLocation model,TextureResolver textures) {
        if(item==null)throw new IllegalArgumentException("Unregistered item for "+model);
        ItemBinding binding=new ItemBinding(item,model,textures);itemBindings.put(item,binding);
        net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer(item,binding);return binding;
    }

    public void applyItemDisplay(net.minecraft.item.Item item,String context,boolean left) {
        ItemBinding binding=itemBindings.get(item);if(binding==null)return;
        Variant variant=binding.models.get(binding.location);if(variant==null)return;
        applyDisplay(variant.model,context,left);
    }

    public IIcon itemIcon(net.minecraft.item.ItemStack stack,Object holder) {
        ItemBinding binding=itemBindings.get(stack.getItem());if(binding==null)return null;
        Variant model=binding.selected(stack,holder);if(model==null)return null;
        String texture=model.model.textures.get("layer0");return texture==null?null:model.icons.get(resolveTexture(model.model.textures,texture));
    }

    private static void applyDisplay(ModelDefinition model,String context,boolean left) {
        JsonObject display=model.display.get(context);
        if(display==null&&context.endsWith("lefthand"))display=model.display.get(context.replace("lefthand","righthand"));
        if(display==null)return;
        double[] t=display.has("translation")?vector(display.getAsJsonArray("translation")):new double[]{0,0,0};
        double[] r=display.has("rotation")?vector(display.getAsJsonArray("rotation")):new double[]{0,0,0};
        double[] z=display.has("scale")?vector(display.getAsJsonArray("scale")):new double[]{1,1,1};
        // vector() keeps JSON units; translations alone are converted from sixteenths.
        GL11.glTranslated(Math.max(-5,Math.min(5,(left?-t[0]:t[0])/16)),Math.max(-5,Math.min(5,t[1]/16)),Math.max(-5,Math.min(5,t[2]/16)));
        GL11.glRotated(r[0],1,0,0);GL11.glRotated(left?-r[1]:r[1],0,1,0);GL11.glRotated(left?-r[2]:r[2],0,0,1);
        GL11.glScaled(Math.max(-4,Math.min(4,z[0])),Math.max(-4,Math.min(4,z[1])),Math.max(-4,Math.min(4,z[2])));
    }

    /** Last matching override wins, as in the modern item model resolver. */
    public static String selectOverride(List<JsonObject> overrides,Map<String,Float> values) {
        String selected=null;
        for(JsonObject override:overrides){boolean matches=true;
            for(Map.Entry<String,JsonElement> predicate:override.getAsJsonObject("predicate").entrySet()) {
                Float value=values.get(predicate.getKey());if(value==null||value<predicate.getValue().getAsFloat()){matches=false;break;}
            }
            if(matches)selected=override.get("model").getAsString();
        }
        return selected;
    }

    private final class ItemBinding implements net.minecraftforge.client.IItemRenderer {
        final net.minecraft.item.Item item;final ResourceLocation location;final TextureResolver textures;
        final Map<ResourceLocation,Variant> models=new HashMap<ResourceLocation,Variant>();
        ItemBinding(net.minecraft.item.Item item,ResourceLocation location,TextureResolver textures){this.item=item;this.location=location;this.textures=textures;}
        void load(IResourceManager resources,IIconRegister atlas,ResourceLocation id)throws Exception {
            if(models.containsKey(id))return;
            ModelDefinition model=loadModel(resources,id,new ArrayList<ResourceLocation>());
            if(!model.generated&&model.elements.isEmpty())throw new IllegalArgumentException("Unsupported empty/builtin entity item: "+id);
            Binding baker=new Binding(id,null,textures,null);
            models.put(id,baker.createVariant(model,0,0,1,false,atlas));
            for(JsonObject override:model.overrides)load(resources,atlas,relative(id,override.get("model").getAsString()));
        }
        Variant selected(net.minecraft.item.ItemStack stack,Object... data) {
            Variant base=models.get(location);if(base==null)return null;
            Map<String,Float> values=new HashMap<String,Float>();
            if(stack.getItem() instanceof com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemLodestoneCompass116)
                values.put("angle",com.mod.htgt6.minecraft1202.Module.Client.Modern.LodestoneCompassClient116.angle(stack,data));
            if(stack.getItem() instanceof com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow) {
                boolean charged=com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow.isCharged(stack);
                net.minecraft.item.ItemStack projectile=com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow.loadedProjectile(stack);
                values.put("charged",charged?1F:0F);values.put("firework",charged&&projectile!=null&&projectile.getItem()==net.minecraft.init.Items.fireworks?1F:0F);
                int elapsed=-1;for(Object value:data)if(value instanceof net.minecraft.entity.player.EntityPlayer) {
                    net.minecraft.entity.player.EntityPlayer player=(net.minecraft.entity.player.EntityPlayer)value;
                    elapsed=player.getItemInUse()==stack?player.getItemInUseDuration():com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120.renderUseTicks(player,stack);
                } else if(value instanceof com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager) {
                    com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager mob=(com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager)value;
                    if(mob.getHeldItem()==stack&&mob.getCrossbowChargeTicks()>0)elapsed=mob.getCrossbowChargeTicks();
                }
                for(Object value:data)if(value instanceof com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116){
                    com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116 piglin=(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116)value;
                    if(piglin.getHeldItem()==stack&&piglin.getCrossbowChargeTicks()>0)elapsed=piglin.getCrossbowChargeTicks();
                }
                values.put("pulling",!charged&&elapsed>=0?1F:0F);values.put("pull",Math.max(0,elapsed)/(float)com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow.chargeDuration(stack));
            }
            String replacement=selectOverride(base.model.overrides,values);
            Variant selected=replacement==null?base:models.get(relative(location,replacement));return selected==null?base:selected;
        }
        public boolean handleRenderType(net.minecraft.item.ItemStack stack,ItemRenderType type){return models.containsKey(location)&&type!=ItemRenderType.FIRST_PERSON_MAP;}
        public boolean shouldUseRenderHelper(ItemRenderType type,net.minecraft.item.ItemStack stack,ItemRendererHelper helper){
            if(type==ItemRenderType.EQUIPPED||type==ItemRenderType.EQUIPPED_FIRST_PERSON)return helper==ItemRendererHelper.EQUIPPED_BLOCK||helper==ItemRendererHelper.BLOCK_3D;
            return type==ItemRenderType.ENTITY&&(helper==ItemRendererHelper.ENTITY_BOBBING||helper==ItemRendererHelper.ENTITY_ROTATION||helper==ItemRendererHelper.BLOCK_3D);
        }
        public void renderItem(ItemRenderType type,net.minecraft.item.ItemStack stack,Object... data) {
            // Forge inventory rendering supplies RenderBlocks only; recover the actual
            // use owner so the hotbar follows the same charging override as the hand.
            Variant model=type==ItemRenderType.INVENTORY?selected(stack,Minecraft.getMinecraft().thePlayer):selected(stack,data);if(model==null)return;
            try(com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112 guard=com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112.capture()) {
                GL11.glPushMatrix();try {
                    GL11.glEnable(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL);GL11.glEnable(GL11.GL_ALPHA_TEST);
                    GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
                    Minecraft.getMinecraft().getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture);
                    String context;
                    if(type==ItemRenderType.INVENTORY){
                        if("front".equals(model.model.guiLight)||model.model.guiLight==null&&model.model.generated)GL11.glDisable(GL11.GL_LIGHTING);else net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                        GL11.glTranslatef(8,8,0);GL11.glScalef(16,-16,16);GL11.glDisable(GL11.GL_CULL_FACE);context="gui";
                    } else if(type==ItemRenderType.ENTITY){
                        // Undo Forge's legacy size before applying the JSON ground/fixed scale.
                        int render=stack.getItem() instanceof net.minecraft.item.ItemBlock?Block.getBlockFromItem(stack.getItem()).getRenderType():1;
                        float inverse=render==1||render==19||render==12||render==2?2:4;GL11.glScalef(inverse,inverse,inverse);
                        context=net.minecraft.client.renderer.entity.RenderItem.renderInFrame?"fixed":"ground";
                        if(net.minecraft.client.renderer.entity.RenderItem.renderInFrame){GL11.glRotatef(90,0,1,0);GL11.glTranslatef(0,-.05F,0);GL11.glScalef(.8F,.8F,.8F);}
                    } else {
                        GL11.glTranslatef(.5F,.5F,.5F); // Inverse Forge EQUIPPED_BLOCK pivot.
                        if(type==ItemRenderType.EQUIPPED_FIRST_PERSON){GL11.glScalef(2.5F,2.5F,2.5F);GL11.glRotatef(-45,0,1,0);context="firstperson_righthand";}
                        else {
                            GL11.glScalef(-1/.375F,-1/.375F,1/.375F);GL11.glRotatef(-45,0,1,0);GL11.glRotatef(-20,1,0,0);GL11.glTranslatef(0,-.1875F,.3125F);
                            GL11.glTranslatef(.0625F,-.4375F,-.0625F);GL11.glRotatef(-90,1,0,0);GL11.glRotatef(180,0,1,0);GL11.glTranslatef(.0625F,.125F,-.625F);
                            context="thirdperson_righthand";
                        }
                    }
                    applyDisplay(model.model,context,false);
                    GL11.glTranslatef(-.5F,-.5F,-.5F);
                    if(model.model.generated) {
                        GL11.glTranslatef(0,0,.53125F);
                        int layers=0;
                        for(int layer=0;layer<5;layer++) {
                            String name=model.model.textures.get("layer"+layer);if(name==null)continue;
                            IIcon icon=model.icons.get(resolveTexture(model.model.textures,name));if(icon==null)continue;
                            int color=stack.getItem().getColorFromItemStack(stack,layer);GL11.glColor4f((color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F,1);
                            net.minecraft.client.renderer.ItemRenderer.renderItemIn2D(Tessellator.instance,icon.getMaxU(),icon.getMinV(),icon.getMinU(),icon.getMaxV(),icon.getIconWidth(),icon.getIconHeight(),.0625F);
                            GL11.glTranslatef(0,0,.0005F);
                            layers++;
                        }
                        if(stack.hasEffect()){GL11.glTranslatef(0,0,-layers*.0005F);com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.CrossbowItemRenderer139.renderGlint();}
                    } else for(Element element:model.model.elements)for(Face face:element.faces.values()) {
                        IIcon icon=model.icons.get(resolveTexture(model.model.textures,face.texture));if(icon==null)continue;
                        Tessellator.instance.startDrawingQuads();renderModelFace(Tessellator.instance,element,face,model,icon,0,0,0,net.minecraft.init.Blocks.air,null,stack.getItemDamage(),true,face.tintIndex>=0?stack.getItem().getColorFromItemStack(stack,face.tintIndex):0xFFFFFF);Tessellator.instance.draw();
                    }
                }finally{GL11.glPopMatrix();}
            }
        }
    }

    private static final class ModelDefinition {
        boolean generated;
        String guiLight;
        final Map<String,JsonObject> display=new HashMap<String,JsonObject>();
        final List<JsonObject> overrides=new ArrayList<JsonObject>();
        final Map<String, String> textures = new HashMap<String, String>();
        final List<Element> elements = new ArrayList<Element>();
    }

    private static final class Variant {
        final ModelDefinition model;
        final int rotationX, rotationY, weight;
        final boolean uvLock;
        final Map<String, IIcon> icons = new HashMap<String, IIcon>();
        Variant(ModelDefinition model, int rotationX, int rotationY, int weight, boolean uvLock) {
            this.model = model; this.rotationX = rotationX; this.rotationY = rotationY;
            this.weight = weight; this.uvLock = uvLock;
        }
    }

    private static final class VariantSet {
        final List<Variant> entries = new ArrayList<Variant>();
        int totalWeight;

        void add(Variant variant) {
            entries.add(variant);
            totalWeight += variant.weight;
        }

        Variant select(long seed) {
            if (entries.isEmpty()) return null;
            int choice = (int)((seed >>> 16) % totalWeight);
            for (Variant variant : entries) {
                choice -= variant.weight;
                if (choice < 0) return variant;
            }
            return entries.get(entries.size() - 1);
        }
    }

    private interface Condition {
        boolean matches(Map<String, String> properties);
        Condition ALWAYS = new Condition() {
            @Override public boolean matches(Map<String, String> properties) { return true; }
        };
    }

    private static final class MultipartPart {
        final Condition condition;
        final VariantSet variants;
        MultipartPart(Condition condition, VariantSet variants) {
            this.condition = condition; this.variants = variants;
        }
    }

    private static final class Element {
        final double[] from, to;
        final Map<Direction, Face> faces = new LinkedHashMap<Direction, Face>();
        ElementRotation rotation;
        boolean shade = true;
        Element(double[] from, double[] to) { this.from = from; this.to = to; }
    }

    private static final class ElementRotation {
        final double[] origin;
        final char axis;
        final double angle;
        final boolean rescale;
        ElementRotation(double[] origin, char axis, double angle, boolean rescale) {
            this.origin = origin; this.axis = axis; this.angle = angle; this.rescale = rescale;
        }
    }

    private static final class Face {
        final Direction direction, cull;
        final String texture;
        final int tintIndex;
        final double[] uv;
        final int uvRotation;
        Face(Direction direction, Direction cull, String texture, int tintIndex, double[] uv, int uvRotation) {
            this.direction = direction; this.cull = cull; this.texture = texture;
            this.tintIndex = tintIndex; this.uv = uv; this.uvRotation = uvRotation;
        }
    }

    private static final class Bounds {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        void include(double x, double y, double z) {
            minX = Math.min(minX, x); minY = Math.min(minY, y); minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x); maxY = Math.max(maxY, y); maxZ = Math.max(maxZ, z);
        }
    }

    private enum Direction {
        DOWN(0, 0, -1, 0), UP(1, 0, 1, 0), NORTH(2, 0, 0, -1),
        SOUTH(3, 0, 0, 1), WEST(4, -1, 0, 0), EAST(5, 1, 0, 0);
        final int side, x, y, z;
        Direction(int side, int x, int y, int z) { this.side = side; this.x = x; this.y = y; this.z = z; }
        static Direction from(String value) { return valueOf(value.toUpperCase(java.util.Locale.ROOT)); }
        // Vanilla Direction.byName returns null for an unknown cullface (including
        // "bottom" in the official scaffolding model). Face names remain strict.
        static Direction optional(String value) {
            for (Direction direction : values())
                if (direction.name().equalsIgnoreCase(value)) return direction;
            return null;
        }
        static Direction fromVector(int x, int y, int z) {
            for (Direction direction : values()) if (direction.x == x && direction.y == y && direction.z == z) return direction;
            throw new IllegalArgumentException("Invalid face normal " + x + "," + y + "," + z);
        }
    }
}
