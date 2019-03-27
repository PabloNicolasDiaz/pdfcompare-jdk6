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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;

import lombok.val;

import org.junit.jupiter.api.Test;

import com.typesafe.config.ConfigException;

public class ExclusionsTest {

	private final Exclusions exclusions = new Exclusions();

	@Test
	public void readExclusions() {
		exclusions.readExclusions("ignore.conf");
		assertThat(exclusions.forPage(1).contains(300, 400), is(true));
		assertThat(exclusions.forPage(1).contains(600, 400), is(false));

		assertThat(exclusions.forPage(2).contains(1800, 250), is(true));
		assertThat(exclusions.forPage(2).contains(600, 400), is(false));

		assertThat(exclusions.forPage(3).contains(600, 400), is(false));
	}

	@Test
	public void readFromFile() {
		exclusions.readExclusions(new File("ignore.conf"));
		assertThat(exclusions.forPage(1).contains(300, 400), is(true));
	}

	@Test
	public void readFromInputStreamWithoutPage() {
		exclusions.readExclusions(
				new ByteArrayInputStream("exclusions: [{x1: 230, y1: 350, x2: 450, y2: 420}]".getBytes()));
		assertThat(exclusions.forPage(1).contains(300, 400), is(true));
		assertThat(exclusions.forPage(8).contains(300, 400), is(true));
	}

	@Test
	public void readFromInputStreamPageOnly() {
		exclusions.readExclusions(new ByteArrayInputStream("exclusions: [{page: 3}]".getBytes()));
		assertThat(exclusions.forPage(1).contains(300, 400), is(false));
		assertThat(exclusions.forPage(3).contains(300, 400), is(true));
	}

	@Test
	public void missingCoordinateIsRejected() {
		assertThrows(ConfigException.class, () -> exclusions.readExclusions(
				new ByteArrayInputStream("exclusions: [{page: 3, x1: 230, y1: 350, x2: 450, y3: 420}]".getBytes())));
	}

	@Test
	public void coordinateBelowZeroAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> exclusions.readExclusions(
				new ByteArrayInputStream("exclusions: [{page: 3, x1: 230, y1: 350, x2: 450, y2: -1}]".getBytes())));
	}

	@Test
	public void pageBelowOneAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> exclusions.readExclusions(
				new ByteArrayInputStream("exclusions: [{page: 0, x1: 230, y1: 350, x2: 450, y2: 600}]".getBytes())));
	}

	@Test
	public void wrongCoordinateOrderIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> exclusions.readExclusions(
				new ByteArrayInputStream("exclusions: [{page: 3, x1: 230, y1: 350, x2: 150, y2: 600}]".getBytes())));
	}

	@Test
	public void coordinatesInCmAndMm() {
		exclusions.readExclusions(new ByteArrayInputStream(
				"exclusions: [{page: 3, x1: 21mm, y1: 134mm, x2: 2.4cm, y2: 14cm}]".getBytes()));
		assertThat(exclusions.forPage(3).contains(247, 1583), is(false));
		assertThat(exclusions.forPage(3).contains(248, 1582), is(false));
		assertThat(exclusions.forPage(3).contains(248, 1583), is(true));
		assertThat(exclusions.forPage(3).contains(283, 1654), is(true));
		assertThat(exclusions.forPage(3).contains(284, 1654), is(false));
		assertThat(exclusions.forPage(3).contains(283, 1655), is(false));
	}

	@Test
	public void coordinatesInPt() {
		exclusions.readExclusions(
				new ByteArrayInputStream("exclusions: [{page: 3, x1: 21pt, y1: 1, x2: 30pt, y2: 10}]".getBytes()));
		val ex = exclusions.forPage(3);
		assertThat(ex.contains(87, 1), is(false));
		assertThat(ex.contains(88, 1), is(true));
		assertThat(ex.contains(125, 1), is(true));
		assertThat(ex.contains(126, 1), is(false));
	}
}