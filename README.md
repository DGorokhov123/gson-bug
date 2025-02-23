Найден баг в библиотеке GSON. Актуально для версии 2.12.1

Описание бага:

Сигнатура метода fromJson выглядит так:
```
public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
```

то есть предполагается, что ловить надо JsonSyntaxException:
```
try {
    goodGson.fromJson(badJson, User.class);
} catch (JsonSyntaxException e) {
    System.out.println("Хороший deserializer поймал JsonSyntaxException");
}
```
Такая конструкция работает, пока мы не добавим Deserializer:
```
Gson badGson = new GsonBuilder()
          .registerTypeAdapter(User.class, new UserBadDeserializer())
          .create();

class UserBadDeserializer implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (!jsonObject.has("name")) throw new JsonParseException("2json should contain the name field!");
        String name = jsonObject.get("name").getAsString();
        return new User(name);
    }
}
```
в сигнатуре метода deserialize, который требуется переопределить, кидается 
другое исключение - JsonParseException, которое является родительским по
отношению к JsonSyntaxException и не ловится конструкцией try-catch, написаной выше
```пример из кода:
        try {
            try {
                badGson.fromJson(badJson, User.class);
            } catch (JsonSyntaxException e) {
                System.out.println("Плохой deserializer поймал JsonSyntaxException");
            }
        } catch (RuntimeException e) {
            System.out.println("Плохой deserializer не поймал JsonSyntaxException, а " + e.getClass().getSimpleName() + " полетел дальше");
        }
```
Решение1 : Изменение сигнатуры метода deserialize, чтобы метод кидал 
JsonSyntaxException.

Решение2 : Изменение сигнатуры метода fromJson (а также, возможно, других методов,
использующих вызов deserialize), чтобы метод кидал JsonParseException.

(С) Dmitriy Gorokhov dgorokhov123@gmail.com, tg:@DGorokhov
