I found a bug in GSON library. It's actual fo version 2.12.1

Bug description:

Method fromJson signature looks like that:
```
public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
```

so, it's obvious to coders to catch JsonSyntaxException:
```
try {
    goodGson.fromJson(badJson, User.class);
} catch (JsonSyntaxException e) {
    System.out.println("Good deserializer caught JsonSyntaxException");
}
```
Such construction works until we add Deserializer:
```
Gson badGson = new GsonBuilder()
          .registerTypeAdapter(User.class, new UserBadDeserializer())
          .create();

class UserBadDeserializer implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, 
                         JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (!jsonObject.has("name")) 
                     throw new JsonParseException("json should contain the name field!");
        String name = jsonObject.get("name").getAsString();
        return new User(name);
    }
}
```
The signature of deserialize method we should override, throws another 
exception - JsonParseException, which is parent to JsonSyntaxException 
and isn't caught by try-catch construction above
It flies further and may cause unexpected behaviour. 

```
        try {
            try {
                badGson.fromJson(badJson, User.class);
            } catch (JsonSyntaxException e) {
                System.out.println("Good deserializer caught JsonSyntaxException");
            }
        } catch (RuntimeException e) {
            System.out.println("Bad deserializer didn't catch JsonSyntaxException, and " 
                        + e.getClass().getSimpleName() + " flew further");
        }
```
Solution 1 : Change signature of deserialize method to throw JsonSyntaxException.

Solution 2 : Change signature of fromJson method (and other methods calling
deserialize) to throw JsonParseException.

(С) Dmitriy Gorokhov dgorokhov123@gmail.com, tg:@DGorokhov

