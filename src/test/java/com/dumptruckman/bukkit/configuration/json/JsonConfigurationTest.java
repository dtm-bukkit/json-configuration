package com.dumptruckman.bukkit.configuration.json;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JsonConfigurationTest {

    private ServerMock server;
    private JsonConfiguration jc;

    @Before
    public void setUp() {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
        ConfigurationSerialization.registerClass(CustomSerializable.class);
        jc = new JsonConfiguration();
    }

    @After
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testDeserializeJson() throws InvalidConfigurationException {
        jc.loadFromString(getResourceAsText("valid.json"));

        assertEquals(1, jc.getInt("test_number"));
        assertEquals("a string", jc.getString("test_string"));
        assertEquals(List.of(1, 2, 3), jc.getIntegerList("test_array"));
        ConfigurationSection testObject = jc.getConfigurationSection("test_object");
        assertNotNull(testObject);
        assertEquals(1, testObject.getInt("a"));
        assertEquals("x", testObject.getString("b"));
        assertEquals(3.3, testObject.getDouble("c"), 0.0000001);

        ConfigurationSection testConfigurationSerialization = jc.getConfigurationSection("test_configuration_serialization");
        assertNotNull(testConfigurationSerialization);
        Location expectedLocation = new Location(server.getWorld("world"), 86.5, 70, -61.5, 135, 0);
        assertEquals(expectedLocation, testConfigurationSerialization.get("location"));
        CustomSerializable expectedCustom = new CustomSerializable(new ItemStack(Material.GRAVEL, 3), 2.34f);
        assertEquals(expectedCustom, testConfigurationSerialization.get("custom"));
    }

    @Test
    public void testSerializeJson() throws ParseException {
        jc.set("test_number", 1);
        jc.set("test_string", "a string");
        jc.set("test_array", List.of(1, 2, 3));
        ConfigurationSection testObject = jc.createSection("test_object");
        testObject.set("a", 1);
        testObject.set("b", "x");
        testObject.set("c", 3.3);
        ConfigurationSection testConfigurationSerialization = jc.createSection("test_configuration_serialization");
        testConfigurationSerialization.set("location", new Location(server.getWorld("world"), 86.5, 70, -61.5, 135, 0));
        testConfigurationSerialization.set("custom", new CustomSerializable(new ItemStack(Material.GRAVEL, 3), 2.34f));

        String actualData = jc.saveToString();
        String expectedData = getResourceAsText("valid.json");

        Object actualJson = new JSONParser(JSONParser.USE_INTEGER_STORAGE).parse(actualData);
        Object expectedJson = new JSONParser(JSONParser.USE_INTEGER_STORAGE).parse(expectedData);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testContinueOnError() throws InvalidConfigurationException {
        jc.options().continueOnSerializationError(true);
        jc.loadFromString(getResourceAsText("error.json"));
        assertEquals(1, jc.getInt("number"));
        assertEquals("this is a test", jc.getString("words"));
        Location expectedLocation = new Location(server.getWorld("world"), 86.5, 70, -61.5, 135, 0);
        assertEquals(expectedLocation, jc.get("location"));
        CustomSerializable expectedCustom = new CustomSerializable(null, 2.34f);
        assertEquals(expectedCustom, jc.get("custom"));
    }

    private String getResourceAsText(String path) {
        try {
            return Files.readString(Paths.get(getClass().getClassLoader().getResource(path).toURI()));
        } catch (IOException | URISyntaxException e) {
            return "";
        }
    }
}
