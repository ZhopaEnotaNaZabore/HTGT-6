package com.mod.htgt6.orehelper.client;

import com.mod.htgt6.orehelper.OreHelperNetwork;
import com.mod.htgt6.orehelper.OreDisplayStacks;
import gregapi.oredict.OreDictMaterial;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class OreHelperScreen extends GuiScreen {
    private static final int W = 400, H = 284;
    private static final AtomicInteger TOKENS = new AtomicInteger();
    private static final ResourceLocation PLANETS = new ResourceLocation("htgt6", "textures/gui/orehelper/microplanets.png");
    private static final RenderItem ITEMS = new RenderItem();
    private final GuiScreen parent;
    private final int material, token = TOKENS.incrementAndGet();
    private final List<NBTTagCompound> entries = new ArrayList<NBTTagCompound>();
    private final List<Hit> hits = new ArrayList<Hit>();
    private final List<RenderedSlot> itemSlots = new ArrayList<RenderedSlot>();
    private static final class RenderedSlot {
        final ItemStack stack; final int x, y;
        RenderedSlot(ItemStack stack, int x, int y) { this.stack=stack; this.x=x; this.y=y; }
    }
    private final Map<NBTTagCompound, DisplayEntry> displayEntries = new IdentityHashMap<NBTTagCompound, DisplayEntry>();
    private final Map<NBTTagCompound, Set<Integer>> dimensionIndex = new IdentityHashMap<NBTTagCompound, Set<Integer>>();
    private List<NBTTagCompound> visibleEntries = Collections.emptyList();
    private final Map<Integer, NBTTagCompound> worldLookup = new TreeMap<Integer, NBTTagCompound>();
    private List<NBTTagCompound> worldList = Collections.emptyList();
    private int revision, cachedRevision = -1, cachedTab = -1;
    private Integer cachedDimension;
    private int hoverX, hoverY;
    private static final class DisplayEntry {
        final ItemStack[] hosts, ores, rocks, flowers;
        DisplayEntry(NBTTagCompound n) {
            rocks = decode(n.getTagList("rockIndicators", 10));
            flowers = decode(n.getTagList("indicators", 10));
            NBTTagList hostTags = n.getTagList("hosts", 10);
            hosts = new ItemStack[hostTags.tagCount()];
            for (int i = 0; i < hosts.length; i++) hosts[i] = ItemStack.loadItemStackFromNBT(hostTags.getCompoundTagAt(i));
            int[] ids = n.getIntArray("ores"); ores = new ItemStack[ids.length];
            for (int i = 0; i < ids.length; i++) ores[i] = OreDisplayStacks.resolve(n.getInteger("type"), ids[i], hosts);
        }
        private static ItemStack[] decode(NBTTagList tags) {
            ItemStack[] stacks = new ItemStack[tags.tagCount()];
            for (int i = 0; i < stacks.length; i++) stacks[i] = ItemStack.loadItemStackFromNBT(tags.getCompoundTagAt(i));
            return stacks;
        }
    }
    private DisplayEntry display(NBTTagCompound n) {
        DisplayEntry entry = displayEntries.get(n);
        if (entry == null) { entry = new DisplayEntry(n); displayEntries.put(n, entry); }
        return entry;
    }
    private boolean requested, done, failed;
    private long requestedAt;
    private int tab, index, hostPage, worldPage;
    private Integer dimension;
    private float scale;
    private int left, top;
    private static final class Hit {
        int x, y, w, h; List<String> tip; Runnable action;
        Hit(int x, int y, int w, int h, List<String> tip, Runnable action) {
            this.x=x; this.y=y; this.w=w; this.h=h; this.tip=tip; this.action=action;
        }
        boolean at(int x, int y) { return x>=this.x && y>=this.y && x<this.x+w && y<this.y+h; }
    }
    public OreHelperScreen(GuiScreen parent, int material) { this.parent = parent; this.material = material; }
    public void initGui() {
        scale = Math.min(1f, Math.min((width - 8f) / W, (height - 8f) / H));
        left = (int) ((width - W * scale) / 2); top = (int) ((height - H * scale) / 2);
        if (!requested) {
            requested = true; requestedAt = System.currentTimeMillis();
            OreHelperNetwork.CHANNEL.sendToServer(new OreHelperNetwork.Request(material, token));
        }
    }
    public void receive(NBTTagCompound n) {
        if (n.getInteger("token") != token) return;
        if (n.getBoolean("done")) {
            done = true;
            failed = n.getBoolean("failed");
            if (!entries.isEmpty() && filtered().isEmpty()) tab = entries.get(0).getInteger("type");
        } else { entries.add(n); revision++; }
    }
    public boolean doesGuiPauseGame() { return false; }
    private static String tr(String key, Object... args) { return I18n.format("ht6.ore." + key, args); }
    private static String materialName(int id) {
        return id > 0 && id < OreDictMaterial.MATERIAL_ARRAY.length && OreDictMaterial.MATERIAL_ARRAY[id] != null ?
                OreDictMaterial.MATERIAL_ARRAY[id].mNameLocal : "#" + id;
    }
    private List<NBTTagCompound> filtered() {
        if (cachedRevision == revision && cachedTab == tab && Objects.equals(cachedDimension, dimension)) return visibleEntries;
        List<NBTTagCompound> list = new ArrayList<NBTTagCompound>();
        worldLookup.clear();
        for (NBTTagCompound n : entries) {
            if (n.getInteger("type") != tab) continue;
            NBTTagList worlds = n.getTagList("worlds", 10);
            for (int i = 0; i < worlds.tagCount(); i++) {
                NBTTagCompound world = worlds.getCompoundTagAt(i);
                worldLookup.put(world.getInteger("id"), world);
            }
            if (dimension == null || hasDimension(n, dimension)) list.add(n);
        }
        worldList = new ArrayList<NBTTagCompound>(worldLookup.values());
        cachedRevision = revision; cachedTab = tab; cachedDimension = dimension;
        return visibleEntries = list;
    }
    private boolean hasDimension(NBTTagCompound n, int id) {
        Set<Integer> dimensions = dimensionIndex.get(n);
        if (dimensions == null) {
            dimensions = new HashSet<Integer>();
            NBTTagList worlds = n.getTagList("worlds", 10);
            for (int i = 0; i < worlds.tagCount(); i++) dimensions.add(worlds.getCompoundTagAt(i).getInteger("id"));
            dimensionIndex.put(n, dimensions);
        }
        return dimensions.contains(id);
    }
    public void drawScreen(int mx, int my, float partial) {
        drawDefaultBackground(); hits.clear(); itemSlots.clear();
        int x = (int)((mx-left)/scale), y = (int)((my-top)/scale);
        hoverX = x; hoverY = y;
        GL11.glPushMatrix(); GL11.glTranslatef(left, top, 0); GL11.glScalef(scale, scale, 1);
        panel(0, 0, W, H, 0xffc6c6c6);
        text(tr("title") + " — " + materialName(material), 10, 10, 357);
        button("x", 374, 5, 19, new Runnable() { public void run() { mc.displayGuiScreen(parent); } });
        for (int i=0; i<4; i++) {
            final int t=i;
            panel(8+i*96, 27, 94, 19, tab==i ? 0xffdedede : 0xff999999);
            text(tr("tab."+i), 13+i*96, 33, 84);
            hits.add(new Hit(8+i*96,27,94,19,null,new Runnable(){public void run(){tab=t;index=hostPage=worldPage=0;dimension=null;}}));
        }
        List<NBTTagCompound> visible = filtered();
        if (!visible.isEmpty()) {
            index = Math.min(index, visible.size()-1);
            final int count = visible.size();
            NBTTagCompound n = visible.get(index);
            DisplayEntry display = display(n);
            text(n.getString("name"), 10, 53, 278);
            text((index+1)+" / "+count, 291, 53, 62);
            button("<", 351, 48, 18, new Runnable(){public void run(){index=(index+count-1)%count;hostPage=0;}});
            button(">", 373, 48, 18, new Runnable(){public void run(){index=(index+1)%count;hostPage=0;}});
            indicators(n, display);
            int[] ores=n.getIntArray("ores");
            for(int i=0;i<ores.length;i++) {
                final int id=ores[i]; int sx=12+i*96;
                if(id==material) drawRect(sx-2,100,sx+19,121,0xff50994e);
                ItemStack stack=display.ores[i];
                slot(stack,sx,102);
                text(tr(ores.length==4 ? "role."+i : "ore"),sx+22,101,70);
                text(materialName(id),sx+22,112,70);
                if(id>0) hits.add(new Hit(sx,102,18,18,Arrays.asList(materialName(id),tr("related")),new Runnable(){public void run(){mc.displayGuiScreen(new OreHelperScreen(OreHelperScreen.this,id));}}));
            }
            text(tr("height", n.getInteger("min"), n.getInteger("max")), 12, 129, 178);
            if(tab==0) {
                text(tr("weight",n.getInteger("weight")),202,129,185);
                text(tr("density",n.getInteger("density"),n.getInteger("size")),12,142,375);
                text(tr("biomes")+": "+tr("unrestricted")+" · "+tr("distance",n.getInteger("distance")),12,155,375);
            } else if(tab==1) {
                text(tr("chance",String.format(Locale.ROOT,"%.5f",100.0*n.getLong("chance")/648648000.0)),202,129,185);
                text(tr(n.getBoolean("boundary") ? "boundary" : "layer"),12,142,375);
                NBTTagList b=n.getTagList("biomes",8); List<String> names=new ArrayList<String>();
                for(int i=0;i<b.tagCount();i++) names.add(b.getStringTagAt(i));
                text(tr("biomes")+": "+(names.isEmpty()?tr("unrestricted"):String.join(", ",names)),12,155,375);
            } else if(tab==2) {
                int amount=n.getInteger("amount");
                text(tr("attempts",Math.max(1,amount/2),Math.max(1,2*(amount/2))),12,142,375);
                text(tr("biomes")+": "+tr("unrestricted")+" · "+tr("placement"),12,155,375);
            } else {
                text(tr("probability",n.getInteger("probability")),202,129,185);
                text(tr("bedrock"),12,142,375);
                text(tr("indicators",tr(n.getBoolean("rocks")?"yes":"no"),tr(n.getBoolean("flowers")?"yes":"no")),12,155,375);
            }
            text(tr("hosts")+(n.getBoolean("hostRegistry")?" *":""),12,172,280);
            if(n.getBoolean("hostRegistry")) hits.add(new Hit(10,169,330,15,Arrays.asList(tr("hostNote")),null));
            final ItemStack[] hosts=display.hosts;
            final int pages=Math.max(1,(hosts.length+16)/17);
            hostPage=Math.min(hostPage,pages-1);
            for(int i=0;i<17 && hostPage*17+i<hosts.length;i++) {
                ItemStack stack=hosts[hostPage*17+i];
                slot(stack,12+i*20,187);
                if(stack!=null && hoverX>=12+i*20 && hoverX<30+i*20 && hoverY>=187 && hoverY<205)
                    hits.add(new Hit(12+i*20,187,18,18,stack.getTooltip(mc.thePlayer,false),null));
            }
            button("<",354,186,17,new Runnable(){public void run(){hostPage=(hostPage+pages-1)%pages;}});
            button(">",374,186,17,new Runnable(){public void run(){hostPage=(hostPage+1)%pages;}});
            text(tr("hostPage",hostPage+1,pages),12,210,378);
            planets(n);
        } else {
            text(done?tr("empty"):tr("loading"),12,65,374);
            if(!done && System.currentTimeMillis()-requestedAt>15000) text(tr("timeout"),12,82,374);
        }
        text(tr(failed?"failed":done?"server":"loading"),10,269,375);
        drawItems();
        for(Hit hit:hits) if(hit.tip!=null && hit.at(x,y)) {
            List<String> lines=new ArrayList<String>();
            for(String line:hit.tip) lines.addAll(fontRendererObj.listFormattedStringToWidth(line,300));
            // Keep the tooltip above the planet row and within the logical panel.
            drawHoveringText(lines,Math.max(10,Math.min(x,80)),Math.max(18,Math.min(y,240)-lines.size()*10),fontRendererObj);
            break;
        }
        GL11.glPopMatrix(); GL11.glColor4f(1,1,1,1);
    }
    private void indicators(NBTTagCompound n, DisplayEntry entry) {
        text(tr("indicatorRow"),12,79,100);
        int x = 118;
        for (ItemStack rock : entry.rocks) { indicatorSlot(rock,x,"indicatorRock"); x += 22; }
        for (ItemStack flower : entry.flowers) { indicatorSlot(flower,x,"indicatorFlower"); x += 22; }
        if (entry.rocks.length == 0 && entry.flowers.length == 0)
            text(tr(n.getBoolean("rocks") || n.getBoolean("flowers") ? "indicatorUnavailable" : "indicatorNone"),118,79,267);
        else if (n.getBoolean("rocks") && entry.rocks.length == 0)
            text(tr("indicatorUnavailable"),x+2,79,386-x);
    }
    private void indicatorSlot(ItemStack stack, int x, String kind) {
        slot(stack,x,74);
        if (stack != null && hoverX>=x && hoverX<x+18 && hoverY>=74 && hoverY<92) {
            List<String> tooltip = new ArrayList<String>();
            tooltip.add(tr(kind)); tooltip.addAll(stack.getTooltip(mc.thePlayer,false));
            tooltip.add(tr("indicatorNote"));
            hits.add(new Hit(x,74,18,18,tooltip,null));
        }
    }
    private void planets(NBTTagCompound current) {
        final Map<Integer,NBTTagCompound> worlds=worldLookup;
        text(tr("worlds")+": "+(dimension==null?tr("all"):worlds.get(dimension).getString("name")),12,227,286);
        button(tr("all"),303,222,44,new Runnable(){public void run(){dimension=null;index=hostPage=0;}});
        final int pages=Math.max(1,(worlds.size()+15)/16);
        worldPage=Math.min(worldPage,pages-1);
        button("<",351,222,18,new Runnable(){public void run(){worldPage=(worldPage+pages-1)%pages;}});
        button(">",373,222,18,new Runnable(){public void run(){worldPage=(worldPage+1)%pages;}});
        List<NBTTagCompound> list=worldList;
        for(int i=0;i<16 && worldPage*16+i<list.size();i++) {
            final NBTTagCompound world=list.get(worldPage*16+i); final int id=world.getInteger("id"); int x=12+i*24;
            panel(x,244,22,22,dimension!=null && dimension==id?0xff73ac67:hasDimension(current,id)?0xff999999:0xff666666);
            planet(x+1,245,icon(world));
            List<String> tip=new ArrayList<String>(); tip.add(world.getString("name")+" (ID: "+id+")");
            tip.add("GT6: "+world.getString("route"));
            if(!world.getString("biome").isEmpty()) tip.add(tr("biomes")+": "+world.getString("biome"));
            tip.add(tr(hasDimension(current,id)?"worldMatch":"worldOther"));
            hits.add(new Hit(x,244,22,22,tip,new Runnable(){public void run(){dimension=id;index=hostPage=0;}}));
        }
    }
    private static int icon(NBTTagCompound world) {
        String r=world.getString("route");
        if(world.getInteger("id")==0)return 0;
        if("NETHER".equals(r))return 1;
        if("END".equals(r))return 2;
        if("MOON".equals(r))return 3;
        if("MARS".equals(r))return 4;
        if("ASTEROIDS".equals(r))return 5;
        if(world.getString("name").toLowerCase(Locale.ROOT).contains("gliese"))return 6;
        return 7;
    }
    private void planet(int x,int y,int icon) {
        mc.getTextureManager().bindTexture(PLANETS); GL11.glColor4f(1,1,1,1);
        GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
        float u=icon/8f,v=(icon+1)/8f;
        Tessellator t=Tessellator.instance;t.startDrawingQuads();
        t.addVertexWithUV(x,y+20,0,u,1);t.addVertexWithUV(x+20,y+20,0,v,1);
        t.addVertexWithUV(x+20,y,0,v,0);t.addVertexWithUV(x,y,0,u,0);t.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }
    private void slot(ItemStack stack,int x,int y) {
        drawRect(x-1,y-1,x+17,y+17,0xff373737);drawRect(x,y,x+17,y+17,0xff8b8b8b);
        if(stack!=null) itemSlots.add(new RenderedSlot(stack,x,y));
    }
    private void drawItems() {
        if(!itemSlots.isEmpty()) {
            // Background rectangles must not hide the 3D block at the same depth.
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS); GL11.glPushMatrix();
            float oldZ = ITEMS.zLevel;
            try {
                GL11.glTranslatef(0,0,100); GL11.glColor4f(1,1,1,1);
                GL11.glEnable(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL); GL11.glEnable(GL11.GL_DEPTH_TEST);
                RenderHelper.enableGUIStandardItemLighting(); ITEMS.zLevel = 100;
                for (RenderedSlot slot : itemSlots)
                    ITEMS.renderItemAndEffectIntoGUI(fontRendererObj,mc.getTextureManager(),slot.stack,slot.x,slot.y);
            } finally {
                ITEMS.zLevel = oldZ; RenderHelper.disableStandardItemLighting();
                GL11.glPopMatrix(); GL11.glPopAttrib();
            }
        }
    }
    private void panel(int x,int y,int w,int h,int color) {
        drawRect(x,y,x+w,y+h,0xff373737);drawRect(x+1,y+1,x+w-1,y+h-1,0xffffffff);drawRect(x+2,y+2,x+w-2,y+h-2,color);
    }
    private void text(String s,int x,int y,int max) {
        String rendered=s;
        if(fontRendererObj.getStringWidth(s)>max) {
            rendered=fontRendererObj.trimStringToWidth(s,max-9)+"...";
            hits.add(new Hit(x,y,max,10,Arrays.asList(s),null));
        }
        fontRendererObj.drawString(rendered,x,y,0x303030);
    }
    private void button(String label,int x,int y,int w,Runnable action) {
        panel(x,y,w,18,0xffa0a0a0); text(label,x+4,y+5,w-6);hits.add(new Hit(x,y,w,18,null,action));
    }
    protected void mouseClicked(int mx,int my,int button) {
        if(button!=0)return;
        int x=(int)((mx-left)/scale),y=(int)((my-top)/scale);
        for(Hit h:hits)if(h.action!=null && h.at(x,y)){h.action.run();break;}
    }
    protected void keyTyped(char c,int key) { if(key==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent); }
    public void handleMouseInput() {
        super.handleMouseInput();int wheel=Mouse.getEventDWheel();
        if(wheel!=0){int size=filtered().size();if(size>0){index=(index+(wheel>0?size-1:1))%size;hostPage=0;}}
    }
}
