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

package net.luxvacuos.voxel.universal.world.chunk.generator;

import net.luxvacuos.voxel.universal.world.block.Blocks;
import net.luxvacuos.voxel.universal.world.block.IBlock;
import net.luxvacuos.voxel.universal.world.utils.BlockNode;

public class ChunkTerrainGenerator extends AbstractChunkGenerator {

	@Override
	protected IBlock generateBlock(int x, int y, int z, double noise, double noise3D) {
		// noise3D *= 8;
		// if (noise3D > 0.8)
//			return Blocks.getBlockByName("voxel:air").getID();

		noise += 1;
		noise *= 128;
		noise = (int) noise;
		if (y == noise - 1 && y > 128)
			return Blocks.getBlockByName("voxel:grass").newInstance(new BlockNode(x, y, z));
		else if (y == noise - 2 && y > 128)
			return Blocks.getBlockByName("voxel:dirt").newInstance(new BlockNode(x, y, z));
		else if (y == noise - 1 && y < 129)
			return Blocks.getBlockByName("voxel:sand").newInstance(new BlockNode(x, y, z));
		else if (y < noise - 2 && y > 128)
			return Blocks.getBlockByName("voxel:stone").newInstance(new BlockNode(x, y, z));
		else if (y < noise - 1 && y < 129)
			return Blocks.getBlockByName("voxel:stone").newInstance(new BlockNode(x, y, z));
		else if (y > noise - 1 && y < 129)
			return Blocks.getBlockByName("voxel:water").newInstance(new BlockNode(x, y, z));
		else
			return Blocks.getBlockByName("voxel:air").newInstance(new BlockNode(x, y, z));
	}

}
