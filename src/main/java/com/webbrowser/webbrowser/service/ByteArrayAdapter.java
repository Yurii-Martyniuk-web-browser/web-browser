package com.webbrowser.webbrowser.service;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Base64;

/**
 * Адаптер для Gson, який автоматично конвертує byte[] <-> Base64 String.
 * Це вирішує проблему "Expected BEGIN_ARRAY but was STRING".
 */
public class ByteArrayAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

    @Override
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            // Якщо сервер надсилає рядок Base64 (стандартна поведінка JAX-RS/Jackson)
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                return Base64.getDecoder().decode(json.getAsString());
            }

            // Якщо сервер раптом надіслав справжній масив JSON [1, 2, 3] (рідкісний випадок)
            if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                byte[] bytes = new byte[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    bytes[i] = array.get(i).getAsByte();
                }
                return bytes;
            }

            return new byte[0]; // Порожній масив, якщо формат невідомий

        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Failed to decode Base64 string to byte[]", e);
        }
    }

    @Override
    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        // При відправці на сервер завжди кодуємо в Base64 рядок
        return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
    }
}