package us.greact.filters;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class ImageFilter {

    public BufferedImage apply(BufferedImage image){

        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                int p = image.getRGB(x,y);

                // What??? Bithacking for rgb values?? Thanks Java
                int a = (p>>24)&0xff;
                int r = (p>>16)&0xff;
                int g = (p>>8)&0xff;
                int b = p&0xff;

                Color color = getPixelColor(r, g, b, a);

                image.setRGB(x,y, color.getRGB());
            }
        }

        return image;
    }

    protected int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public abstract Color getPixelColor(int r, int g, int b, int a);
}
