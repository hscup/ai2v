package com.uw;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

public class Utils {
	public static String createVideo(Configuration config, String inputDir, String outputPath) {
		if (config == null) {
			System.out.println("Please make sure you have 'config.json' file and format it correctly");
			return null;
		}

		if (config.getImages().size() == 0) {
			System.out.println("Please set image in configuration file");
			return null;
		}

		FFmpeg ffmpeg = null;
		if (config.getFfmpegPath() != null && !config.getFfmpegPath().isEmpty()) {
			try {
				ffmpeg = new FFmpeg(config.getFfmpegPath());
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else if (FFmpeg.DEFAULT_PATH == null || FFmpeg.DEFAULT_PATH.isEmpty()) {
			System.out.println("Please set ffmpeg path in config file or add ffmpeg to PATH");
			;
			return null;
		} else {
			try {
				ffmpeg = new FFmpeg();
			} catch (IOException e) {
				// ignored
				return null;
			}
		}

		try {
			FFmpegBuilder builder = new FFmpegBuilder();
			String tmp = "temp.mp4";

			String imageListFile = createListConcat(config.getImages(), inputDir);
			if (imageListFile != null && !imageListFile.isEmpty()) {
				builder.addInput(new File(imageListFile).getAbsolutePath());
			}

			builder.overrideOutputFiles(true) // Override the output if it exists
					.addExtraArgs("-safe", "0", "-f", "concat").addOutput(tmp) // Filename for the destination
					.setFormat("mp4") // Format is inferred from filename, or can be set

					.disableSubtitle() // No subtiles
					.setVideoCodec("libx264") // Video using x264
					.setVideoFrameRate(24, 1) // at 24 frames per second
					.addExtraArgs("-pix_fmt", "yuv420p")
//					.setVideoResolution(1280, 720) // at 1280x720 resolution

					.setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
					.done();

			FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);

			// Run a one-pass encode
			executor.createJob(builder).run();
			
			if (config.getAudioFile() != null && !config.getAudioFile().isEmpty()) {
				builder = new FFmpegBuilder();
				builder.setInput(tmp);
				Path audioPath = Paths.get(inputDir, config.getAudioFile());
				builder.addInput("\"" + (new File(audioPath.toString())).getAbsolutePath().trim() + "\"")
				.overrideOutputFiles(true)
				.addOutput(outputPath)
				.addExtraArgs("-c:v", "copy", "-c:a", "aac")
//				.addExtraArgs("-shortest")
				.done();

				executor = new FFmpegExecutor(ffmpeg);

				// Run a one-pass encode
				executor.createJob(builder).run();
			}
			
			return outputPath;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private static String createListConcat(Map<String, Integer> images, String saveDir) {
		String outputFile = null;
		Writer writer = null;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(Paths.get(saveDir, Constants.IMAGE_LIST_FILE).toString()), "utf-8"));

			Iterator<Entry<String, Integer>> entries = images.entrySet().iterator();

			double preInpoint = 0.0;
			while (entries.hasNext()) {
				// File file = new File(entry.getKey());
				Entry<String, Integer> entry = entries.next();
				String imagePath = Paths.get(saveDir, entry.getKey()).toString();
				double currentInpoint = (double) entry.getValue();

				if (currentInpoint == preInpoint) {
					writer.write("file '" + (new File(imagePath)).getAbsolutePath() + "'");
					writer.write(System.lineSeparator());
					continue;
				} else {
					double duration = currentInpoint - preInpoint;;
					writer.write("duration " + duration);
					writer.write(System.lineSeparator());
					writer.write("file '" + (new File(imagePath)).getAbsolutePath() + "'");
					writer.write(System.lineSeparator());
					preInpoint = currentInpoint;
				}
				
				// Last element
				// Due to a quirk, the last image has to be specified twice - the 2nd time without any duration directive
				if (!entries.hasNext()) {
					double duration = Constants.DEFAULT_LAST_DURATION / 2;
					// duration = (currentInpoint - preInpoint) / 2;
					writer.write("duration " + duration);
					writer.write(System.lineSeparator());
					writer.write("file '" + (new File(imagePath)).getAbsolutePath() + "'");
					writer.write(System.lineSeparator());
				} 
			}
			outputFile = Paths.get(saveDir, Constants.IMAGE_LIST_FILE).toString();
		} catch (IOException ex) {
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}

		return outputFile;
	}

	public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		LinkedHashMap<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
