package io.github.guerra24.voxel.client.launcher.properties;

import io.github.guerra24.voxel.client.kernel.util.Logger;
import io.github.guerra24.voxel.client.launcher.ConstantsLauncher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class Writter {
	public static void configWritter() {

		Properties prop = new Properties();
		OutputStream output = null;

		File f = new File(ConstantsLauncher.optionsPath);
		if (!f.exists()) {

			try {
				Logger.log("Creating config file");
				output = new FileOutputStream(ConstantsLauncher.optionsPath);
				prop.setProperty("IconPath", ConstantsLauncher.iconPath1);
				prop.setProperty("BackgroundPath", ConstantsLauncher.iconPath2);
				prop.store(output, null);

			} catch (IOException io) {
				io.printStackTrace();
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}
}