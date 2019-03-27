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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CliArgumentsImpl implements CliArguments {

	private static final int EXPECTED_FILENAME_INDEX = 0;
	private static final int ACTUAL_FILENAME_INDEX = 1;

	private static final String OUTPUT_OPTION = "o";
	private static final String OUTPUT_LONG_OPTION = "output";
	private static final String HELP_OPTION = "h";
	private static final String HELP_LONG_OPTION = "help";

	private final Options options;
	private CommandLine commandLine;

	public CliArgumentsImpl(String[] args) {
		options = new Options();
		options.addOption(buildOutputOption());
		options.addOption(buildHelpOption());

		process(args);
	}

	@Override
	public boolean areAvailable() {
		return commandLine.getArgList().size() == 2 && getExpectedFile() != null && getActualFile() != null;
	}

	@Override
	public boolean isHelp() {
		return commandLine.hasOption(HELP_OPTION);
	}

	@Override
	public String getExpectedFile() {
		return getRemainingArgument(EXPECTED_FILENAME_INDEX);
	}

	@Override
	public String getActualFile() {
		return getRemainingArgument(ACTUAL_FILENAME_INDEX);
	}

	@Override
	public String getOutputFile() {
		if (!commandLine.hasOption(OUTPUT_OPTION)) {
			return null;
		}

		return commandLine.getOptionValue(OUTPUT_OPTION);
	}

	@Override
	public void printHelp() {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("java -jar pdfcompare-x.x.x.jar [EXPECTED] [ACTUAL]", options);
	}

	private Option buildOutputOption() {
		return Option.builder(OUTPUT_OPTION).argName("output").desc("Provide an optional output file for the result")
				.hasArg(true).longOpt(OUTPUT_LONG_OPTION).numberOfArgs(1).required(false).type(String.class)
				.valueSeparator('=').build();
	}

	private Option buildHelpOption() {
		return Option.builder(HELP_OPTION).argName("help").desc("Displays this text and exit").hasArg(false)
				.longOpt(HELP_LONG_OPTION).numberOfArgs(0).required(false).build();
	}

	private void process(String[] args) {
		try {
			CommandLineParser commandLineParser = new DefaultParser();
			commandLine = commandLineParser.parse(options, args);
		} catch (ParseException exception) {
			throw new CliArgumentsParseException(exception);
		}
	}

	private String getRemainingArgument(int index) {
		if (commandLine.getArgList().isEmpty() || commandLine.getArgList().size() < index + 1) {
			return null;
		}

		return commandLine.getArgList().get(index);
	}
}
