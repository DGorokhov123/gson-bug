import com.google.gson.*;

import java.lang.reflect.Type;

public class Main {
    public static void main(String[] args) {
        String goodJson = "{\"name\":\"ivan\"}";
        String badJson = "{\"nickname\":\"ivan\"}";
        Gson goodGson = new GsonBuilder().registerTypeAdapter(User.class, new UserGoodDeserializer()).create();
        Gson badGson = new GsonBuilder().registerTypeAdapter(User.class, new UserBadDeserializer()).create();

        System.out.println("===== Десериализируем корректный JSON: " + goodJson);
        User user1 = goodGson.fromJson(goodJson, User.class);
        System.out.println("Хороший deserializer: " + user1);

        User user2 = goodGson.fromJson(goodJson, User.class);
        System.out.println("Плохой deserializer:  " + user2);

        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("в чем собственно проблема: сигнатура метода fromJson выглядит так:");
        System.out.println(" -- > public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {");
        System.out.println("то есть предполагается, что ловить надо JsonSyntaxException, " +
                "но при добавлении десериалайзера там в интерфейсе кидается другой exception:");
        System.out.println(" -- > public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {");
        System.out.println("и этот exception является родительским по отношению к JsonSyntaxException и летит дальше");
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("===== Десериализируем некорректный JSON: " + badJson);
        try {
            goodGson.fromJson(badJson, User.class);
        } catch (JsonSyntaxException e) {
            System.out.println("Хороший deserializer поймал JsonSyntaxException");
        }

        try {
            try {
                badGson.fromJson(badJson, User.class);
            } catch (JsonSyntaxException e) {
                System.out.println("Плохой deserializer поймал JsonSyntaxException");
            }
        } catch (RuntimeException e) {
            System.out.println("Плохой deserializer не поймал JsonSyntaxException, а " + e.getClass().getSimpleName() + " полетел дальше");
        }

    }
}

class User {
    String name;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "User{name='" + name + "'}";
    }
}

class UserGoodDeserializer implements JsonDeserializer<User> {

    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (!jsonObject.has("name")) throw new JsonSyntaxException("1json should contain the name field!");
        String name = jsonObject.get("name").getAsString();
        return new User(name);
    }
}

class UserBadDeserializer implements JsonDeserializer<User> {

    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (!jsonObject.has("name")) throw new JsonParseException("2json should contain the name field!");
        String name = jsonObject.get("name").getAsString();
        return new User(name);
    }
}
