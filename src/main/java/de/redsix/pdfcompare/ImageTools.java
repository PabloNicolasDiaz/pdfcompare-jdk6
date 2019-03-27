/*
 * Copyright 2016 Malte Finsterwalder
 * Copyright [2018] Pablo Nicolas Diaz Bilotto [https://github.com/PabloNicolasDiaz/]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.redsix.pdfcompare;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageTools {

	public static final int EXCLUDED_BACKGROUND_RGB = new Color(255, 255, 100).getRGB();

	public static BufferedImage blankImage(final BufferedImage image) {
		Graphics2D graphics = image.createGraphics();
		graphics.setPaint(Color.white);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		return image;
	}

	public static int fadeElement(final int i) {
		final Color color = new Color(i);
		return new Color(fade(color.getRed()), fade(color.getGreen()), fade(color.getBlue())).getRGB();
	}

	public static int fadeExclusion(final int i) {
		final Color color = new Color(i);
		if (color.getRed() > 245 && color.getGreen() > 245 && color.getBlue() > 245) {
			return EXCLUDED_BACKGROUND_RGB;
		}
		return fadeElement(i);
	}

	private static int fade(final int i) {
		return i + ((255 - i) * 3 / 5);
	}

	public static BufferedImage deepCopy(BufferedImage image) {
		return new BufferedImage(image.getColorModel(), image.copyData(null),
				image.getColorModel().isAlphaPremultiplied(), null);
	}
}
