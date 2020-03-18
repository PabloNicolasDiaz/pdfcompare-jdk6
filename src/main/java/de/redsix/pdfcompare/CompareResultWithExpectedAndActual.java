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

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

/**
 * A CompareResult, that also stores the expected and actual Image and also
 * keeps diffImages in memory for later display.
 */
public class CompareResultWithExpectedAndActual extends CompareResultImpl {

	private final Map<Integer, ImageWithDimension> expectedImages = new TreeMap<Integer, ImageWithDimension>();
	private final Map<Integer, ImageWithDimension> actualImages = new TreeMap<Integer, ImageWithDimension>();

    @Override
    public synchronized void addPage(final PageDiffCalculator diffCalculator, final int pageIndex,
            final ImageWithDimension expectedImage, final ImageWithDimension actualImage, final ImageWithDimension diffImage) {
        super.addPage(diffCalculator, pageIndex, expectedImage, actualImage, diffImage);
        expectedImages.put(pageIndex, expectedImage);
        actualImages.put(pageIndex, actualImage);
    }

	@Override
	protected boolean keepImages() {
		return true;
	}

	public synchronized BufferedImage getDiffImage(final int page) {
		return getBufferedImageOrNull(diffImages.get(page));
	}

	public BufferedImage getExpectedImage(final int page) {
		return getBufferedImageOrNull(expectedImages.get(page));
	}

	public BufferedImage getActualImage(final int page) {
		return getBufferedImageOrNull(actualImages.get(page));
	}

	private BufferedImage getBufferedImageOrNull(final ImageWithDimension imageWithDimension) {
		return imageWithDimension == null ? null : imageWithDimension.bufferedImage;
	}
}
