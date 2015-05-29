package io.github.guerra24.voxel.client.launcher.init;

import io.github.guerra24.voxel.client.kernel.util.Logger;
import io.github.guerra24.voxel.client.launcher.ConstantsLauncher;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Init {
	public static void InitLog() {
		try {
			System.setOut(new PrintStream(new FileOutputStream(
					ConstantsLauncher.logpath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void checkJava() {
		String javaVersion = System.getProperty("java.version");
		if (!javaVersion.startsWith("1.8"))
			throw new RuntimeException("JRE 1.8.0 "
					+ "is required to run the launcher.");
		Logger.log("JRE " + javaVersion + " found");
	}
	
	public static void printSystemInfo() {
		Logger.log("System Info");
		Logger.log(System.getProperty("os.name"));
		Logger.log("OS Version " + System.getProperty("os.version"));
		Logger.log("OS Arch " + System.getProperty("os.arch"));
	}
}