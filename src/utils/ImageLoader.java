package utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageLoader {

	public static BufferedImage loadImage(String imgFile) {
		BufferedImage img = null;
		try {
			File f = new File(imgFile);
			if (f.exists()) {
				img = ImageIO.read(f);
			}
		} catch (IOException e) {
			System.out.println("Image Files: Error - " + e.getMessage());
		}
		return img;
	}
	//Saves a BufferedImage to a file.
	public static boolean saveImage(BufferedImage img, String fileName, String fileFormat) {
		try {

			File saveFile = new File(fileName + "." + fileFormat);
			ImageIO.write(img, fileFormat, saveFile);

		} catch (IOException e) {
			return false;
		}
		return true;
	}
}


