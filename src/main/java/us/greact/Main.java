package us.greact;

import us.greact.filters.HueShiftFilter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {

    public static File runDirectory = null;
    public static File convertPacks = null;
    public static File resultPacks = null;

    public static Logger logger;


    public static void main(String[] args) throws IOException {

        logger = Logger.getLogger(Main.class.getName());

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        FileHandler fileHandler = new FileHandler("status.log");

        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);

        logger.addHandler(fileHandler);

        runDirectory = new File(System.getProperty("user.dir"));
        convertPacks = new File(runDirectory.getPath()+"/convert");
        resultPacks = new File(runDirectory.getPath()+"/result");

        // Make directories.
        if (!convertPacks.exists()){
            convertPacks.mkdirs();
        }
        if (!resultPacks.exists()){
            resultPacks.mkdirs();
        }

        for (File file : Objects.requireNonNull(convertPacks.listFiles())){
            if (file.isDirectory()){
                TintedResourcePack tintedResourcePack = new TintedResourcePack(file);
                // Not setting a predicate defaults to a normal minecraft resource pack with all images modified.
                // Otherwise, every item texture will have a predicate with this value set to 1
                // Could set this to custom_model_data and thatll work too.
                tintedResourcePack.setPredicate("diamond");

                tintedResourcePack.read();
                try {
                    // Netherite, hue shift
                    tintedResourcePack.convert(new HueShiftFilter(new Color(173,216,230)));
                }
                catch (Exception e){
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    logger.severe(sw.toString());
                }
            }
        }
        System.out.println("Did all we could find!");
        pressEnterKeyToContinue();
    }

    private static void pressEnterKeyToContinue()
    {
        System.out.println("Press Enter key to exit...");
        Scanner s = new Scanner(System.in);
        s.nextLine();
    }
}
