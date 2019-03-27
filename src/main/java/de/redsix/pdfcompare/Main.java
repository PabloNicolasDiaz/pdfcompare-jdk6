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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.redsix.pdfcompare.cli.CliArguments;
import de.redsix.pdfcompare.cli.CliArgumentsImpl;
import de.redsix.pdfcompare.cli.CliArgumentsParseException;
import de.redsix.pdfcompare.cli.CliComparator;
import de.redsix.pdfcompare.ui.Display;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		try {
			final CliArguments cliArguments = new CliArgumentsImpl(args);

			if (cliArguments.areAvailable()) {
				System.exit(startCLI(cliArguments));
			} else if (cliArguments.isHelp()) {
				cliArguments.printHelp();
			} else {
				startUI();
			}
		} catch (CliArgumentsParseException exception) {
			LOG.error(exception.getMessage());
		}
	}

	private static void startUI() {
		new Display().init();
	}

	private static int startCLI(CliArguments cliArguments) {
		return new CliComparator(cliArguments).getResult();
	}
}
