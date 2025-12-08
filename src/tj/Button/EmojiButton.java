package tj.Button;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import utils.ImageLoader;

public class EmojiButton extends ColorButton implements Button {
    protected int screenX;
    protected int screenY;
    protected double sca;
    protected BufferedImage img;

    public EmojiButton(int marginX, int marginY, Anchor anchor) {
        super(marginX, marginY, anchor);
        img = ImageLoader.loadImage("assets/EmojiButton.png");
        sca = 0.25;
    }
}
