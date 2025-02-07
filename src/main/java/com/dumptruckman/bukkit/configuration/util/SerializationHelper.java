package com.dumptruckman.bukkit.configuration.util;

import com.dumptruckman.bukkit.configuration.SerializableSet;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.serializer.SerializerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jeremy Wood
 * @version 6/14/2017
 */
public class SerializationHelper {

    private static final Logger LOG = Logger.getLogger(SerializationHelper.class.getName());

    public static Object serialize(@NotNull Object value) {
        if (value instanceof Object[]) {
            value = new ArrayList<>(Arrays.asList((Object[]) value));
        }
        if (value instanceof Set && !(value instanceof SerializableSet)) {
            value = new SerializableSet((Set) value);
        }
        if (value instanceof ConfigurationSection) {
            return buildMap(((ConfigurationSection) value).getValues(false));
        } else if (value instanceof Map) {
            return buildMap((Map) value);
        } else if (value instanceof List) {
            return buildList((List) value);
        } else if (value instanceof ConfigurationSerializable) {
            ConfigurationSerializable serializable = (ConfigurationSerializable) value;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
            values.putAll(serializable.serialize());
            return buildMap(values);
        } else {
            return value;
        }
    }

    /**
     * Takes a Map and parses through the values, to ensure that, before saving, all objects are as appropriate as
     * possible for storage in most data formats.
     * <p>
     * Specifically it does the following:
     *   for Map: calls this method recursively on the Map before putting it in the returned Map.
     *   for List: calls {@link #buildList(java.util.Collection)} which functions similar to this method.
     *   for ConfigurationSection: gets the values as a map and calls this method recursively on the Map before putting
     *       it in the returned Map.
     *   for ConfigurationSerializable: add the {@link ConfigurationSerialization#SERIALIZED_TYPE_KEY} to a new Map
     *       along with the Map given by {@link org.bukkit.configuration.serialization.ConfigurationSerializable#serialize()}
     *       and calls this method recursively on the new Map before putting it in the returned Map.
     *   for Everything else: stores it as is in the returned Map.
     *
     * @return The serialised map
     */
    @NotNull
    private static Map<String, Object> buildMap(@NotNull final Map<?, ?> map) {
        final Map<String, Object> result = new LinkedHashMap<String, Object>(map.size());
        try {
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(entry.getKey().toString(), serialize(entry.getValue()));
            }
        } catch (final Exception e) {
            LOG.log(Level.WARNING, "Error while building configuration map.", e);
        }
        return result;
    }

    /**
     * Takes a Collection and parses through the values, to ensure that, before saving, all objects are as appropriate
     * as possible for storage in most data formats.
     * <p>
     * Specifically it does the following:
     *   for Map: calls {@link #buildMap(java.util.Map)} on the Map before adding to the returned list.
     *   for List: calls this method recursively on the List.
     *   for ConfigurationSection: gets the values as a map and calls {@link #buildMap(java.util.Map)} on the Map
     *       before adding to the returned list.
     *   for ConfigurationSerializable: add the {@link ConfigurationSerialization#SERIALIZED_TYPE_KEY} to a new Map
     *       along with the Map given by {@link org.bukkit.configuration.serialization.ConfigurationSerializable#serialize()}
     *       and calls {@link #buildMap(java.util.Map)} on the new Map before adding to the returned list.
     *   for Everything else: stores it as is in the returned List.
     *
     * @return The serialised list
     */
    private static List<Object> buildList(@NotNull final Collection<?> collection) {
        final List<Object> result = new ArrayList<Object>(collection.size());
        try {
            for (Object o : collection) {
                result.add(serialize(o));
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error while building configuration list.", e);
        }
        return result;
    }

    /**
     * Parses through the input map to deal with serialized objects a la {@link ConfigurationSerializable}.
     * <p>
     * Called recursively first on Maps and Lists before passing the parsed input over to
     * {@link ConfigurationSerialization#deserializeObject(java.util.Map)}.  Basically this means it will deserialize
     * the most nested objects FIRST and the top level object LAST.
     *
     * @return The deserialized object
     */
    public static Object deserialize(@NotNull final Map<?, ?> input, boolean continueOnSerializationError) {
        final Map<String, Object> output = new LinkedHashMap<>(input.size());
        for (final Map.Entry<?, ?> e : input.entrySet()) {
            if (e.getValue() instanceof Map) {
                output.put(e.getKey().toString(), deserialize((Map<?, ?>) e.getValue(), continueOnSerializationError));
            }  else if (e.getValue() instanceof List) {
                output.put(e.getKey().toString(), deserialize((List<?>) e.getValue(), continueOnSerializationError));
            } else {
                output.put(e.getKey().toString(), e.getValue());
            }
        }
        if (output.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
            try {
                return ConfigurationSerialization.deserializeObject(output);
            } catch (IllegalArgumentException ex) {
                LOG.severe("Could not deserialize the following object:");
                LOG.severe(String.valueOf(output));
                if (!continueOnSerializationError) {
                    throw new SerializerException(ex.getMessage());
                }
                return null;
            }
        }
        return output;
    }

    /**
     * Parses through the input list to deal with serialized objects a la {@link ConfigurationSerializable}.
     * <p>
     * Functions similarly to {@link #deserialize(java.util.Map, boolean)} but only for detecting lists within
     * lists and maps within lists.
     *
     * @return The deserialized object
     */
    private static Object deserialize(@NotNull final List<?> input, boolean continueOnSerializationError) {
        final List<Object> output = new ArrayList<Object>(input.size());
        for (final Object o : input) {
            if (o instanceof Map) {
                output.add(deserialize((Map<?, ?>) o, continueOnSerializationError));
            } else if (o instanceof List) {
                output.add(deserialize((List<?>) o, continueOnSerializationError));
            } else {
                output.add(o);
            }
        }
        return output;
    }
}
