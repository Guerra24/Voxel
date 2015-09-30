package io.github.guerra24.voxel.client.kernel.world;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import com.google.gson.JsonSyntaxException;

import io.github.guerra24.voxel.client.kernel.api.VAPI;
import io.github.guerra24.voxel.client.kernel.core.KernelConstants;
import io.github.guerra24.voxel.client.kernel.core.WorldThread;
import io.github.guerra24.voxel.client.kernel.resources.GameControllers;
import io.github.guerra24.voxel.client.kernel.util.Logger;
import io.github.guerra24.voxel.client.kernel.util.vector.Vector2f;
import io.github.guerra24.voxel.client.kernel.util.vector.Vector3f;
import io.github.guerra24.voxel.client.kernel.world.chunks.Chunk;
import io.github.guerra24.voxel.client.kernel.world.chunks.ChunkKey;

public class DimensionalWorld {
	private int chunkDim;
	private int worldID;
	private HashMap<ChunkKey, Chunk> chunks;
	private Random seed;
	private SimplexNoise noise;
	private String name;
	private int xPlayChunk;
	private int zPlayChunk;
	private transient int tempRadius = 0;

	/**
	 * Start a new World
	 * 
	 * @param name
	 *            World Name
	 * @param camera
	 *            Camera
	 * @param seed
	 *            Seed
	 * @param dimension
	 *            World Dimension
	 * @author Guerra24 <pablo230699@hotmail.com>
	 */
	public void startWorld(String name, Random seed, int chunkDim, VAPI api, GameControllers gm) {
		this.name = name;
		this.seed = seed;
		this.chunkDim = chunkDim;
		gm.getCamera().setPosition(new Vector3f(10, 128, 10));
		if (existWorld()) {
			loadWorld(gm);
		}
		saveWorld(gm);
		initialize(gm);
		createWorld(gm, api);
	}

	private void initialize(GameControllers gm) {
		noise = new SimplexNoise(128, 0.2f, seed.nextInt());
		chunks = new HashMap<ChunkKey, Chunk>();
		gm.getPhysics().getMobManager().getPlayer().setPosition(gm.getCamera().getPosition());
	}

	private void createWorld(GameControllers gm, VAPI api) {
		Logger.log(Thread.currentThread(), "Generating World");
		xPlayChunk = (int) (gm.getCamera().getPosition().x / 16);
		zPlayChunk = (int) (gm.getCamera().getPosition().z / 16);
		float o = 1f;
		float i = 0f;
		for (int zr = -10; zr <= 10; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -10; xr <= 10; xr++) {
				int xx = xPlayChunk + xr;
				if (zr * zr + xr * xr < 10 * 10) {
					i += 0.00200f;
					gm.guis3.get(1).setScale(new Vector2f(i, 0.041f));
					if (i > 0.5060006f) {
						o -= 0.04f;
						if (o >= 0)
							gm.getSoundSystem().setVolume("menu1", o);
					}
					if (!hasChunk(chunkDim, xx, zz)) {
						if (existChunkFile(chunkDim, xx, zz)) {
							loadChunk(chunkDim, xx, zz, gm);
						} else {
							addChunk(new Chunk(chunkDim, xx, zz, this));
							saveChunk(chunkDim, xx, zz, gm);
						}
					} else {
						getChunk(chunkDim, xx, zz).update(this);
					}
				}
			}
		}
	}

	public void updateChunkGeneration(GameControllers gm, VAPI api) {
		if (gm.getCamera().getPosition().x < 0)
			xPlayChunk = (int) ((gm.getCamera().getPosition().x - 16) / 16);
		if (gm.getCamera().getPosition().z < 0)
			zPlayChunk = (int) ((gm.getCamera().getPosition().z - 16) / 16);
		if (gm.getCamera().getPosition().x > 0)
			xPlayChunk = (int) ((gm.getCamera().getPosition().x) / 16);
		if (gm.getCamera().getPosition().z > 0)
			zPlayChunk = (int) ((gm.getCamera().getPosition().z) / 16);
		KernelConstants.update();
		for (int zr = -tempRadius; zr <= tempRadius; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -tempRadius; xr <= tempRadius; xr++) {
				int xx = xPlayChunk + xr;
				if (zr * zr + xr * xr <= (KernelConstants.genRadius - KernelConstants.radiusLimit)
						* (KernelConstants.genRadius - KernelConstants.radiusLimit)) {
					if (!hasChunk(chunkDim, xx, zz)) {
						if (existChunkFile(chunkDim, xx, zz)) {
							loadChunk(chunkDim, xx, zz, gm);
						} else {
							addChunk(new Chunk(chunkDim, xx, zz, this));
							saveChunk(chunkDim, xx, zz, gm);
						}
					} else {
						getChunk(chunkDim, xx, zz).update(this);
					}
				}
				if (zr * zr + xr * xr <= KernelConstants.genRadius * KernelConstants.genRadius
						&& zr * zr + xr * xr >= (KernelConstants.genRadius - KernelConstants.radiusLimit)
								* (KernelConstants.genRadius - KernelConstants.radiusLimit - 1)) {
					if (hasChunk(chunkDim, xx, zz)) {
						saveChunk(chunkDim, xx, zz, gm);
						removeChunk(getChunk(chunkDim, xx, zz));
					}
				}
			}
		}
		if (tempRadius <= KernelConstants.genRadius)
			tempRadius++;
	}

	public void updateChunksRender(GameControllers gm) {
		gm.lights.clear();
		for (int zr = -KernelConstants.radius; zr <= KernelConstants.radius; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -KernelConstants.radius; xr <= KernelConstants.radius; xr++) {
				int xx = xPlayChunk + xr;
				if (hasChunk(chunkDim, xx, zz)) {
					Chunk chunk = getChunk(chunkDim, xx, zz);
					if (chunk.sec1NotClear)
						if (gm.getFrustum().cubeInFrustum(chunk.posX, 0, chunk.posZ, chunk.posX + 16, 32,
								chunk.posZ + 16))
							chunk.render1(gm);

					if (chunk.sec2NotClear)
						if (gm.getFrustum().cubeInFrustum(chunk.posX, 32, chunk.posZ, chunk.posX + 16, 64,
								chunk.posZ + 16))
							chunk.render2(gm);

					if (chunk.sec3NotClear)
						if (gm.getFrustum().cubeInFrustum(chunk.posX, 64, chunk.posZ, chunk.posX + 16, 96,
								chunk.posZ + 16))
							chunk.render3(gm);

					if (chunk.sec4NotClear)
						if (gm.getFrustum().cubeInFrustum(chunk.posX, 96, chunk.posZ, chunk.posX + 16, 128,
								chunk.posZ + 16))
							chunk.render4(gm);

				}
			}
		}

	}

	public void switchDimension(int id, GameControllers gm, VAPI api) {
		clearChunkDimension(gm);
		chunkDim = id;
		initialize(gm);
		createWorld(gm, api);
	}

	public void saveWorld(GameControllers gm) {
		if (!existWorld()) {
			File file = new File(KernelConstants.worldPath + name + "/");
			file.mkdirs();
		}
		if (!existChunkFolder(chunkDim)) {
			File filec = new File(KernelConstants.worldPath + name + "/chunks_" + chunkDim + "/");
			filec.mkdirs();
		}
		String jsonwo = gm.getGson().toJson(seed);
		try {
			FileWriter file = new FileWriter(KernelConstants.worldPath + name + "/world.json");
			file.write(jsonwo);
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadWorld(GameControllers gm) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(KernelConstants.worldPath + name + "/world.json"));
			seed = gm.getGson().fromJson(br, Random.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void saveChunk(int chunkDim, int cx, int cz, GameControllers gm) {
		String json = gm.getGson().toJson(getChunk(chunkDim, cx, cz));
		try {
			File chunksFolder = new File(KernelConstants.worldPath + name + "/chunks_" + chunkDim);
			if (!chunksFolder.exists())
				chunksFolder.mkdirs();
			FileWriter file = new FileWriter(KernelConstants.worldPath + name + "/chunks_" + chunkDim + "/chunk_"
					+ chunkDim + "_" + cx + "_" + cz + ".json");
			file.write(json);
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadChunk(int chunkDim, int cx, int cz, GameControllers gm) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(KernelConstants.worldPath + name + "/chunks_"
					+ chunkDim + "/chunk_" + chunkDim + "_" + cx + "_" + cz + ".json"));
			Chunk chunk = gm.getGson().fromJson(br, Chunk.class);
			if (chunk != null)
				chunk.loadInit();
			else {
				Logger.warn(Thread.currentThread(), "Re-Creating Chunk " + chunkDim + " " + cx + " " + cz);
				chunk = new Chunk(chunkDim, cx, cz, this);
			}
			addChunk(chunk);
		} catch (JsonSyntaxException | FileNotFoundException e) {
			e.printStackTrace();
			Logger.warn(Thread.currentThread(), "Re-Creating Chunk " + chunkDim + " " + cx + " " + cz);
			Chunk chunk = new Chunk(chunkDim, cx, cz, this);
			addChunk(chunk);
			saveChunk(chunkDim, cx, cz, gm);
		}
	}

	public boolean existChunkFile(int chunkDim, int cx, int cz) {
		File file = new File(KernelConstants.worldPath + name + "/chunks_" + chunkDim + "/chunk_" + chunkDim + "_" + cx
				+ "_" + cz + ".json");
		return file.exists();
	}

	public boolean existWorld() {
		File file = new File(KernelConstants.worldPath + name + "/world.json");
		return file.exists();
	}

	public boolean existChunkFolder(int chunkDim) {
		File file = new File(KernelConstants.worldPath + name + "/chunks_" + chunkDim + "/");
		return file.exists();
	}

	public Chunk getChunk(int chunkDim, int cx, int cz) {
		ChunkKey key = ChunkKey.alloc(chunkDim, cx, cz);
		Chunk chunk;
		chunk = chunks.get(key);
		key.free();
		return chunk;
	}

	public boolean hasChunk(int chunkDim, int cx, int cz) {
		ChunkKey key = ChunkKey.alloc(chunkDim, cx, cz);
		boolean contains;
		contains = chunks.containsKey(key);
		key.free();
		return contains;
	}

	public void addChunk(Chunk chunk) {
		ChunkKey key = ChunkKey.alloc(chunk.dim, chunk.cx, chunk.cz);
		Chunk old = chunks.get(key);
		if (old != null) {
			removeChunk(old);
		}
		chunks.put(key.clone(), chunk);
	}

	public void removeChunk(Chunk chunk) {
		if (chunk != null) {
			ChunkKey key = ChunkKey.alloc(chunk.dim, chunk.cx, chunk.cz);
			chunks.remove(key);
			key.free();
		}
	}

	public int getCount() {
		int cnt;
		cnt = chunks.size();
		return cnt;
	}

	public byte getGlobalBlock(int chunkDim, int x, int y, int z) {
		int cx = x >> 4;
		int cz = z >> 4;
		Chunk chunk = getChunk(chunkDim, cx, cz);
		if (chunk != null)
			return chunk.getLocalBlock(x, y, z);
		else
			return -99;
	}

	public void setGlobalBlock(int chunkDim, int x, int y, int z, byte id) {
		int cx = x >> 4;
		int cz = z >> 4;
		Chunk chunk = getChunk(chunkDim, cx, cz);
		if (chunk != null) {
			chunk.setLocalBlock(x, y, z, id);
			chunk.rebuild = true;
		}
	}

	public void clearChunkDimension(GameControllers gm) {
		Logger.log(Thread.currentThread(), "Saving World");
		try {
			WorldThread.sleep(10000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int zr = -KernelConstants.genRadius; zr <= KernelConstants.genRadius; zr++) {
			int zz = getzPlayChunk() + zr;
			for (int xr = -KernelConstants.genRadius; xr <= KernelConstants.genRadius; xr++) {
				int xx = getxPlayChunk() + xr;
				if (zr * zr + xr * xr <= KernelConstants.genRadius * KernelConstants.genRadius) {
					if (hasChunk(chunkDim, xx, zz)) {
						saveChunk(chunkDim, xx, zz, gm);
					}
				}
			}
		}
		chunks.clear();
	}

	public int getzPlayChunk() {
		return zPlayChunk;
	}

	public int getxPlayChunk() {
		return xPlayChunk;
	}

	public int getWorldID() {
		return worldID;
	}

	public int getChunkDimension() {
		return chunkDim;
	}

	public HashMap<ChunkKey, Chunk> getChunks() {
		return chunks;
	}

	public SimplexNoise getNoise() {
		return noise;
	}

	public Random getSeed() {
		return seed;
	}

	public void setTempRadius(int tempRadius) {
		this.tempRadius = tempRadius;
	}

	public int getTempRadius() {
		return tempRadius;
	}

}