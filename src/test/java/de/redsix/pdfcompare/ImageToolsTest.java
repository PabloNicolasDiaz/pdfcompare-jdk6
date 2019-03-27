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

import static de.redsix.pdfcompare.ImageTools.EXCLUDED_BACKGROUND_RGB;
import static de.redsix.pdfcompare.ImageTools.fadeElement;
import static de.redsix.pdfcompare.ImageTools.fadeExclusion;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.*;

import org.junit.jupiter.api.Test;

public class ImageToolsTest {

	@Test
	public void fadeElementMakesDarkerPixelsLighter() {
		int actual = fadeElement(new Color(0, 0, 0).getRGB());
		assertThat(actual, is(new Color(153, 153, 153).getRGB()));
		actual = fadeElement(new Color(255, 255, 255).getRGB());
		assertThat(actual, is(new Color(255, 255, 255).getRGB()));
		actual = fadeElement(new Color(0, 0, 255).getRGB());
		assertThat(actual, is(new Color(153, 153, 255).getRGB()));
		actual = fadeElement(new Color(180, 60, 130).getRGB());
		assertThat(actual, is(new Color(225, 177, 205).getRGB()));
	}

	@Test
	public void fadeExclusionOfDarkPixlesIsARegularFade() {
		int actual = fadeExclusion(new Color(0, 0, 0).getRGB());
		assertThat(actual, is(new Color(153, 153, 153).getRGB()));
	}

	@Test
	public void fadeExclusionOfLightPixlesMakesItYellow() {
		int actual = fadeExclusion(new Color(250, 250, 250).getRGB());
		assertThat(actual, is(EXCLUDED_BACKGROUND_RGB));
	}
}