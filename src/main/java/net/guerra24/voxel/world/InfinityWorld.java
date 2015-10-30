/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Guerra24
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.guerra24.voxel.world;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.google.gson.JsonSyntaxException;

import net.guerra24.voxel.api.VAPI;
import net.guerra24.voxel.core.VoxelVariables;
import net.guerra24.voxel.resources.GameResources;
import net.guerra24.voxel.util.Logger;
import net.guerra24.voxel.util.vector.Vector2f;
import net.guerra24.voxel.util.vector.Vector3f;
import net.guerra24.voxel.world.chunks.Chunk;
import net.guerra24.voxel.world.chunks.ChunkGenerator;
import net.guerra24.voxel.world.chunks.ChunkKey;
import net.guerra24.voxel.world.chunks.LightNode;

/**
 * Dimensional World
 * 
 * @author Guerra24 <pablo230699@hotmail.com>
 * @category World
 */
public class InfinityWorld implements IWorld {

	/**
	 * Dimensional World Data
	 */
	private int chunkDim;
	private int worldID;
	private HashMap<ChunkKey, Chunk> chunks;
	private Random seed;
	private SimplexNoise noise;
	private String name;
	private int xPlayChunk;
	private int zPlayChunk;
	private int yPlayChunk;
	private int tempRadius = 0;
	private int seedi;
	private ChunkGenerator chunkGenerator;
	private WorldService service;
	private String codeName = "Infinity";
	private Queue<LightNode> lightNodes;

	@Override
	public void startWorld(String name, Random seed, int chunkDim, VAPI api, GameResources gm) {
		this.name = name;
		this.seed = seed;
		this.chunkDim = chunkDim;
		gm.getCamera().setPosition(new Vector3f(0, 64, 0));
		if (existWorld()) {
			loadWorld(gm);
		}
		saveWorld(gm);
		init(gm);
		createDimension(gm, api);
	}

	@Override
	public void init(GameResources gm) {
		seedi = seed.nextInt();
		noise = new SimplexNoise(128, 0.3f, seedi);
		lightNodes = new LinkedList<>();
		chunks = new HashMap<ChunkKey, Chunk>();
		chunkGenerator = new ChunkGenerator();
		service = new WorldService();
		gm.getPhysics().getMobManager().getPlayer().setPosition(gm.getCamera().getPosition());
	}

	@Override
	public void createDimension(GameResources gm, VAPI api) {
		Logger.log("Generating World");
		xPlayChunk = (int) (gm.getCamera().getPosition().x / 16);
		zPlayChunk = (int) (gm.getCamera().getPosition().z / 16);
		yPlayChunk = (int) (gm.getCamera().getPosition().y / 16);
		float o = 1f;
		float i = 0f;
		for (int zr = -4; zr <= 4; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -4; xr <= 4; xr++) {
				int xx = xPlayChunk + xr;
				for (int yr = -4; yr <= 4; yr++) {
					int yy = yPlayChunk + yr;
					if (zr * zr + xr * xr + yr * yr < 4 * 4 * 4) {
						i += 0.00080f;
						gm.guis3.get(1).setScale(new Vector2f(i, 0.041f));
						if (i > 0.5060006f) {
							o -= 0.04f;
							if (o >= 0)
								gm.getSoundSystem().setVolume("menu1", o);
						}
						if (!hasChunk(chunkDim, xx, yy, zz)) {
							if (existChunkFile(chunkDim, xx, yy, zz)) {
								loadChunk(chunkDim, xx, yy, zz, gm);
							} else {
								addChunk(new Chunk(chunkDim, xx, yy, zz, this));
								saveChunk(chunkDim, xx, yy, zz, gm);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void updateChunksGeneration(GameResources gm, VAPI api) {
		if (gm.getCamera().getPosition().x < 0)
			xPlayChunk = (int) ((gm.getCamera().getPosition().x - 16) / 16);
		if (gm.getCamera().getPosition().y < 0)
			yPlayChunk = (int) ((gm.getCamera().getPosition().y - 16) / 16);
		if (gm.getCamera().getPosition().z < 0)
			zPlayChunk = (int) ((gm.getCamera().getPosition().z - 16) / 16);
		if (gm.getCamera().getPosition().x > 0)
			xPlayChunk = (int) ((gm.getCamera().getPosition().x) / 16);
		if (gm.getCamera().getPosition().y > 0)
			yPlayChunk = (int) ((gm.getCamera().getPosition().y) / 16);
		if (gm.getCamera().getPosition().z > 0)
			zPlayChunk = (int) ((gm.getCamera().getPosition().z) / 16);
		VoxelVariables.update();
		for (int zr = -tempRadius; zr <= tempRadius; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -tempRadius; xr <= tempRadius; xr++) {
				int xx = xPlayChunk + xr;
				for (int yr = -tempRadius; yr <= tempRadius; yr++) {
					int yy = yPlayChunk + yr;
					if (yy >= 0) {
						if (zr * zr + xr * xr + yr * yr <= (VoxelVariables.genRadius - VoxelVariables.radiusLimit)
								* (VoxelVariables.genRadius - VoxelVariables.radiusLimit)
								* (VoxelVariables.genRadius - VoxelVariables.radiusLimit)) {
							if (!hasChunk(chunkDim, xx, yy, zz)) {
								if (!hasChunk(chunkDim, xx, yy, zz)) {
									if (existChunkFile(chunkDim, xx, yy, zz)) {
										loadChunk(chunkDim, xx, yy, zz, gm);
									} else {
										addChunk(new Chunk(chunkDim, xx, yy, zz, this));
										saveChunk(chunkDim, xx, yy, zz, gm);
									}
								}
							} else {
								Chunk chunk = getChunk(chunkDim, xx, yy, zz);
								if (gm.getFrustum().cubeInFrustum(chunk.posX, chunk.posY, chunk.posZ, chunk.posX + 16,
										chunk.posY + 16, chunk.posZ + 16)) {
									if (!chunk.created)
										chunk.createBasicTerrain(this);
									chunk.rebuild(service, this);
								}
							}
						}
						if (zr * zr + xr * xr + yr * yr <= VoxelVariables.genRadius * VoxelVariables.genRadius
								* VoxelVariables.genRadius
								&& zr * zr + xr * xr
										+ yr * yr >= (VoxelVariables.genRadius - VoxelVariables.radiusLimit + 1)
												* (VoxelVariables.genRadius - VoxelVariables.radiusLimit + 1)
												* (VoxelVariables.genRadius - VoxelVariables.radiusLimit + 1)) {
							if (hasChunk(getChunkDimension(), xx, yy, zz)) {
								saveChunk(getChunkDimension(), xx, yy, zz, gm);
								removeChunk(getChunk(getChunkDimension(), xx, yy, zz));
							}
						}
					}
				}
			}
		}
		if (tempRadius <= VoxelVariables.genRadius)
			tempRadius++;
	}

	@Override
	public void updateChunksRender(GameResources gm) {
		for (int zr = -VoxelVariables.radius; zr <= VoxelVariables.radius; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -VoxelVariables.radius; xr <= VoxelVariables.radius; xr++) {
				int xx = xPlayChunk + xr;
				for (int yr = -VoxelVariables.radius; yr <= VoxelVariables.radius; yr++) {
					int yy = yPlayChunk + yr;
					if (hasChunk(chunkDim, xx, yy, zz)) {
						Chunk chunk = getChunk(chunkDim, xx, yy, zz);
						if (gm.getFrustum().cubeInFrustum(chunk.posX, chunk.posY, chunk.posZ, chunk.posX + 16,
								chunk.posY + 16, chunk.posZ + 16))
							chunk.render(gm);
					}
				}
			}
		}

	}

	@Override
	public void lighting() {
		while (!lightNodes.isEmpty()) {
			LightNode node = lightNodes.poll();
			int x = node.x;
			int y = node.y;
			int z = node.z;
			Chunk chunk = node.chunk;
			int lightLevel = (int) chunk.getTorchLight(x, y, z);
			if (x > chunk.posX + 1 && x < chunk.posX + 16) {
				if (chunk.getTorchLight(x - 1, y, z) + 2 <= lightLevel) {
					setupLight(x - 1, y, z, chunk, lightLevel);
				}
			}
			if (x > chunk.posX && x < chunk.posX + 15) {
				if (chunk.getTorchLight(x + 1, y, z) + 2 <= lightLevel) {
					setupLight(x + 1, y, z, chunk, lightLevel);
				}
			}
			if (z > chunk.posZ + 1 && z < chunk.posZ + 16) {
				if (chunk.getTorchLight(x, y, z - 1) + 2 <= lightLevel) {
					setupLight(x, y, z - 1, chunk, lightLevel);
				}
			}
			if (z > chunk.posZ && z < chunk.posZ + 15) {
				if (chunk.getTorchLight(x, y, z + 1) + 2 <= lightLevel) {
					setupLight(x, y, z + 1, chunk, lightLevel);
				}
			}
			if (y > chunk.posY + 1 && y < chunk.posY + 16) {
				if (chunk.getTorchLight(x, y - 1, z) + 2 <= lightLevel) {
					setupLight(x, y - 1, z, chunk, lightLevel);
				}
			}
			if (y > chunk.posY && y < chunk.posY + 15) {
				if (chunk.getTorchLight(x, y + 1, z) + 2 <= lightLevel) {
					setupLight(x, y + 1, z, chunk, lightLevel);
				}
			}
		}
	}

	@Override
	public void setupLight(int x, int y, int z, Chunk chunk, int lightLevel) {
		if (chunk.getTorchLight(x, y, z) + 2 <= lightLevel) {
			chunk.setTorchLight(x, y, z, lightLevel - 1);
			lightNodes.add(new LightNode(x, y, z, chunk));
		}
	}

	@Override
	public void switchDimension(int id, GameResources gm, VAPI api) {
		if (id != chunkDim) {
			clearDimension(gm);
			chunkDim = id;
			init(gm);
			createDimension(gm, api);
		}
	}

	@Override
	public void saveWorld(GameResources gm) {
		if (!existWorld()) {
			File file = new File(VoxelVariables.worldPath + name + "/");
			file.mkdirs();
		}
		if (!existChunkFolder(chunkDim)) {
			File filec = new File(VoxelVariables.worldPath + name + "/chunks_" + chunkDim + "/");
			filec.mkdirs();
		}
		String jsonwo = gm.getGson().toJson(seed);
		try {
			FileWriter file = new FileWriter(VoxelVariables.worldPath + name + "/world.json");
			file.write(jsonwo);
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadWorld(GameResources gm) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(VoxelVariables.worldPath + name + "/world.json"));
			seed = gm.getGson().fromJson(br, Random.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveChunk(int chunkDim, int cx, int cy, int cz, GameResources gm) {
		String json = gm.getGson().toJson(getChunk(chunkDim, cx, cy, cz));
		try {
			File chunksFolder = new File(VoxelVariables.worldPath + name + "/chunks_" + chunkDim);
			if (!chunksFolder.exists())
				chunksFolder.mkdirs();
			FileWriter file = new FileWriter(VoxelVariables.worldPath + name + "/chunks_" + chunkDim + "/chunk_"
					+ chunkDim + "_" + cx + "_" + cy + "_" + cz + ".json");
			file.write(json);
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadChunk(int chunkDim, int cx, int cy, int cz, GameResources gm) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(VoxelVariables.worldPath + name + "/chunks_"
					+ chunkDim + "/chunk_" + chunkDim + "_" + cx + "_" + cy + "_" + cz + ".json"));
			Chunk chunk = gm.getGson().fromJson(br, Chunk.class);
			if (chunk != null)
				chunk.createList();
			else {
				Logger.warn("Re-Creating Chunk " + chunkDim + " " + cx + " " + cy + " " + cz);
				Logger.warn("Probably a chunk corruption");
				chunk = new Chunk(chunkDim, cx, cy, cz, this);
			}
			addChunk(chunk);
		} catch (JsonSyntaxException | FileNotFoundException e) {
			e.printStackTrace();
			Logger.warn("Re-Creating Chunk " + chunkDim + " " + cx + " " + cy + " " + cz);
			Logger.warn("Probably a chunk corruption");
			Chunk chunk = new Chunk(chunkDim, cx, cy, cz, this);
			addChunk(chunk);
			saveChunk(chunkDim, cx, cy, cz, gm);
		}
	}

	@Override
	public boolean existChunkFile(int chunkDim, int cx, int cy, int cz) {
		File file = new File(VoxelVariables.worldPath + name + "/chunks_" + chunkDim + "/chunk_" + chunkDim + "_" + cx
				+ "_" + cy + "_" + cz + ".json");
		return file.exists();
	}

	/**
	 * Check if exist a world file
	 * 
	 * @return true if exist
	 * @author Guerra24 <pablo230699@hotmail.com>
	 */
	@Override
	public boolean existWorld() {
		File file = new File(VoxelVariables.worldPath + name + "/world.json");
		return file.exists();
	}

	@Override
	public boolean existChunkFolder(int chunkDim) {
		File file = new File(VoxelVariables.worldPath + name + "/chunks_" + chunkDim + "/");
		return file.exists();
	}

	@Override
	public Chunk getChunk(int chunkDim, int cx, int cy, int cz) {
		ChunkKey key = ChunkKey.alloc(chunkDim, cx, cy, cz);
		Chunk chunk;
		chunk = chunks.get(key);
		key.free();
		return chunk;
	}

	@Override
	public boolean hasChunk(int chunkDim, int cx, int cy, int cz) {
		ChunkKey key = ChunkKey.alloc(chunkDim, cx, cy, cz);
		boolean contains;
		contains = chunks.containsKey(key);
		key.free();
		return contains;
	}

	@Override
	public void addChunk(Chunk chunk) {
		ChunkKey key = ChunkKey.alloc(chunk.dim, chunk.cx, chunk.cy, chunk.cz);
		Chunk old = chunks.get(key);
		if (old != null) {
			removeChunk(old);
		}
		chunks.put(key.clone(), chunk);
		for (int xx = chunk.cx - 2; xx < chunk.cx + 2; xx++) {
			for (int zz = chunk.cz - 2; zz < chunk.cz + 2; zz++) {
				for (int yy = chunk.cy - 2; yy < chunk.cy + 2; yy++) {
					Chunk chunka = getChunk(chunkDim, xx, yy, zz);
					if (chunka != null)
						chunka.needsRebuild = true;
				}
			}
		}
	}

	@Override
	public void removeChunk(Chunk chunk) {
		if (chunk != null) {
			ChunkKey key = ChunkKey.alloc(chunk.dim, chunk.cx, chunk.cy, chunk.cz);
			chunk.dispose();
			chunks.remove(key);
			key.free();
			for (int xx = chunk.cx - 2; xx < chunk.cx + 2; xx++) {
				for (int zz = chunk.cz - 2; zz < chunk.cz + 2; zz++) {
					for (int yy = chunk.cy - 2; yy < chunk.cy + 2; yy++) {
						Chunk chunka = getChunk(chunkDim, xx, yy, zz);
						if (chunka != null)
							chunka.needsRebuild = true;
					}
				}
			}
		}
	}

	/**
	 * @return Chunks in Memory
	 * @author Guerra24 <pablo230699@hotmail.com>
	 */
	public int getCount() {
		int cnt;
		cnt = chunks.size();
		return cnt;
	}

	@Override
	public byte getGlobalBlock(int x, int y, int z) {
		int cx = x >> 4;
		int cz = z >> 4;
		int cy = y >> 4;
		Chunk chunk = getChunk(chunkDim, cx, cy, cz);
		if (chunk != null)
			return chunk.getLocalBlock(x, y, z);
		else
			return 0;
	}

	@Override
	public void setGlobalBlock(int x, int y, int z, byte id) {
		int cx = x >> 4;
		int cz = z >> 4;
		int cy = y >> 4;
		Chunk chunk = getChunk(chunkDim, cx, cy, cz);
		if (chunk != null) {
			chunk.setLocalBlock(x, y, z, id);
			chunk.updated = false;
			chunk.needsRebuild = true;
		}
	}

	@Override
	public void lighting(int x, int y, int z, int val) {
		int cx = x >> 4;
		int cz = z >> 4;
		int cy = y >> 4;
		Chunk chunk = getChunk(chunkDim, cx, cy, cz);
		chunk.setTorchLight(x, y, z, val);
		lightNodes.add(new LightNode(x, y, z, chunk));
	}

	@Override
	public void clearDimension(GameResources gm) {
		Logger.log("Saving World");
		for (int zr = -VoxelVariables.genRadius; zr <= VoxelVariables.genRadius; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -VoxelVariables.genRadius; xr <= VoxelVariables.genRadius; xr++) {
				int xx = xPlayChunk + xr;
				for (int yr = -VoxelVariables.genRadius; yr <= VoxelVariables.genRadius; yr++) {
					int yy = yPlayChunk + yr;
					if (zr * zr + xr * xr + yr * yr <= VoxelVariables.genRadius * VoxelVariables.genRadius
							* VoxelVariables.genRadius) {
						if (hasChunk(chunkDim, xx, yy, zz)) {
							saveChunk(chunkDim, xx, yy, zz, gm);
						}
					}
				}
			}
		}
		service.es.shutdown();
		chunks.clear();
	}

	@Override
	public int getzPlayChunk() {
		return zPlayChunk;
	}

	@Override
	public int getxPlayChunk() {
		return xPlayChunk;
	}

	@Override
	public int getWorldID() {
		return worldID;
	}

	@Override
	public int getChunkDimension() {
		return chunkDim;
	}

	@Override
	public int getyPlayChunk() {
		return yPlayChunk;
	}

	@Override
	public SimplexNoise getNoise() {
		return noise;
	}

	@Override
	public Random getSeed() {
		return seed;
	}

	@Override
	public ChunkGenerator getChunkGenerator() {
		return chunkGenerator;
	}

	public String getCodeName() {
		return codeName;
	}

	@Override
	public void setTempRadius(int tempRadius) {
		this.tempRadius = tempRadius;
	}

	public int getTempRadius() {
		return tempRadius;
	}

}