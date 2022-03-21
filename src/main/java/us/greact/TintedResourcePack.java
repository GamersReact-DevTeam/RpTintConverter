package us.greact;


import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.io.FileUtils;
import us.greact.filters.ImageFilter;
import us.greact.model.JavaModel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TintedResourcePack {

    private final File original;
    private File tintedDirectory;
    private final List<File> allImportantFiles = new ArrayList<>();
    private static HashMap<String, String> manualAdditions = new HashMap<>();

    static {
        manualAdditions.put("compass", "_00");
        manualAdditions.put("clock", "_00");
    }

    private String predicate = null;

    public TintedResourcePack(File directory) {
        this.original = directory;
        this.tintedDirectory = new File(Main.resultPacks.getPath()+"/"+original.getName()+"_tinted");
        tintedDirectory = checkIfExists(1, tintedDirectory, tintedDirectory.getPath());
    }

    public File checkIfExists(int currentLoop, File directory, String originalPath){
        if (directory.exists()){
            File newCheck = new File(originalPath + " ("+currentLoop+")");
            currentLoop++;
            if (newCheck.exists()){
                return checkIfExists(currentLoop, newCheck, originalPath);
            }
            else {
                return newCheck;
            }
        }
        else {
            return directory;
        }
    }

    public void setPredicate(String predicate){
        this.predicate = predicate;
    }

    public void read(){
        Main.logger.info("Reading through all folders.");
        recursiveImageGrab(original);
    }

    private void recursiveImageGrab(File folder){
        Main.logger.info("Reading through "+folder.getName()+".");
        // Loops through all files in a folder. We only care about the PNGs and Jsons at this stage.
        allImportantFiles.addAll(getAllImportantFiles(folder));

        // Everything it didn't get is a directory, we need to get those after we got all the files that are actually useful to
        // Maximize performance.
        for (File file : Objects.requireNonNull(folder.listFiles())){
            // Is it a directory? Run this method again.
            if (file.isDirectory()){
                recursiveImageGrab(file);
            }
        }
    }

    public void convert(ImageFilter filter) throws IOException {
        for (File file : allImportantFiles){
            if (file.getName().endsWith(".png")){
                Main.logger.info("Modifying "+file.getName()+".");

                BufferedImage bufferedImage = ImageIO.read(file);

                String declaration = null;

                if (this.predicate != null){
                    declaration = "_tinted.png";
                }
                else {
                    declaration = ".png";
                }

                File destination = new File(getMirrorPath(file.getPath()).replace(".png", declaration));

                if (!destination.exists()){
                    destination.mkdirs();
                }

                filter.apply(bufferedImage);

                ImageIO.write(bufferedImage, "png", destination);
                Main.logger.info("Wrote "+file.getName()+".");
            }
            else if (file.getName().endsWith(".json")) {
                Main.logger.info("Modifying " + file.getName() + ".");
                // No predicate = We dont want to have the textures set based on condition.
                if (this.predicate == null) {
                    continue;
                }
                if (file.getPath().contains("models\\item")) {
                    Main.logger.info(file.getName() + " matches conditions for a json item model.");

                    File tintModelFile = new File(file.getPath().replace(".json", "_tinted.json"));

                    Main.logger.info("Made mirror: " + tintModelFile.getName());
                    JavaModel javaModel = new JavaModel(file);

                    if (!javaModel.hasParent()){
                        Main.logger.info("Has no parent. Ignored.");
                        // Some items just dont have actual models... Dont ask me why...
                        continue;
                    }

                    Main.logger.info("Made internal java model object for " + file.getName() + ".");

                    if (!javaModel.hasOverrides()) {

                        JsonObject object = new JsonObject();
                        JsonObject predicate = new JsonObject();

                        predicate.add(this.predicate, new JsonPrimitive(1));
                        object.add("model", new JsonPrimitive("item/" + tintModelFile.getName().replace(".json", "")));
                        object.add("predicate", predicate);

                        JavaModel.Override override = new JavaModel.Override(object);

                        Main.logger.info("Made custom override for " + file.getName() + ".");

                        List<JavaModel.Override> overrides = new ArrayList<>();
                        overrides.add(override);

                        javaModel.setOverrides(overrides);
                    } else {
                        List<JavaModel.Override> overrides = javaModel.getOverrides();

                        List<JavaModel.Override> newOverrides = new ArrayList<>();



                        JsonObject baseObject = new JsonObject();
                        JsonObject basePredicate = new JsonObject();

                        // Need some kind of predicate object inside the model already because I am a bad developer.
                        basePredicate.add(this.predicate, new JsonPrimitive(1));
                        baseObject.add("model", new JsonPrimitive("item/" + tintModelFile.getName().replace(".json", "")));
                        baseObject.add("predicate", basePredicate);

                        JavaModel.Override baseOverride = new JavaModel.Override(baseObject);

                        newOverrides.add(baseOverride);

                        newOverrides.addAll(overrides);

                        for (JavaModel.Override override : overrides) {

                            JsonObject object = new JsonObject();
                            JsonObject predicate = new JsonObject();

                            // Need some kind of predicate object inside the model already because I am a bad developer.
                            predicate.add(this.predicate, new JsonPrimitive(1));
                            object.add("model", new JsonPrimitive("item/" + tintModelFile.getName().replace(".json", "")));
                            object.add("predicate", predicate);

                            JavaModel.Override newOverride = new JavaModel.Override(object);

                            JsonObject oldPredicate = override.getPredicate().deepCopy();
                            oldPredicate.add(this.predicate, new JsonPrimitive(1));

                            newOverride.setPredicate(oldPredicate);

                            // model: "cool_model + _tinted"
                            newOverride.setModel(override.getModelName() + "_tinted");

                            newOverrides.add(newOverride);
                        }


                        javaModel.setOverrides(newOverrides);
                    }

                    String intermediary = "";

                    if (getIntermediary(file, ".json") != null){
                        intermediary = getIntermediary(file, ".json");
                        Main.logger.info("Has intermediary! Probably a clock or a compass.. 00's man..."+" "+file.getName() +" "+intermediary);
                    }

                    JavaModel tintModel = new JavaModel(javaModel.getParent(), "item/"+file.getName().replace(".json", intermediary+"_tinted"));

                    File tintModelMirror = new File(getMirrorPath(tintModelFile.getPath()));

                    makeDirectoryProper(tintModelMirror);

                    tintModel.writeToFile(tintModelMirror);

                    Main.logger.info("Made tinted model mirror for "+file.getName());

                    File mirrorMainModel = new File(getMirrorPath(file.getPath()));

                    makeDirectoryProper(mirrorMainModel);

                    javaModel.writeToFile(mirrorMainModel);

                    Main.logger.info("Made main model mirror for "+file.getName());

                    Main.logger.info("Completed all json tasks for " + file.getName() + ".");
                }
            }
            else if (file.getName().endsWith(".mcmeta")){
                FileUtils.copyFile(file, new File(getMirrorPath(file.getPath())));
            }
        }
    }

    private String getIntermediary(File file, String removal){
        return manualAdditions.get(file.getName().replace(removal, ""));
    }

    private void makeDirectoryProper(File file) {
        File nonImportantDirectory = new File(file.getPath().replace(file.getName(), ""));
        nonImportantDirectory.mkdirs();
    }

    private String getMirrorPath(String string){
        String[] split = string.split(original.getName());

        String after = split[1];

        return tintedDirectory.getPath()+after;
    }

    private List<File> getAllImportantFiles(File folder){
        List<File> files = new ArrayList<>();
        for (File file : Objects.requireNonNull(folder.listFiles())){
            if (!file.isDirectory()){
                if (file.getName().endsWith(".png") || file.getName().endsWith(".json") || file.getName().endsWith(".mcmeta")) {
                    files.add(file);
                    Main.logger.info("Found "+file.getName()+" added it to read files.");
                }
            }
        }
        return files;
    }
}
