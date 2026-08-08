package de.ole101.mctrafficcontrol.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.awt.Color;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Pattern;

public class ModUtils {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, _, _) -> Instant.parse(json.getAsString()))
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, _, _) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, _, _) -> LocalDateTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, _, _) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, _, _) -> LocalTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (src, _, _) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Pattern.class, (JsonDeserializer<Pattern>) (json, _, _) -> Pattern.compile(json.getAsString()))
            .registerTypeAdapter(Pattern.class, (JsonSerializer<Pattern>) (src, _, _) -> new JsonPrimitive(src.pattern()))
            .registerTypeAdapter(Color.class, (JsonDeserializer<Color>) (json, _, _) -> new Color(json.getAsInt()))
            .registerTypeAdapter(Color.class, (JsonSerializer<Color>) (src, _, _) -> new JsonPrimitive(src.getRGB()))
            .create();
}
