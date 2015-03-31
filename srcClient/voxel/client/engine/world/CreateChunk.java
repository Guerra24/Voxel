package voxel.client.engine.world;

import org.lwjgl.util.vector.Vector3f;

import voxel.client.StartClient;
import voxel.client.engine.entities.Entity;

public class CreateChunk {

	public static final int CHUNK_SIZE = 16;
	public static final int CHUNK_HEIGHT = 32;

	public static void createChunks() {
		for (int x = 0; x < CHUNK_SIZE; x++) {
			for (int y = 0; y < CHUNK_HEIGHT; y++) {
				for (int z = 0; z < CHUNK_SIZE; z++) {
					StartClient.allCubes.add(new Entity(StartClient.cubeGrass,
							new Vector3f(x, 16, z), 0f, 0f, 0f, 1f));
					if (y < 16) {
						if (StartClient.rand.nextInt(2) == 0) {
							if (StartClient.rand.nextBoolean()) {
								StartClient.allCubes.add(new Entity(
										StartClient.cubeStone, new Vector3f(x,
												y, z), 0f, 0f, 0f, 1f));
							}
							if (StartClient.rand.nextInt(2) == 0) {
								StartClient.allCubes.add(new Entity(
										StartClient.cubeSand, new Vector3f(x,
												y, z), 0f, 0f, 0f, 1f));
							}
						}
					}
				}
			}
		}
	}
}
