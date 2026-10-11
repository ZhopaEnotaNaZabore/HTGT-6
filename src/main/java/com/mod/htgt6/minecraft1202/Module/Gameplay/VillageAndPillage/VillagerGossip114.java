package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
/** Five official gossip types, weighted exchange, decay and persistent player UUIDs. */
public final class VillagerGossip114 {
    public static final String KEY="HTGT6Gossip114";
    public static final int[] WEIGHT={-5,-1,1,5,1},MAX={100,200,25,20,25},DECAY={10,20,1,0,2},TRANSFER={10,20,5,20,20};
    public static final class Memory {
        private final Map<UUID,int[]> values=new LinkedHashMap<UUID,int[]>();
        public void add(UUID who,int type,int amount){if(who==null||type<0||type>=5)return;int[] v=values.get(who);if(v==null){v=new int[5];values.put(who,v);}v[type]=(int)Math.max(0,Math.min(MAX[type],(long)v[type]+amount));if(v[type]<2)v[type]=0;prune();}
        public int reputation(UUID who){int[] v=values.get(who);int sum=0;if(v!=null)for(int i=0;i<5;i++)sum+=v[i]*WEIGHT[i];return sum;}
        public int value(UUID who,int type){int[] v=values.get(who);return v==null?0:v[type];}
        public void decay(int days){for(int[] v:values.values())for(int i=0;i<5;i++){v[i]=(int)Math.max(0,v[i]-(long)DECAY[i]*Math.max(0,days));if(v[i]<2)v[i]=0;}prune();}
        private void prune(){Iterator<int[]> it=values.values().iterator();while(it.hasNext()){int sum=0;for(int x:it.next())sum+=x;if(sum==0)it.remove();}}
        public void transferFrom(Memory source,Random random){List<UUID> who=new ArrayList<UUID>();List<Integer> type=new ArrayList<Integer>(),end=new ArrayList<Integer>();int total=0;for(Map.Entry<UUID,int[]> e:source.values.entrySet())for(int t=0;t<5;t++)if(e.getValue()[t]>0){who.add(e.getKey());type.add(t);total+=e.getValue()[t]*Math.abs(WEIGHT[t]);end.add(total);}if(total==0)return;Set<Integer> picked=new HashSet<Integer>();for(int i=0;i<20;i++){int roll=random.nextInt(total),index=0;while(roll>=end.get(index))index++;picked.add(index);}for(int i:picked){UUID id=who.get(i);int t=type.get(i),amount=source.value(id,t)-TRANSFER[t];if(amount>=2&&amount>value(id,t))add(id,t,amount-value(id,t));}}
        public NBTTagList write(){NBTTagList list=new NBTTagList();for(Map.Entry<UUID,int[]> e:values.entrySet()){NBTTagCompound n=new NBTTagCompound();n.setLong("Most",e.getKey().getMostSignificantBits());n.setLong("Least",e.getKey().getLeastSignificantBits());n.setIntArray("Values",e.getValue());list.appendTag(n);}return list;}
        public static Memory read(NBTTagList list){Memory m=new Memory();for(int i=0;i<list.tagCount();i++){NBTTagCompound n=list.getCompoundTagAt(i);UUID id=new UUID(n.getLong("Most"),n.getLong("Least"));int[] a=n.getIntArray("Values");for(int t=0;t<Math.min(5,a.length);t++)m.add(id,t,a[t]);}return m;}
    }
    public static boolean eligible(EntityVillager v){return v.getClass()==EntityVillager.class&&v.getProfession()>=0&&v.getProfession()<5;}
    public static Memory memory(EntityVillager v){return Memory.read(v.getEntityData().getTagList(KEY,10));}
    private static void save(EntityVillager v,Memory m){v.getEntityData().setTag(KEY,m.write());}
    public static int reputation(EntityVillager v,UUID who){return memory(v).reputation(who);}
    public static void add(EntityVillager v,UUID who,int type,int amount){if(!eligible(v)||who==null)return;Memory m=memory(v);m.add(who,type,amount);save(v,m);}
    public static void cured(EntityVillager v,UUID player){if(player==null)return;add(v,player,3,20);add(v,player,2,25);}
    @SubscribeEvent public void hurt(LivingHurtEvent e){if(e.ammount>0&&e.entityLiving instanceof EntityVillager&&!e.entityLiving.worldObj.isRemote&&e.source.getEntity() instanceof EntityPlayer)add((EntityVillager)e.entityLiving,e.source.getEntity().getUniqueID(),1,25);}
    @SubscribeEvent public void killed(LivingDeathEvent e){if(!(e.entityLiving instanceof EntityVillager)||e.entityLiving.worldObj.isRemote||!(e.source.getEntity() instanceof EntityPlayer))return;for(Object o:e.entityLiving.worldObj.getEntitiesWithinAABB(EntityVillager.class,e.entityLiving.boundingBox.expand(16,16,16))){EntityVillager witness=(EntityVillager)o;if(witness!=e.entityLiving&&witness.canEntityBeSeen(e.entityLiving))add(witness,e.source.getEntity().getUniqueID(),0,25);}}
    @SubscribeEvent public void tick(LivingEvent.LivingUpdateEvent e){if(!(e.entityLiving instanceof EntityVillager)||e.entityLiving.worldObj.isRemote||e.entityLiving.ticksExisted%20!=0)return;EntityVillager v=(EntityVillager)e.entityLiving;if(!eligible(v))return;NBTTagCompound data=v.getEntityData();long now=v.worldObj.getTotalWorldTime(),last=data.getLong("HTGT6GossipDecay");
        if(!data.hasKey("HTGT6GossipDecay")||now<last)data.setLong("HTGT6GossipDecay",now);else if(now-last>=24000){int days=(int)Math.min(Integer.MAX_VALUE,(now-last)/24000);Memory m=memory(v);m.decay(days);save(v,m);data.setLong("HTGT6GossipDecay",now);}
        if(v.isChild()||v.isTrading()||now-data.getLong("HTGT6GossipExchange")<1200)return;
        for(Object o:v.worldObj.getEntitiesWithinAABB(EntityVillager.class,v.boundingBox.expand(4,2,4))){EntityVillager peer=(EntityVillager)o;if(peer==v||!eligible(peer)||peer.isChild()||peer.isTrading()||!v.canEntityBeSeen(peer)||now-peer.getEntityData().getLong("HTGT6GossipExchange")<1200)continue;Memory a=memory(v),b=memory(peer);a.transferFrom(b,v.getRNG());b.transferFrom(a,peer.getRNG());save(v,a);save(peer,b);data.setLong("HTGT6GossipExchange",now);peer.getEntityData().setLong("HTGT6GossipExchange",now);break;}
    }
}
