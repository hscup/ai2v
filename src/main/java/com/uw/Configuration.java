package com.uw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Configuration {
	private String ffmpegPath;
	private String audioFile;
	private Map<String, Integer> images;

	public Configuration() {

	}

	public Configuration(String ffmpegPath, String audioFile, Map<String, Integer> images) {
		this.ffmpegPath = ffmpegPath;
		this.audioFile = audioFile;
		this.images = images;
	}

	public String getFfmpegPath() {
		return ffmpegPath;
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	public String getAudioFile() {
		return audioFile;
	}

	public void setAudioFile(String audioFile) {
		this.audioFile = audioFile;
	}

	public Map<String, Integer> getImages() {
		return images;
	}

	public void setImages(Map<String, Integer> images) {
		this.images = images;
	}

	public static Configuration parseConfig(String inputDir) {
		JSONParser parser = new JSONParser();
		String configFile = Constants.CONFIG_FILE;
		if (!inputDir.trim().equals(".")) {
			Path configPath = Paths.get(inputDir, Constants.CONFIG_FILE);
			if (!(new File(configPath.toString()).exists())) {
				return null;
			}
			configFile = configPath.toString();
		}

		try {

			Object obj = parser.parse(new FileReader((new File(configFile)).getAbsolutePath()));
			JSONObject jsonObject = (JSONObject) obj;
			String ffmpegPath = (String) jsonObject.get("ffmpegPath");
			String audio = (String) jsonObject.get("audio");
			JSONObject imageObj = (JSONObject) jsonObject.get("images");
			Map<String, Integer> images = new LinkedHashMap<>();
			for (Object key : imageObj.keySet()) {
				key = (String) key;
				Integer value = ((Long) imageObj.get(key)).intValue();
				images.put((String) key, (Integer) value);
			}

			images = Utils.sortByValue(images);

			return new Configuration(ffmpegPath, audio, images);
		} catch (ParseException e) {
			System.out.println("Please make sure the config file is formated correctly");
			e.printStackTrace();
			return null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
