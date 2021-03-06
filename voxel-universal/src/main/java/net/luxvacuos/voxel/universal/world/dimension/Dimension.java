/*
 * This file is part of Voxel
 * 
 * Copyright (C) 2016-2018 Lux Vacuos
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

package net.luxvacuos.voxel.universal.world.dimension;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.joml.Vector3f;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.hackhalo2.nbt.CompoundBuilder;
import com.hackhalo2.nbt.stream.NBTOutputStream;
import com.hackhalo2.nbt.tags.TagCompound;
import com.hackhalo2.nbt.tags.TagLong;

import net.luxvacuos.igl.Logger;
import net.luxvacuos.lightengine.universal.core.IWorldSimulation;
import net.luxvacuos.lightengine.universal.core.WorldSimulation;
import net.luxvacuos.lightengine.universal.core.subsystems.CoreSubsystem;
import net.luxvacuos.lightengine.universal.ecs.entities.LEEntity;
import net.luxvacuos.lightengine.universal.ecs.entities.PlayerEntity;
import net.luxvacuos.lightengine.universal.network.AbstractChannelHandler;
import net.luxvacuos.lightengine.universal.util.registry.Key;
import net.luxvacuos.voxel.universal.ecs.Components;
import net.luxvacuos.voxel.universal.ecs.components.ChunkLoader;
import net.luxvacuos.voxel.universal.world.IWorld;
import net.luxvacuos.voxel.universal.world.block.Blocks;
import net.luxvacuos.voxel.universal.world.block.IBlock;
import net.luxvacuos.voxel.universal.world.chunk.ChunkManager;
import net.luxvacuos.voxel.universal.world.chunk.IChunk;
import net.luxvacuos.voxel.universal.world.chunk.generator.ChunkTerrainGenerator;
import net.luxvacuos.voxel.universal.world.chunk.generator.SimplexNoise;
import net.luxvacuos.voxel.universal.world.utils.BlockNode;
import net.luxvacuos.voxel.universal.world.utils.ChunkNode;

public class Dimension extends AbstractChannelHandler implements IDimension {

	private int id;
	protected IWorld world;
	protected ChunkManager chunkManager;
	protected TagCompound data;

	public Dimension(IWorld world, TagCompound data, int id) {
		this.world = world;
		this.data = data;
		this.id = id;
		this.setupWorldSimulator();
		long seed = 0l;
		try {
			super.worldSimulation.setTime(this.data.getFloat("Time"));
			// super.worldSimulation.setRainFactor(this.data.getFloat("RainFactor"));
			if (this.data.hasTagByName("Seed")) {
				seed = this.data.getLong("Seed");
			} else {
				seed = new Random().nextLong();
				this.data.addTag(new TagLong("Seed", seed));
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		this.setupChunkManager(new Random(seed));
		super.engine = new Engine();
		super.engine.addSystem(new PhysicsSystem(this));
	}

	protected void setupChunkManager(Random rgn) {
		this.chunkManager = new ChunkManager(this);
		ChunkTerrainGenerator gen = new ChunkTerrainGenerator();
		gen.setNoiseGenerator(new SimplexNoise(256, 0.15f, rgn.nextInt()));
		this.chunkManager.setGenerator(gen);
	}

	protected void setupWorldSimulator() {
		super.worldSimulation = new WorldSimulation();
	}

	@Override
	public String getWorldName() {
		return world.getName();
	}

	@Override
	public int getID() {
		return id;
	}

	@Override
	public void update(float delta) {
		int entityCX = 0, entityCY = 0, entityCZ = 0, chunkRadius = 0;
		ImmutableArray<Entity> players = super.engine.getEntitiesFor(Family.all(ChunkLoader.class).get());
		Array<ChunkNode> toRemove = new Array<>();
		for (Entity entity : players) {
			ChunkLoader loader = Components.CHUNK_LOADER.get(entity);
			Vector3f position = ((LEEntity) entity).getPosition();

			if (position.x < 0)
				entityCX = (int) ((position.x - 16) / 16);
			else
				entityCX = (int) ((position.x) / 16);

			if (position.z < 0)
				entityCY = (int) ((position.y - 16) / 16);
			else
				entityCY = (int) ((position.y) / 16);

			if (position.z < 0)
				entityCZ = (int) ((position.z - 16) / 16);
			else
				entityCZ = (int) ((position.z) / 16);

			chunkRadius = loader.getChunkRadius();

			ChunkNode node;
			int xx, yy, zz;
			for (int zr = -chunkRadius; zr <= chunkRadius; zr++) {
				zz = entityCZ + zr;
				for (int yr = -chunkRadius; yr <= chunkRadius; yr++) {
					yy = entityCY + yr;
					for (int xr = -chunkRadius; xr <= chunkRadius; xr++) {
						xx = entityCX + xr;
						node = new ChunkNode(xx, yy, zz);
						if (!chunkManager.isChunkLoaded(node))
							chunkManager.loadChunk(node);
						else
							chunkManager.getChunkAt(node).registerChunkLoader(entity);
					}
				}
			}
			for (IChunk chunk : chunkManager.getLoadedChunks()) {
				if (Math.abs(chunk.getNode().getX() - entityCX) > chunkRadius
						|| Math.abs(chunk.getNode().getZ() - entityCZ) > chunkRadius) {
					chunk.removeChunkLoader(entity);
				}
			}

		}
		for (IChunk chunk : chunkManager.getLoadedChunks()) {
			if (chunk.chunkLoaders() <= 0)
				toRemove.add(chunk.getNode());
		}

		for (ChunkNode chunkNode : toRemove) {
			chunkManager.unloadChunk(chunkNode);
		}

		chunkManager.update(delta);
		super.worldSimulation.update(delta);
		super.engine.update(delta);
	}

	@Override
	public void beforeUpdate(float delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterUpdate(float delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public IChunk getChunkAt(int x, int y, int z) {
		return this.chunkManager.getChunkAt(ChunkNode.getFromBlockCoords(x, y, z));
	}

	@Override
	public IBlock getBlockAt(int x, int y, int z) {
		IBlock block = Blocks.getBlockByName("voxel:air").newInstance(new BlockNode(x, y, z));
		block.setPosition(x, y, z);

		IChunk c = this.chunkManager.getChunkAt(ChunkNode.getFromBlockCoords(x, y, z));
		if (c == null)
			return block;

		IBlock b = c.getBlockAt(x & 0xF, y & 0xF, z & 0xF);
		if (b == null)
			return block;

		b.setPosition(x, y, z);
		return b;
	}

	@Override
	public boolean setBlockAt(int x, int y, int z, IBlock block) {
		IChunk c = this.chunkManager.getChunkAt(ChunkNode.getFromBlockCoords(x, y, z));
		if (c == null)
			return false;

		// Trigger Block Updates and chunk rebuilds if needed
		ChunkNode node;
		BlockNode bNode = new BlockNode(x, y, z);
		for (int mx = x - 1; mx <= x + 1; mx++) {
			for (int my = y - 1; my <= y + 1; my++) {
				for (int mz = z - 1; mz <= z + 1; mz++) {
					if ((mx == x) && (my == y) && (mz == z))
						continue;

					node = ChunkNode.getFromBlockCoords(mx, my, mz);
					if (!c.getNode().equals(node)) {
						IChunk mc = this.chunkManager.getChunkAt(node);
						if (mc != null) { // Mark neighbor chunk for a rebuild
							mc.markForRebuild();
						}
					}

					// Trigger a block update for blocks that care
					// XXX: Maybe move to an event system like
					// Bukkit/Spout/Forge?
					// this.getBlockAt(mx, my, mz).onBlockUpdate(bNode, block);
				}
			}
		}

		c.setBlockAt(x & 0xF, y & 0xF, z & 0xF, block);
		return true;
	}

	@Override
	public List<BoundingBox> getGlobalBoundingBox(BoundingBox box) {
		List<BoundingBox> array = new ArrayList<>();

		for (int i = (int) Math.floor(box.min.x); i < (int) Math.ceil(box.max.x); i++) {
			// XXX: Hardcoded 256* limit until custom world height is
			// implemented
			for (int j = (int) Math.floor(box.min.y); j < (int) Math.min(Math.ceil(box.max.y), 256); j++) {
				for (int k = (int) Math.floor(box.min.z); k < (int) Math.ceil(box.max.z); k++) {
					IBlock block = this.getBlockAt(i, j, k);
					if (Blocks.getBlockByBlock(block).getMaterial().blocksMovement())
						array.add(Blocks.getBlockByBlock(block).getBoundingBox(new BlockNode(i, j, k)));
				}
			}
		}
		return array;
	}

	@Override
	public void dispose() {
		File file = new File(CoreSubsystem.REGISTRY.getRegistryItem(new Key("/Voxel/Settings/World/directory"))
				+ this.world.getName() + "/dim" + this.id + "_data.nbt");
		NBTOutputStream out = null;
		try {
			out = new NBTOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			CompoundBuilder builder = new CompoundBuilder().modify(this.data);
			builder.modifyFloat("Time", super.worldSimulation.getTime()).modifyFloat("RainFactor",
					// super.worldSimulation.getRainFactor()
					0);
			// builder.addBoolean("IsRaining", false);
			builder.build().writeNBT(out, false);
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Logger.error(e);
				}
			}
		}
		super.engine.removeAllEntities();
		chunkManager.dispose();
	}

	@Override
	public Engine getEntitiesManager() {
		return super.engine;
	}

	@Override
	public Collection<IChunk> getLoadedChunks() {
		return chunkManager.getLoadedChunks();
	}

	@Override
	public Map<UUID, PlayerEntity> getPlayers() {
		return super.players;
	}

	@Override
	public Engine getEngine() {
		return super.engine;
	}

	@Override
	public IWorldSimulation getWorldSimulation() {
		return super.worldSimulation;
	}

}
