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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import de.redsix.pdfcompare.cli.CliArgumentsImpl;
import de.redsix.pdfcompare.cli.CliComparator;

public class CliComparatorTest {

	@Test
	public void comparesTwoEqualFilesAndReturnsZero() {
		CliComparator testCliComparator = new CliComparator(
				new CliArgumentsImpl(new String[] { "actual.pdf", "actual.pdf" }));

		assertThat(testCliComparator.getResult(), equalTo(0));
	}

	@Test
	public void comparesTwoDifferentFilesAndReturnsOne() {
		CliComparator testCliComparator = new CliComparator(
				new CliArgumentsImpl(new String[] { "expected.pdf", "actual.pdf" }));

		assertThat(testCliComparator.getResult(), equalTo(1));
	}

}
