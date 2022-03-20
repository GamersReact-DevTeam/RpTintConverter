package us.greact.filters;

import java.awt.*;

public class HueShiftFilter extends ImageFilter {

    private final Color desiredColor;

    public HueShiftFilter(Color desiredColor) {
        this.desiredColor = desiredColor;
    }

    @Override
    public Color getPixelColor(int r, int g, int b, int a) {
        int subtraction = 255-(r+g+b)/3;

        return new Color(clamp(desiredColor.getRed()-subtraction, 0, 255), clamp(desiredColor.getGreen()-subtraction, 0, 255), clamp(desiredColor.getBlue()-subtraction, 0, 255), a);
    }
}
