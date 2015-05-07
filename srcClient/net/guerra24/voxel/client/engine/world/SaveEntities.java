package net.guerra24.voxel.client.engine.world;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.guerra24.voxel.client.engine.Engine;
import net.guerra24.voxel.client.engine.entities.Entity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class SaveEntities {
	public static void saveGame(String path) {
		Gson gson = new Gson();
		String json = gson.toJson(Engine.allObjects);

		FileWriter writer;
		try {
			writer = new FileWriter(path);
			writer.write(json);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadGame(String path) {
		Gson gson = new Gson();

		try {
			BufferedReader world = new BufferedReader(new FileReader(path));
			JsonParser parser = new JsonParser();
			JsonArray jArray = parser.parse(world).getAsJsonArray();
			List<Entity> lcs = new ArrayList<Entity>();

			for (JsonElement obj : jArray) {
				Entity cse = gson.fromJson(obj, Entity.class);
				lcs.add(cse);
			}
			Engine.allEntities.removeAll(Engine.allObjects);
			Engine.allObjects = lcs;	
			Engine.allEntities.addAll(Engine.allObjects);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}