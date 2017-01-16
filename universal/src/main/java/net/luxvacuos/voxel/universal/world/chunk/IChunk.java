package net.luxvacuos.voxel.universal.world.chunk;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.luxvacuos.voxel.universal.util.IUpdatable;
import net.luxvacuos.voxel.universal.world.block.IBlock;
import net.luxvacuos.voxel.universal.world.dimension.IDimension;
import net.luxvacuos.voxel.universal.world.utils.ChunkNode;

public interface IChunk extends IChunkHandle, IUpdatable {

	public void setBlockAt(int x, int y, int z, IBlock block);

	public boolean hasCollisionData(int x, int y, int z);

	public IDimension getDimension();

	public void markForRebuild();

	public boolean needsRebuild();

	public void registerChunkLoader(Entity entity);

	public void removeChunkLoader(Entity entity);

	public int chunkLoaders();

	public BoundingBox getBoundingBox(ChunkNode node);
}
