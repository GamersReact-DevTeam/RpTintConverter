package us.greact.filters;

import java.awt.*;

public class NetheriteFilter extends ImageFilter {
    @Override
    public Color getPixelColor(int r, int g, int b, int a) {
        // Grayscale
        int average = (r+g+b)/3;
        int extraDarken = 0;

        average = clamp(average - extraDarken, 0, 255);

        return new Color(average,average,average,a);
    }
}
