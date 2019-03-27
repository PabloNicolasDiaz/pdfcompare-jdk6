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
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PageDiffCalculatorTest {

	@Test
	public void booleanConstructor1() {
		final PageDiffCalculator diffCalculator = new PageDiffCalculator(true, true);
		assertTrue(diffCalculator.differencesFound());
		assertTrue(diffCalculator.differencesFoundInExclusion());
	}

	@Test
	public void booleanConstructor2() {
		final PageDiffCalculator diffCalculator = new PageDiffCalculator(false, false);
		assertFalse(diffCalculator.differencesFound());
		assertFalse(diffCalculator.differencesFoundInExclusion());
	}

	@Test
	public void noDiffFound() {
		final PageDiffCalculator diffCalculator = new PageDiffCalculator(1000, 0);
		assertFalse(diffCalculator.differencesFound());
		assertFalse(diffCalculator.differencesFoundInExclusion());
	}

	@Test
	public void diffFoundInExclusionIsAlwaysCounted() {
		final PageDiffCalculator diffCalculator = new PageDiffCalculator(1000, 1);
		diffCalculator.diffFoundInExclusion();
		assertFalse(diffCalculator.differencesFound());
		assertTrue(diffCalculator.differencesFoundInExclusion());
	}

	@Test
	public void diffFoundWithZeroPercentAllowedIsCounted() {
		final PageDiffCalculator diffCalculator = new PageDiffCalculator(1000, 0);
		diffCalculator.diffFound();
		assertTrue(diffCalculator.differencesFound());
	}

	@Test
	public void diffBelowPercentageIsNotReportedAsDifferenceFound() {
		final PageDiffCalculator diffCalculator = new PageDiffCalculator(200, 1);
		diffCalculator.diffFound();
		diffCalculator.diffFound();
		assertFalse(diffCalculator.differencesFound());
	}

	@Test
	public void diffAbovePercentageIsReportedAsDifferenceFound() {
		final PageDiffCalculator diffCalculator = new PageDiffCalculator(200, 1);
		diffCalculator.diffFound();
		diffCalculator.diffFound();
		diffCalculator.diffFound();
		assertTrue(diffCalculator.differencesFound());
	}

	@Test
	public void diffBelowPercentageAsFractionIsNotSupported() {
		final PageDiffCalculator diffCalculator = new PageDiffCalculator(1000, 0.2);
		diffCalculator.diffFound();
		diffCalculator.diffFound();
		assertFalse(diffCalculator.differencesFound());
	}

	@Test
	public void diffAbovePercentageAsFractionIsReported() {
		final PageDiffCalculator diffCalculator = new PageDiffCalculator(1000, 0.2);
		diffCalculator.diffFound();
		diffCalculator.diffFound();
		diffCalculator.diffFound();
		assertTrue(diffCalculator.differencesFound());
	}
}