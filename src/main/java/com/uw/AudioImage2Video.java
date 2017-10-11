package com.uw;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class AudioImage2Video {
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();

		Options optionNotRequired = new Options();
		Option help = new Option("h", "help", false, "Show help");
		help.setRequired(false);
		optionNotRequired.addOption(help);

		Options options = new Options();

		Option input = new Option("i", "input", true, "input directory path");
		input.setRequired(true);
		options.addOption(input);

		Option output = new Option("o", "output", true, "output video file");
		output.setRequired(true);
		options.addOption(output);

		options.addOption(help);

		// this parses the command line but doesn't throw an exception on unknown
		// options
		CommandLine cl = null;
		try {
			cl = new DefaultParser().parse(optionNotRequired, args, true);
		} catch (ParseException e1) {
			e1.printStackTrace();
			formatter.printHelp("<command>", options);
			System.exit(1);
			return;
		}

		if (!(cl.getOptions().length == 0)) {
			if (cl.hasOption("h")) {
				formatter.printHelp("<command>", options);
				return;
			}
		} else {
			CommandLine cmd;

			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				System.out.println(e.getMessage());
				formatter.printHelp("<command>", options);

				System.exit(1);
				return;
			}
			String inputDir = cmd.getOptionValue("input");
			String outputFilePath = cmd.getOptionValue("output", Constants.DEFAUL_OUTPUT_VIDEO_NAME);

			File file = new File(inputDir);
			if (!file.isDirectory()) {
				System.out.println("Please ensure the input directory is exist");
				System.exit(0);
			}
			
			Configuration config = Configuration.parseConfig(inputDir);
			Utils.createVideo(config,inputDir, outputFilePath);
		}

	}
}
