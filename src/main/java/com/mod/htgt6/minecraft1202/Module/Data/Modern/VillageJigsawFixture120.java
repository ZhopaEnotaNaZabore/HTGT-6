package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.VillageJigsawRegistry120;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;

/** Loads the official plains/common pools and expands a deterministic depth-6 village. */
public final class VillageJigsawFixture120 {
    private static final long EXPECTED=0xe80cfa6ed81043aaL; // v2 scoped collision spaces and internal-only expansion

    public static long assertContract(){
        VillageJigsawRegistry120 registry=new VillageJigsawRegistry120();
        registry.validateAll();
        if(registry.loadedTemplateCount()!=483)throw new IllegalStateException(
                "Not every retained village NBT was loaded: "+registry.loadedTemplateCount()+"/483");
        if(registry.legacyElementCount()!=592)throw new IllegalStateException("Official legacy_single contract changed: "
                +registry.legacyElementCount());
        if(registry.featureTypes().size()!=13||!registry.unsupportedFeatures().isEmpty())
            throw new IllegalStateException("Official village feature adapters are incomplete: "+registry.featureTypes());
        if(registry.entityCount()==0||!registry.entityTypes().contains("minecraft:villager"))
            throw new IllegalStateException("Official village NBT entity lists were not loaded");
        if(!registry.entityTypes().contains("minecraft:zombie_villager"))
            throw new IllegalStateException("Official zombie-village residents were not loaded");
        for(String entity:registry.entityTypes())if(!FuturaAncientCityGenerator120.supportsTemplateEntity(entity))
            throw new IllegalStateException("Official village entity has no runtime adapter: "+entity);
        long hash=0xcbf29ce484222325L;
        String[] styles={"plains","desert","savanna","snowy","taiga"};
        for(int style=0;style<styles.length;style++){
            JigsawPlacementRuntime120.Result result=JigsawPlacementRuntime120.generate(registry,
                    new JigsawPlacementRuntime120.Terrain(){public int surfaceY(int x,int z){return 127;}},
                    VillageJigsawRegistry120.startPool(styles[style]),0x1202f00dL+style,0,127,0,6,80);
            if(result.pieces.size()<2)throw new IllegalStateException("Official "+styles[style]
                    +" village fixture produced only "+result.pieces.size()+" piece(s)");
            int houses=0,terminators=0;StringBuilder sample=new StringBuilder();
            for(JigsawPlacementRuntime120.Piece piece:result.pieces){
                if(sample.length()<800){if(sample.length()>0)sample.append(',');sample.append(piece.template.id);}
                if(piece.template.id.contains("/houses/"))houses++;
                if(piece.template.id.contains("/terminators/"))terminators++;
                hash=fnv(hash,piece.template.id.hashCode());hash=fnv(hash,piece.x);hash=fnv(hash,piece.y);
                hash=fnv(hash,piece.z);hash=fnv(hash,piece.rotation);hash=fnv(hash,piece.depth);
                hash=fnv(hash,piece.element.processors.hashCode());
            }
            if(houses<4)throw new IllegalStateException("Official "+styles[style]
                    +" village fixture is fragmented: "+houses+" houses, "+result.pieces.size()
                    +" pieces; attempts="+result.attemptedByPool+", accepted="+result.acceptedByPool
                    +", rejected="+result.rejectedByPool+", first="+result.firstRejectionByPool+"; "+sample);
            hash=fnv(hash,result.rejectedByBoundsOrCollision);
        }
        hash=fnv(hash,registry.loadedPoolCount());hash=fnv(hash,registry.loadedTemplateCount());
        hash=fnv(hash,registry.featureTypes().hashCode());
        hash=fnv(hash,registry.entityCount());hash=fnv(hash,registry.entityTypes().hashCode());
        if(EXPECTED!=0L&&hash!=EXPECTED)throw new IllegalStateException("Official village jigsaw fixture changed: expected "
                +Long.toHexString(EXPECTED)+", got "+Long.toHexString(hash));
        return hash;
    }

    private static long fnv(long hash,int value){return(hash^value)*0x100000001b3L;}
    public static void main(String[] ignored){System.out.println(Long.toHexString(assertContract()));}
    private VillageJigsawFixture120(){ }
}
