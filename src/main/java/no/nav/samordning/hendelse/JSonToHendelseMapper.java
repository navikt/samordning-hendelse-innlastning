package no.nav.samordning.hendelse;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.LinkedList;

public class JSonToHendelseMapper {
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
        @Override
        public LocalDate deserialize(JsonElement json,
                                     Type type,
                                     JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
            return LocalDate.parse(json.getAsJsonPrimitive().getAsString());
        }
    }).create();

    public static LinkedList<Hendelse> mapFromJsonToHendelse(String jsonString) {

        LinkedList<Hendelse> hendelser;

        hendelser = new LinkedList<Hendelse>(
                gson.fromJson(
                        jsonString, new TypeToken<LinkedList<Hendelse>>() {
                        }.getType()
                ));

        return hendelser;
    }
}
