package com.dumptruckman.bukkit.configuration.json;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SerializableAs("MyCustomObject")
public class CustomSerializable implements ConfigurationSerializable {

    private final ItemStack item;
    private final float rating;

    public CustomSerializable(ItemStack item, float rating) {
        this.item = item;
        this.rating = rating;
    }

    public static CustomSerializable deserialize(@NotNull final Map<String, Object> map) {
        return new CustomSerializable((ItemStack) map.get("item"), (Float) map.get("rating"));
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("rating", rating);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CustomSerializable that)) return false;
        return Float.compare(rating, that.rating) == 0 && Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, rating);
    }
}
