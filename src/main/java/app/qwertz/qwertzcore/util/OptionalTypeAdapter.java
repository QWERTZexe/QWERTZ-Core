package app.qwertz.qwertzcore.util;

import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

// Custom TypeAdapter for java.util.Optional
public class OptionalTypeAdapter implements JsonSerializer<Optional<?>>, JsonDeserializer<Optional<?>> {
    @Override
    public JsonElement serialize(Optional<?> src, Type typeOfSrc, JsonSerializationContext context) {
        if (src.isPresent()) {
            return context.serialize(src.get());
        } else {
            return JsonNull.INSTANCE;
        }
    }

    @Override
    public Optional<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json.isJsonNull()) {
            return Optional.empty();
        } else {
            Type actualType =
                    ((ParameterizedType) typeOfT).getActualTypeArguments()[0]; // Get the type inside Optional
            return Optional.of(context.deserialize(json, actualType));
        }
    }
}