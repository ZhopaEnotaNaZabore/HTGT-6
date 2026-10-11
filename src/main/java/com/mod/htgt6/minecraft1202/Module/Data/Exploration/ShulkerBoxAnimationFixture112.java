package com.mod.htgt6.minecraft1202.Module.Data.Exploration;

import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.TileEntityShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ModelCombatShulker;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderShulkerBox112;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

/** Actual container callbacks, tile events, joining-client state and model transforms. */
public final class ShulkerBoxAnimationFixture112 {
    public static void main(String[] args) throws Exception {
        TileEntityShulkerBox112 server = new TileEntityShulkerBox112();
        TileEntityShulkerBox112 client = new TileEntityShulkerBox112();
        server.openInventory(); server.openInventory(); server.closeInventory();
        S35PacketUpdateTileEntity packet=(S35PacketUpdateTileEntity)server.getDescriptionPacket();
        require(packet.func_148857_g().getInteger("OpenCount")==1,"second viewer keeps box open");
        require(!packet.func_148857_g().hasKey("Items"),"animation packet does not expose inventory");
        client.onDataPacket(null,packet);
        client.updateEntity();
        close(client.getProgress(0),0,"previous frame");
        close(client.getProgress(.5F),.05F,"interpolated opening frame");
        for(int i=1;i<10;i++)client.updateEntity();
        close(client.getProgress(1),1,"ten tick opening");
        client.receiveClientEvent(1,0); client.updateEntity();
        close(client.getProgress(.5F),.95F,"interpolated closing frame");
        client.receiveClientEvent(1,1); client.updateEntity();
        close(client.getProgress(1),1,"reopening during close");
        client.receiveClientEvent(1,0);
        for(int i=0;i<12;i++)client.updateEntity();
        close(client.getProgress(1),0,"closing clamps at zero");
        server.closeInventory(); server.closeInventory();
        require(((S35PacketUpdateTileEntity)server.getDescriptionPacket()).func_148857_g().getInteger("OpenCount")==0,"duplicate close cannot make negative viewers");
        ModelCombatShulker model=new ModelCombatShulker();
        RenderShulkerBox112.applyPose(model,0);close(model.lid.rotationPointY,24,"closed lid");
        RenderShulkerBox112.applyPose(model,.5F);close(model.lid.rotationPointY,20,"half-open lid");
        close(model.lid.rotateAngleY,(float)Math.PI*.75F,"half turn");
        RenderShulkerBox112.applyPose(model,1);close(model.lid.rotationPointY,16,"open lid");
        close(model.lid.rotateAngleY,(float)Math.PI*1.5F,"270 degree turn");
        require(new BlockShulkerBox112("white").getRenderType()==-1,"static cube disabled");
        try(ZipFile reference=new ZipFile(args[1])) {
            for(String color:new String[]{"white","orange","magenta","light_blue","yellow","lime","pink","gray","silver","cyan","purple","blue","brown","green","red","black"}) {
                String path="assets/minecraft/textures/entity/shulker/shulker_"+(color.equals("silver")?"light_gray":color)+".png";
                byte[] original=new byte[(int)reference.getEntry(path).getSize()];
                try(java.io.DataInputStream input=new java.io.DataInputStream(reference.getInputStream(reference.getEntry(path)))) { input.readFully(original); }
                require(java.util.Arrays.equals(original,Files.readAllBytes(Paths.get(args[0],"assets/htgt6/textures/entity/exploration_update/shulker_box/"+color+".png"))),"official texture "+color);
            }
        }
        System.out.println("ShulkerBoxAnimationFixture112: OK; tile events, viewers, interpolation, 16 official textures");
    }
    private static void close(float value,float expected,String message) { require(Math.abs(value-expected)<.0001F,message); }
    private static void require(boolean condition,String message) { if(!condition)throw new AssertionError(message); }
}
