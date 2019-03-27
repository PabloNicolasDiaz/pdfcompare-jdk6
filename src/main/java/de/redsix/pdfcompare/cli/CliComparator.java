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
package de.redsix.pdfcompare.cli;

import java.io.IOException;

import lombok.val;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.redsix.pdfcompare.CompareResult;
import de.redsix.pdfcompare.CompareResultWithExpectedAndActual;
import de.redsix.pdfcompare.PdfComparator;

public class CliComparator {

	private static final int EQUAL_DOCUMENTS_RESULT_VALUE = 0;
	private static final int UNEQUAL_DOCUMENTS_RESULT_VALUE = 1;
	private static final int ERROR_RESULT_VALUE = 2;

	private int result;
	private CompareResult compareResult;
	private static final Logger LOG = LoggerFactory.getLogger(CliComparator.class);

	public CliComparator(CliArguments cliArguments) {
		val expectedFile = cliArguments.getExpectedFile();
		val actualFile = cliArguments.getActualFile();
		if (expectedFile != null && actualFile != null) {
			result = compare(expectedFile, actualFile);
			val outputFile = cliArguments.getOutputFile();
			if (outputFile != null) {
				compareResult.writeTo(outputFile);
			}
		}
	}

	public int getResult() {
		return result;
	}

	private int compare(String expectedFile, String actualFile) {
		try {
			compareResult = new PdfComparator<CompareResultWithExpectedAndActual>(expectedFile, actualFile,
					new CompareResultWithExpectedAndActual()).compare();
			return (compareResult.isEqual()) ? EQUAL_DOCUMENTS_RESULT_VALUE : UNEQUAL_DOCUMENTS_RESULT_VALUE;
		} catch (IOException ex) {
			LOG.error(ex.getMessage());

			return ERROR_RESULT_VALUE;
		}
	}

}
