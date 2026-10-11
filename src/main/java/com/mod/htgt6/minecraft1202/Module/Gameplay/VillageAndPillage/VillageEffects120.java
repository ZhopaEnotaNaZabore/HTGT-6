package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.asm.HTGT6Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Normal Potion lifecycle supplies persistence, packets, milk and expiry. */
public final class VillageEffects120 {
    private static Potion omen, hero;
    private static final String LEGACY_OMEN="HTGT6VillagePillageBadOmen", LEGACY_HERO="HTGT6VillagePillageHeroUntil";
    private VillageEffects120() {}
    public static synchronized void init(){
        if(omen!=null&&hero!=null)return;
        int a=HTGT6Config.BAD_OMEN_POTION_ID,b=HTGT6Config.HERO_OF_VILLAGE_POTION_ID;
        if(a==b)throw new IllegalStateException("Village potion IDs must be unique");
        validate(a,"BadOmen");validate(b,"HeroOfTheVillage");
        omen=new VillagePotion(a,745784,"bad_omen");hero=new VillagePotion(b,4521796,"hero_of_the_village");
    }
    private static void validate(int id,String name){
        if(id<0||id>=Potion.potionTypes.length)throw new IllegalStateException(name+" ID outside potion registry: "+id);
        if(Potion.potionTypes[id]!=null)throw new IllegalStateException(name+" ID "+id+" occupied by "+Potion.potionTypes[id].getName()+"; configure matching village_potion_ids on client/server");
    }
    public static int badOmenLevel(EntityPlayer player){
        PotionEffect e=omen==null?null:player.getActivePotionEffect(omen);return e==null?0:Math.min(5,e.getAmplifier()+1);
    }
    /** -1 means no active Hero; otherwise vanilla zero-based amplifier, for trading. */
    public static int heroAmplifier(EntityPlayer player){
        PotionEffect e=hero==null?null:player.getActivePotionEffect(hero);return e==null?-1:e.getAmplifier();
    }
    public static void captainKilled(EntityPlayer player){
        if(player.worldObj.isRemote||omen==null)return;migrateLegacy(player);
        int level=Math.min(5,badOmenLevel(player)+1);
        player.addPotionEffect(new PotionEffect(omen.id,120000,level-1));
    }
    /** Called only after the server has accepted a new raid. */
    public static void consumeBadOmen(EntityPlayer player){if(!player.worldObj.isRemote&&omen!=null)player.removePotionEffect(omen.id);}
    public static void rewardHero(EntityPlayer player,int omenLevel){
        if(!player.worldObj.isRemote&&hero!=null)player.addPotionEffect(new PotionEffect(hero.id,48000,Math.max(0,Math.min(4,omenLevel-1))));
    }
    public static void migrateLegacy(EntityPlayer player){
        if(player.worldObj.isRemote||omen==null||hero==null)return;
        NBTTagCompound n=player.getEntityData();
        if(n.hasKey(LEGACY_OMEN)){
            int level=Math.min(5,n.getInteger(LEGACY_OMEN));n.removeTag(LEGACY_OMEN);
            if(level>0&&!player.isPotionActive(omen))player.addPotionEffect(new PotionEffect(omen.id,120000,level-1));
        }
        if(n.hasKey(LEGACY_HERO)){
            long until=n.getLong(LEGACY_HERO),now=player.worldObj.getTotalWorldTime();n.removeTag(LEGACY_HERO);
            if(until>now&&!player.isPotionActive(hero))player.addPotionEffect(new PotionEffect(hero.id,(int)Math.min(48000L,until-now),0));
        }
    }
    private static final class VillagePotion extends Potion {
        private final String texture;
        VillagePotion(int id,int color,String name){super(id,false,color);texture=name;setPotionName("potion."+name);}
        @Override public boolean isReady(int duration,int amplifier){return false;}
        @Override @SideOnly(Side.CLIENT)
        public void renderInventoryEffect(int x,int y,PotionEffect effect,net.minecraft.client.Minecraft mc){
            try(com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112 ignored=com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112.capture()){
                org.lwjgl.opengl.GL11.glColor4f(1,1,1,1);
                mc.getTextureManager().bindTexture(new net.minecraft.util.ResourceLocation("htgt6","textures/gui/effects/"+texture+".png"));
                net.minecraft.client.gui.Gui.func_146110_a(x+6,y+7,0,0,18,18,18,18);
            }
        }
    }
}
