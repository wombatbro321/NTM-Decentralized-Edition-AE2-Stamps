// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.hbm.backport.TooltipDisplay;
import org.jspecify.annotations.Nullable;
import com.hbm.backport.compat.ItemCompat;

public class ItemStamp extends ItemCompat {

    public final StampType type;

    public ItemStamp(Properties properties, StampType type) {
        super(properties);
        this.type = type;
    }

    public static StampType typeOf(ItemStack stack) {
    if (stack.isEmpty()) return null;

    // Normal NTM stamps
    if (stack.getItem() instanceof ItemStamp stamp) {
        return stamp.getStampType(stack);
    }

    // AE2 inscriber presses (optional dependency)
    var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
    if (id != null && id.getNamespace().equals("ae2")) {
        return switch (id.getPath()) {
            case "silicon_press" -> StampType.SILICON;
            case "logic_processor_press" -> StampType.LOGIC;
            case "calculation_processor_press" -> StampType.CALCULATION;
            case "engineering_processor_press" -> StampType.ENGINEERING;
            // Extended AE
            case "extendedae:concurrent_processor_press" -> StampType.CONCURRENT;
            // Advanced AE
            case "advanced_ae:quantum_processor_press" -> StampType.QUANTUM;
            default -> null;
        };
    }

    return null;
}

    public StampType getStampType(ItemStack stack) {
        return type;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        if (stack.getMaxDamage() > 0 && stack.getDamageValue() == 0) {
            adder.accept(
                    Component.translatable(
                            "desc.item.stamp.durability",
                            stack.getMaxDamage() + " / " + stack.getMaxDamage()));
        }
    }

    public enum StampType {
        FLAT,
        PLATE,
        WIRE,
        CIRCUIT,
        C357,
        C44,
        C50,
        C9,
        PRINTING1,
        PRINTING2,
        PRINTING3,
        PRINTING4,
        PRINTING5,
        PRINTING6,
        PRINTING7,
        PRINTING8,
        CALCULATION,
        ENGINEERING,
        LOGIC,
        SILICON,
        CONCURRENT,
        QUANTUM
    }

    private static volatile @Nullable Map<StampType, List<Item>> byType;

    public static List<Item> stampsOf(StampType type) {
    Map<StampType, List<Item>> held = byType;
    if (held == null) {
        Map<StampType, List<Item>> map = new EnumMap<>(StampType.class);

        // existing NTM stamps
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ItemStamp stamp) {
                map.computeIfAbsent(stamp.type, t -> new ArrayList<>()).add(item);
            }
        }

        // AE2 presses
        addIfPresent(map, StampType.SILICON, "ae2:silicon_press");
        addIfPresent(map, StampType.LOGIC, "ae2:logic_processor_press");
        addIfPresent(map, StampType.CALCULATION, "ae2:calculation_processor_press");
        addIfPresent(map, StampType.ENGINEERING, "ae2:engineering_processor_press");
        addIfPresent(map, StampType.CONCURRENT, "extendedae:concurrent_processor_press");
        addIfPresent(map, StampType.QUANTUM, "advanced_ae:quantum_processor_press");

        map.replaceAll((t, items) -> List.copyOf(items));
        byType = held = map;
    }
    return held.getOrDefault(type, List.of());
}

private static void addIfPresent(Map<StampType, List<Item>> map, StampType type, String id) {
    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    if (item != null && item != Items.AIR) {
        map.computeIfAbsent(type, t -> new ArrayList<>()).add(item);
    }
}
