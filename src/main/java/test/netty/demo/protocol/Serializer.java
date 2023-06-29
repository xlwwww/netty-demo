package test.netty.demo.protocol;

import com.google.gson.*;
import test.netty.demo.message.Message;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public interface Serializer {
    <T> byte[] serialize(T object) throws IOException;

    <T> T deserialize(Class<T> clazz, byte[] bytes);

    enum Algorithm implements Serializer {
        JAVA {
            @Override
            public <T> byte[] serialize(T object) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = null;
                try {
                    outputStream = new ObjectOutputStream(byteArrayOutputStream);
                    outputStream.writeObject(object);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return byteArrayOutputStream.toByteArray();
            }

            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    // 字节数组转换为Message对象
                    return (T) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }
        },

        JSON {
            @Override
            public <T> byte[] serialize(T object) throws IOException {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Class.class, new ClassCodec())
                        .create();
                String s = gson.toJson(object);
                return s.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Class.class, new ClassCodec())
                        .create();
                String s = new String(bytes,StandardCharsets.UTF_8);
                return gson.fromJson(s, clazz);
            }
        };
    }

    class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                return Class.forName(jsonElement.getAsString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(aClass.getName());
        }
    }
}
