package us.greact;

import us.greact.filters.HueShiftFilter;
import us.greact.filters.ImageFilter;
import us.greact.filters.NetheriteFilter;

import java.awt.*;
import java.io.*;
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
                logger.info("Found: " + file.getName());
                // Not setting a predicate defaults to a normal minecraft resource pack with all images modified.
                // Otherwise, every item texture will have a predicate with this value set to 1
                // Could set this to custom_model_data and thatll work too.
                logger.info("Pick a predicate, type null for no predicate: ");
                String predicate = readInput();
                if (!Objects.equals(predicate, "null")){
                    tintedResourcePack.setPredicate(predicate);
                    logger.info("Selected predicate: "+ predicate);
                }
                else {
                    logger.info("Selected no predicate. ");
                }
                pressEnterKeyToContinue();

                tintedResourcePack.read();
                try {
                    logger.info("READ ALL FILES.");

                    logger.info("Pick a filter. ");
                    logger.info("Available filters: ");

                    // Add new options here.
                    logger.info("Hue ");
                    logger.info("Netherite ");

                    String filter = readInput();

                    ImageFilter imageFilter = null;

                    // Add a case for other filters if they are added.
                    switch (filter.toLowerCase()) {
                        case "hue":
                            logger.info("Selected Hue shift, select RGB values. ");
                            logger.info("Ex. (255 255 255)");
                            String rgb = readInput();

                            String[] split = rgb.split(" ");
                            int r = Integer.parseInt(split[0]);
                            int g = Integer.parseInt(split[1]);
                            int b = Integer.parseInt(split[2]);

                            imageFilter = new HueShiftFilter(new Color(r,g,b));
                            break;
                        case "netherite":
                            logger.info("Selected Netherite");
                            imageFilter = new NetheriteFilter();
                            break;
                        default:
                            logger.info("Invalid filter, defaulting to hue 0,0,0");
                            imageFilter = new HueShiftFilter(new Color(0,0,0));
                            break;
                    }
                    pressEnterKeyToContinue();

                    tintedResourcePack.convert(imageFilter);
                }
                catch (Exception e){
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    logger.severe(sw.toString());
                    System.out.println("An Error occurred!");
                    pressEnterKeyToContinue();
                }
            }
        }
        System.out.println("Did all we could find!");
        pressEnterKeyToContinue();
    }

    public static String readInput() throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));

        // Reading data using readLine
        return reader.readLine();
    }

    private static void pressEnterKeyToContinue()
    {
        System.out.println("Press Enter key to continue...");
        Scanner s = new Scanner(System.in);
        s.nextLine();
    }
}
