package com.mod.htgt6.common.block.info;

import com.cleanroommc.modularui.screen.Tooltip;
import com.mod.htgt6.api.hitech6.BaseMachines.BlockMachine;
import com.mod.htgt6.common.block.hitech6.*;
import net.minecraft.block.Block;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Single source for item Shift tooltips and ModularUI machine information HUDs. */
public final class MachineDescriptions {
    private MachineDescriptions() {}

    public static Description get(Block block, int metadata) {
        if (block instanceof BlockUniversalGasTurbine) return description("HIGHTECH6 GENERATORS", "UNIVERSAL GAS TURBINE",
                "Gas-powered generator", "Operating efficiency: 90%", "Base tier: LV",
                "Supports Tier Modules", "Supports Transformer Upgrades");
        if (block instanceof BlockUniversalEngine) return description("HIGHTECH6 GENERATORS", "UNIVERSAL ENGINE",
                "Semi / Fuel / Chemical / Thermal generator", "Operating efficiency: 90%", "Base tier: LV",
                "Supports Tier Modules", "Supports Transformer Upgrades");
        if (block instanceof BlockRadioisotopeGenerator) return description("HIGHTECH6 NUCLEAR TECH", "RADIOISOTOPE GENERATOR",
                "Processes radioactive isotopes and produces decay products.",
                "More active isotopes produce more EU/t but decay faster.",
                "Long-lived isotopes produce less EU/t for a longer time.",
                "Fixed tier: IV");
        if (block instanceof BlockMagicEnergyGenerator) return description("HIGHTECH6 GENERATORS", "MAGIC ENERGY GENERATOR",
                "Converts enchantments, potions and configured magic fuels into EU.",
                "Enchantments: level x 25,000 EU", "Potions: 16,000 / 24,000 / 40,000 EU",
                "Base tier: LV", "Supports Tier Modules", "Supports Transformer Upgrades");
        if (block instanceof BlockAssembler) return description("HIGHTECH6 MACHINERY", "ASSEMBLER",
                "Creates the majority of technological components.", "Base tier: LV",
                "Supports Tier Modules", "Supports Transformer Upgrades");
        if (block instanceof BlockFluidSolidifier) return description("HIGHTECH6 MACHINERY", "FLUID SOLIDIFIER",
                "Multiblock controller: exact 5 x 7 x 16 casting line.",
                "Controller and all inputs are on the front wall; face points outward.",
                "The item-output gateway is fixed at the rear conveyor centre (u=0, y=1).",
                "Glass is used only on the left observation wall; the opposite and rear walls are casing.",
                "Fluid gateways use Selector Tags 1-24 (1 ingot, 2 plate); controller selector has priority.",
                "Base tier: LV",
                "Supports Tier Modules", "Supports Transformer Upgrades");
        if (block instanceof BlockSuperMasicCompressor) return description("HIGHTECH6 MACHINERY", "SUPER-MASSIVE COMPRESSOR",
                "Multiblock controller: exact 5 x 5 x 5 shell.",
                "Controller: centre of the lower front side; hollow 3 x 3 x 3 chamber.",
                "Bottom accepts 1-10 input and output hatches; exactly one energy input.",
                "Runs HTGT6, GT6 Compressor and GT6 Implosion Compressor recipes.",
                "Base tier: LV", "Supports Tier Modules", "Supports Transformer Upgrades");
        if (block instanceof BlockMachine) {
            switch (metadata) {
                case 0: return description("HIGHTECH6 MACHINERY", "LINE ASSEMBLER",
                        "Multi-input assembly line with fluid support.", "Processes registered Line Assembler recipes.");
                case 1: return description("HIGHTECH6 MACHINERY", "ELECTROMAGNETIC PROCESSOR",
                        "Processes materials using electromagnetic recipes.");
                case 2: return description("HIGHTECH6 MACHINERY", "METAL FORMER",
                        "Forms metal components using machine molds.");
                default: return Description.EMPTY;
            }
        }
        return Description.EMPTY;
    }

    private static Description description(String category, String title, String... lines) {
        return new Description(category, title, Arrays.asList(lines));
    }

    public static final class Description {
        private static final Description EMPTY = new Description("", "", Collections.<String>emptyList());
        public final String category;
        public final String title;
        public final List<String> lines;
        private Description(String category, String title, List<String> lines) {
            this.category = category; this.title = title; this.lines = lines;
        }
        public boolean isEmpty() { return this == EMPTY || lines.isEmpty(); }

        @SuppressWarnings("unchecked")
        public void addShiftTooltip(List tooltip) {
            if (isEmpty()) return;
            tooltip.add(EnumChatFormatting.GREEN + category);
            tooltip.add(EnumChatFormatting.AQUA + "====================");
            tooltip.add(EnumChatFormatting.GREEN + title);
            for (String line : lines) tooltip.add(EnumChatFormatting.YELLOW + line);
            tooltip.add(EnumChatFormatting.AQUA + "====================");
        }

        public void addModularHud(Tooltip tooltip) {
            if (isEmpty()) return;
            tooltip.addLine(category);
            tooltip.addLine("============================");
            tooltip.addLine(title);
            for (String line : lines) tooltip.addLine(line);
            tooltip.addLine("============================");
        }
    }
}
