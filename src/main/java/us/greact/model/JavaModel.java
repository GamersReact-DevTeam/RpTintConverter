package us.greact.model;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import us.greact.Main;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JavaModel {

    private final JsonObject object;
    private final Gson gson;
    List<Override> overrides = new ArrayList<>();

    public JavaModel(File file) throws IOException {
        gson = new GsonBuilder().setPrettyPrinting().create();
        Main.logger.info("Attempting to create java model for: "+file.getName());

        JsonParser parser = new JsonParser();

        Main.logger.info("Created json parser.");

        JsonReader reader = new JsonReader(new FileReader(file));

        Main.logger.info("Created file reader.");

        this.object = parser.parse(reader).getAsJsonObject();

        Main.logger.info("Parsed json.");
        parseOverrides();
        Main.logger.info("Parsed overrides and found: "+overrides.size());
    }

    public JavaModel(String parent, String texture){
        gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("parent", new JsonPrimitive(parent));
        JsonObject textures = new JsonObject();
        textures.add("layer0", new JsonPrimitive(texture));
        jsonObject.add("textures", textures);

        this.object = jsonObject;
    }

    private void parseOverrides(){
        overrides.clear();
        if (object.has("overrides")){
            JsonArray array = (JsonArray) object.get("overrides");
            for (Object override : array) {
                JsonObject jsonObject = (JsonObject) override;
                overrides.add(new Override(jsonObject));
            }
        }
    }

    public List<Override> getOverrides() {
        return overrides;
    }

    public void writeToFile(File file) throws IOException {
        FileWriter writer = new FileWriter(file.getPath());
        writer.write(gson.toJson(object));
        writer.close();
    }

    public void setOverrides(List<Override> overrides){
        this.overrides.clear();
        JsonArray overrideArray = new JsonArray();
        for (Override override : overrides){
            overrideArray.add(override.override);
        }
        object.add("overrides", overrideArray);

        parseOverrides();
    }

    public boolean hasParent(){
        // Fatherless behavior
        return object.has("parent");
    }

    public String getParent(){
        return object.get("parent").getAsString();
    }

    public boolean hasOverrides(){
        return !overrides.isEmpty();
    }

    public record Override(JsonObject override) {

        public JsonObject getPredicate() {
            return override.getAsJsonObject("predicate");
        }

        public String getModelName() {
            return override.get("model").getAsString();
        }

        public void setPredicate(JsonObject object){
            override.add("predicate", object);
        }

        public void setModel(String model){
            override.add("model", new JsonPrimitive(model));
        }
    }
}


