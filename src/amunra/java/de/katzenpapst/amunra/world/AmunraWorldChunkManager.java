package de.katzenpapst.amunra.world;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import net.minecraft.world.biome.BiomeGenBase;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class AmunraWorldChunkManager extends WorldChunkManagerSpace {
	private final BiomeGenBase biome;

	public AmunraWorldChunkManager() {
		this(BiomeGenSpaceUltimate.spaceUltimate);
	}

	protected AmunraWorldChunkManager(BiomeGenBase biome) {
		this.biome = biome;
	}

	@Override
	public BiomeGenBase getBiome() {
		return biome;
	}

	public static final class Horus extends AmunraWorldChunkManager {
		public Horus() { super(BiomeGenSpaceUltimate.raHorus); }
	}

	public static final class Neper extends AmunraWorldChunkManager {
		public Neper() { super(BiomeGenSpaceUltimate.raNeper); }
	}

	public static final class Maahes extends AmunraWorldChunkManager {
		public Maahes() { super(BiomeGenSpaceUltimate.raMaahes); }
	}

	public static final class Seth extends AmunraWorldChunkManager {
		public Seth() { super(BiomeGenSpaceUltimate.raSeth); }
	}

	public static final class Anubis extends AmunraWorldChunkManager {
		public Anubis() { super(BiomeGenSpaceUltimate.raAnubis); }
	}

}
