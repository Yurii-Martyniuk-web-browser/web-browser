package com.webbrowser.webbrowser.service;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Base64;

public class ByteArrayAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

    @Override
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                return Base64.getDecoder().decode(json.getAsString());
            }

            if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                byte[] bytes = new byte[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    bytes[i] = array.get(i).getAsByte();
                }
                return bytes;
            }

            return new byte[0];

        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Failed to decode Base64 string to byte[]", e);
        }
    }

    @Override
    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
    }
}