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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.val;

import org.junit.jupiter.api.Test;

import de.redsix.pdfcompare.cli.CliArgumentsImpl;

public class CliArgumentsTest {

	@Test
	public void cliIsAvailableWhenExpectedAndActualFilenameAreProvided() {
		val cliArguments = new CliArgumentsImpl(new String[] { "expected.pdf", "actual.pdf" });
		assertThat(cliArguments.areAvailable(), is(true));
	}

	@Test
	public void cliIsNotAvailableWhenOnlyOneFilenameIsProvided() {
		val cliArguments = new CliArgumentsImpl(new String[] { "expected.pdf" });
		assertThat(cliArguments.areAvailable(), is(false));
	}

	@Test
	public void cliIsNotAvailableWhenMoreArgumentsAreProvidedThanExpected() {
		val cliArguments = new CliArgumentsImpl(new String[] { "a.pdf", "b.pdf", "c.pdf" });
		assertThat(cliArguments.areAvailable(), is(false));
	}

	@Test
	public void cliIsNotAvailableWhenNoArgumentsAreProvided() {
		val cliArguments = new CliArgumentsImpl(new String[] {});
		assertThat(cliArguments.areAvailable(), is(false));
	}

	@Test
	public void provideExpectedAndActualFilename() {
		val cliArguments = new CliArgumentsImpl(new String[] { "expected.pdf", "actual.pdf" });
		assertNotNull(cliArguments.getExpectedFile());
		assertThat(cliArguments.getExpectedFile(), equalTo("expected.pdf"));
		assertNotNull(cliArguments.getActualFile());
		assertThat(cliArguments.getActualFile(), equalTo("actual.pdf"));
	}

	@Test
	public void provideOutputFilenameWithShortArgument() {
		val cliArguments = new CliArgumentsImpl(new String[] { "-o", "result.pdf" });
		assertNotNull(cliArguments.getOutputFile());
		assertThat(cliArguments.getOutputFile(), equalTo("result.pdf"));
	}

	@Test
	public void provideOutputFilenameWithLongArgument() {
		val cliArguments = new CliArgumentsImpl(new String[] { "--output", "result.pdf" });
		assertNotNull(cliArguments.getOutputFile());
		assertThat(cliArguments.getOutputFile(), equalTo("result.pdf"));
	}

}
