/*
 * This file is part of Voxel
 * 
 * Copyright (C) 2016 Lux Vacuos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.luxvacuos.voxel.client.world;

import static org.lwjgl.opengl.GL15.GL_QUERY_RESULT;
import static org.lwjgl.opengl.GL15.glGetQueryObjectui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import net.luxvacuos.igl.Logger;
import net.luxvacuos.igl.vector.Vector3f;
import net.luxvacuos.voxel.client.core.VoxelVariables;
import net.luxvacuos.voxel.client.particle.ParticlePoint;
import net.luxvacuos.voxel.client.particle.ParticleSystem;
import net.luxvacuos.voxel.client.resources.GameResources;
import net.luxvacuos.voxel.client.util.Maths;
import net.luxvacuos.voxel.client.world.block.Block;
import net.luxvacuos.voxel.client.world.chunks.Chunk;
import net.luxvacuos.voxel.client.world.chunks.ChunkGenerator;
import net.luxvacuos.voxel.client.world.chunks.ChunkKey;
import net.luxvacuos.voxel.client.world.chunks.LightNode;

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
	private int version = 1;
	private Map<ChunkKey, Chunk> chunks;
	private Random seed;
	private SimplexNoise noise;
	private String name;
	private int xPlayChunk;
	private int zPlayChunk;
	private int yPlayChunk;
	private int tempRadius = 0;
	private int seedi;
	private ChunkGenerator chunkGenerator;
	private String codeName = "Infinity";
	private Queue<LightNode> lightNodes;
	private ParticleSystem particleSystem;
	private WorldService worldService;
	private int renderedChunks = 0;
	private boolean saving;

	@Override
	public void startWorld(String name, Random seed, int chunkDim, GameResources gm) {
		this.name = name;
		this.seed = seed;
		this.chunkDim = chunkDim;
		gm.getCamera().setPosition(new Vector3f(0, 140, 0));
		if (existWorld()) {
			loadWorld(gm);
		}
		saveWorld(gm);
		init(gm);
		createDimension(gm);
	}

	@Override
	public void init(GameResources gm) {
		particleSystem = new ParticleSystem(gm.getTorchTexture(), 2, 1, -0.01f, 4, 0.5f);
		particleSystem.setDirection(new Vector3f(0, 1, 0), 0.1f);
		particleSystem.setLifeError(0.8f);
		particleSystem.setScaleError(0.2f);
		particleSystem.setSpeedError(0.2f);
		seedi = seed.nextInt();
		noise = new SimplexNoise(256, 0.15f, seedi);
		lightNodes = new LinkedList<>();
		chunks = new HashMap<ChunkKey, Chunk>();
		chunkGenerator = new ChunkGenerator();
		worldService = new WorldService();
	}

	@Override
	public void createDimension(GameResources gm) {
		Logger.log("Generating World");
		xPlayChunk = (int) (gm.getCamera().getPosition().x / 16);
		zPlayChunk = (int) (gm.getCamera().getPosition().z / 16);
		yPlayChunk = (int) (gm.getCamera().getPosition().y / 16);
		for (int zr = -4; zr <= 4; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -4; xr <= 4; xr++) {
				int xx = xPlayChunk + xr;
				for (int yr = -4; yr <= 4; yr++) {
					int yy = yPlayChunk + yr;
					if (zr * zr + xr * xr + yr * yr < 4 * 4 * 4) {
						if (!hasChunk(chunkDim, xx, yy, zz)) {
							if (existChunkFile(chunkDim, xx, yy, zz)) {
								loadChunk(chunkDim, xx, yy, zz, gm);
							} else {
								addChunk(new Chunk(chunkDim, xx, yy, zz, this, gm));
								saveChunk(chunkDim, xx, yy, zz, gm);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void updateChunksGeneration(GameResources gm, float delta) {
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
					if (zr * zr + xr * xr + yr * yr <= (VoxelVariables.genRadius - VoxelVariables.radiusLimit)
							* (VoxelVariables.genRadius - VoxelVariables.radiusLimit)
							* (VoxelVariables.genRadius - VoxelVariables.radiusLimit)) {
						if (!hasChunk(chunkDim, xx, yy, zz)) {
							if (existChunkFile(chunkDim, xx, yy, zz)) {
								loadChunk(chunkDim, xx, yy, zz, gm);
							} else {
								if (VoxelVariables.generateChunks) {
									addChunk(new Chunk(chunkDim, xx, yy, zz, this, gm));
									// saveChunk(chunkDim, xx, yy, zz, gm);
								}
							}
						} else {
							Chunk chunk = getChunk(chunkDim, xx, yy, zz);
							chunk.update(this, worldService, gm.getCamera());
							if (gm.getFrustum().cubeInFrustum(chunk.posX, chunk.posY, chunk.posZ, chunk.posX + 16,
									chunk.posY + 16, chunk.posZ + 16)) {
								chunk.rebuild(worldService, this);
							}
							for (ParticlePoint particlePoint : chunk.getParticlePoints()) {
								particleSystem.generateParticles(particlePoint, delta);
							}
						}
					}
					if (zr * zr + xr * xr + yr * yr <= VoxelVariables.genRadius * VoxelVariables.genRadius
							* VoxelVariables.genRadius
							&& zr * zr + xr * xr
									+ yr * yr >= (VoxelVariables.genRadius - VoxelVariables.radiusLimit + 1)
											* (VoxelVariables.genRadius - VoxelVariables.radiusLimit + 1)
											* (VoxelVariables.genRadius - VoxelVariables.radiusLimit + 1)) {

						if (hasChunk(chunkDim, xx, yy, zz)) {
							saveChunk(chunkDim, xx, yy, zz, gm);
							removeChunk(getChunk(chunkDim, xx, yy, zz));
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
		renderedChunks = 0;
		for (int zr = -VoxelVariables.radius; zr <= VoxelVariables.radius; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -VoxelVariables.radius; xr <= VoxelVariables.radius; xr++) {
				int xx = xPlayChunk + xr;
				for (int yr = -VoxelVariables.radius; yr <= VoxelVariables.radius; yr++) {
					int yy = yPlayChunk + yr;
					if (hasChunk(chunkDim, xx, yy, zz)) {
						Chunk chunk = getChunk(chunkDim, xx, yy, zz);
						if (chunk != null) {
							if (gm.getFrustum().cubeInFrustum(chunk.posX, chunk.posY, chunk.posZ, chunk.posX + 16,
									chunk.posY + 16, chunk.posZ + 16)) {
								int res = glGetQueryObjectui(chunk.getTess().getOcclusion(), GL_QUERY_RESULT);
								if (res > 0) {
									chunk.render(gm);
									renderedChunks++;
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void updateChunksShadow(GameResources gm) {
		for (int zr = -VoxelVariables.radius; zr <= VoxelVariables.radius; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -VoxelVariables.radius; xr <= VoxelVariables.radius; xr++) {
				int xx = xPlayChunk + xr;
				for (int yr = -VoxelVariables.radius; yr <= VoxelVariables.radius; yr++) {
					int yy = yPlayChunk + yr;
					if (hasChunk(chunkDim, xx, yy, zz)) {
						Chunk chunk = getChunk(chunkDim, xx, yy, zz);
						if (chunk != null)
							if (gm.getFrustum().cubeInFrustum(chunk.posX, chunk.posY, chunk.posZ, chunk.posX + 16,
									chunk.posY + 16, chunk.posZ + 16))
								chunk.renderShadow(gm);
					}
				}
			}
		}
	}

	@Override
	public void updateChunksOcclusion(GameResources gm) {
		List<Chunk> chunks = new ArrayList<Chunk>();
		for (int zr = -VoxelVariables.radius; zr <= VoxelVariables.radius; zr++) {
			int zz = zPlayChunk + zr;
			for (int xr = -VoxelVariables.radius; xr <= VoxelVariables.radius; xr++) {
				int xx = xPlayChunk + xr;
				for (int yr = -VoxelVariables.radius; yr <= VoxelVariables.radius; yr++) {
					int yy = yPlayChunk + yr;
					if (hasChunk(chunkDim, xx, yy, zz)) {
						Chunk chunk = getChunk(chunkDim, xx, yy, zz);
						if (chunk != null)
							if (gm.getFrustum().cubeInFrustum(chunk.posX, chunk.posY, chunk.posZ, chunk.posX + 16,
									chunk.posY + 16, chunk.posZ + 16))
								chunks.add(chunk);
					}
				}
			}
		}
		Maths.sortLowToHigh(chunks);
		for (Chunk chunk : chunks) {
			chunk.renderOcclusion(gm);
		}
	}

	@Override
	public void lighting() {
		while (!lightNodes.isEmpty()) {
			LightNode node = lightNodes.poll();
			int x = node.x;
			int y = node.y;
			int z = node.z;
			int cx = x >> 4;
			int cz = z >> 4;
			int cy = y >> 4;
			Chunk chunk = getChunk(chunkDim, cx, cy, cz);
			int lightLevel = (int) chunk.getTorchLight(x, y, z);
			if (chunk.getTorchLight(x - 1, y, z) + 2 <= lightLevel) {
				setupLight(x - 1, y, z, lightLevel);
			}
			if (chunk.getTorchLight(x + 1, y, z) + 2 <= lightLevel) {
				setupLight(x + 1, y, z, lightLevel);
			}
			if (chunk.getTorchLight(x, y, z - 1) + 2 <= lightLevel) {
				setupLight(x, y, z - 1, lightLevel);
			}
			if (chunk.getTorchLight(x, y, z + 1) + 2 <= lightLevel) {
				setupLight(x, y, z + 1, lightLevel);
			}
			if (chunk.getTorchLight(x, y - 1, z) + 2 <= lightLevel) {
				setupLight(x, y - 1, z, lightLevel);
			}
			if (chunk.getTorchLight(x, y + 1, z) + 2 <= lightLevel) {
				setupLight(x, y + 1, z, lightLevel);
			}
		}
	}

	@Override
	public void setupLight(int x, int y, int z, int lightLevel) {
		int cx = x >> 4;
		int cz = z >> 4;
		int cy = y >> 4;
		Chunk chunk = getChunk(chunkDim, cx, cy, cz);
		if (chunk != null)
			if (chunk.getTorchLight(x, y, z) + 2 <= lightLevel) {
				chunk.setTorchLight(x, y, z, lightLevel - 1);
				chunk.needsRebuild = true;
				lightNodes.add(new LightNode(x, y, z));
			}
	}

	@Override
	public void switchDimension(int id, GameResources gm) {
		if (id != chunkDim) {
			clearDimension(gm);
			chunkDim = id;
			init(gm);
			createDimension(gm);
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
		try {
			Output output = new Output(new FileOutputStream(VoxelVariables.worldPath + name + "/world.dat"));
			gm.getKryo().writeObject(output, seed);
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadWorld(GameResources gm) {
		try {
			Input input = new Input(new FileInputStream(VoxelVariables.worldPath + name + "/world.dat"));
			seed = gm.getKryo().readObject(input, Random.class);
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveChunk(int chunkDim, int cx, int cy, int cz, GameResources gm) {
		try {
			Output output = new Output(new FileOutputStream(VoxelVariables.worldPath + name + "/chunks_" + chunkDim
					+ "/chunk_" + chunkDim + "_" + cx + "_" + cy + "_" + cz + ".dat"));
			gm.getKryo().writeObject(output, getChunk(chunkDim, cx, cy, cz));
			output.close();
		} catch (Exception e) {
			Logger.warn("Error Saving Chunk " + chunkDim + " " + cx + " " + cy + " " + cz);
			e.printStackTrace();
		}
	}

	@Override
	public void loadChunk(int chunkDim, int cx, int cy, int cz, GameResources gm) {
		try {
			Input input = new Input(new FileInputStream(VoxelVariables.worldPath + name + "/chunks_" + chunkDim
					+ "/chunk_" + chunkDim + "_" + cx + "_" + cy + "_" + cz + ".dat"));
			Chunk chunk = gm.getKryo().readObject(input, Chunk.class);
			input.close();
			if (chunk != null) {
				chunk.load(gm);
				chunk.checkForMissingBlocks();
				if (chunk.version != this.version) {
					Logger.warn("An invalid chunk has been detected in: " + cx + " " + cy + " " + cz);
					chunk = new Chunk(chunkDim, cx, cy, cz, this, gm);
				}
				addChunk(chunk);
			}

		} catch (Exception e) {
			Logger.warn("Error Loading Chunk " + chunkDim + " " + cx + " " + cy + " " + cz);
			e.printStackTrace();
		}
	}

	@Override
	public boolean existChunkFile(int chunkDim, int cx, int cy, int cz) {
		File file = new File(VoxelVariables.worldPath + name + "/chunks_" + chunkDim + "/chunk_" + chunkDim + "_" + cx
				+ "_" + cy + "_" + cz + ".dat");
		return file.exists();
	}

	/**
	 * Check if exist a world file
	 * 
	 * @return true if exist
	 */
	@Override
	public boolean existWorld() {
		File file = new File(VoxelVariables.worldPath + name + "/world.dat");
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
		key.free();
		for (int xx = chunk.cx - 1; xx < chunk.cx + 1; xx++) {
			for (int zz = chunk.cz - 1; zz < chunk.cz + 1; zz++) {
				for (int yy = chunk.cy - 1; yy < chunk.cy + 1; yy++) {
					Chunk chunka = getChunk(chunkDim, xx, yy, zz);
					if (chunka != null) {
						chunka.needsRebuild = true;
					}
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
			for (int xx = chunk.cx - 1; xx < chunk.cx + 1; xx++) {
				for (int zz = chunk.cz - 1; zz < chunk.cz + 1; zz++) {
					for (int yy = chunk.cy - 1; yy < chunk.cy + 1; yy++) {
						Chunk chunka = getChunk(chunkDim, xx, yy, zz);
						if (chunka != null) {
							chunka.needsRebuild = true;
						}
					}
				}
			}
			chunk = null;
		}
	}

	@Override
	public int getLoadedChunks() {
		return chunks.size();
	}

	@Override
	public int getRenderedChunks() {
		return renderedChunks;
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
	public List<BoundingBox> getGlobalBoundingBox(BoundingBox box) {
		List<BoundingBox> array = new ArrayList<>();
		Vector3f vec = new Vector3f(0, 0, 0);

		for (int i = (int) Math.floor(box.min.x); i < (int) Math.ceil(box.max.x); i++) {
			for (int j = (int) Math.floor(box.min.y); j < (int) Math.ceil(box.max.y); j++) {
				for (int k = (int) Math.floor(box.min.z); k < (int) Math.ceil(box.max.z); k++) {
					vec.set(i, j, k);
					BoundingBox cmp = Block.getBlock(getGlobalBlock(i, j, k)).getBoundingBox(vec);
					if (cmp != null) {
						array.add(cmp);
					}
				}
			}
		}
		return array;
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
		if (chunk != null) {
			chunk.setTorchLight(x, y, z, val);
			lightNodes.add(new LightNode(x, y, z));
		}
	}

	@Override
	public float getLight(int x, int y, int z) {
		int cx = x >> 4;
		int cz = z >> 4;
		int cy = y >> 4;
		Chunk chunk = getChunk(chunkDim, cx, cy, cz);
		if (chunk != null) {
			return chunk.getTorchLight(x, y, z);
		}
		return 0;
	}

	@Override
	public void clearDimension(GameResources gm) {
		if (!saving) {
			saving = true;
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
			worldService.es.shutdown();
			chunks.clear();
			saveWorld(gm);
		}
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

}