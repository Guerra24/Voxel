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

package io.github.guerra24.voxel.client.world;

import io.github.guerra24.voxel.client.kernel.Kernel;
import io.github.guerra24.voxel.client.world.chunks.Chunk;
import io.github.guerra24.voxel.client.world.entities.Camera;

import java.util.Random;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class World {

	private int octaveCount;
	public int viewDistance = 32;
	public float[][] perlinNoiseArray;
	public int time = 0;
	public Chunk[][] chunks;
	public byte[][][] blocks;
	public byte[][][] water;
	public boolean isCustomSeed = false;
	public Random seed;

	public void startWorld() {
		initialize();
		createWorld();
	}

	private void initialize() {
		chunks = new Chunk[viewDistance][viewDistance];
		octaveCount = 7;
		if (isCustomSeed) {
			seed = new Random("X".hashCode());
		} else {
			seed = new Random();
		}
		blocks = new byte[viewDistance * 16][144][viewDistance * 16];
		water = new byte[viewDistance * 16][144][viewDistance * 16];
		perlinNoiseArray = new float[Chunk.CHUNK_SIZE * viewDistance][];
		perlinNoiseArray = PerlinNoise.GeneratePerlinNoise(Chunk.CHUNK_SIZE
				* viewDistance, Chunk.CHUNK_SIZE * viewDistance, octaveCount);
		Kernel.gameResources.camera.setPosition(new Vector3f(
				viewDistance / 2 * 16, 64, viewDistance / 2 * 16));
		// Kernel.gameResources.player.setPosition(Kernel.gameResources.camera
		// .getPosition());
	}

	private void createWorld() {
		for (int x = 0; x < viewDistance; x++) {
			for (int z = 0; z < viewDistance; z++) {
				chunks[x][z] = new Chunk(new Vector3f(x * Chunk.CHUNK_SIZE, 0,
						z * Chunk.CHUNK_SIZE), false);
			}
		}
		for (int x = 0; x < viewDistance; x++) {
			for (int z = 0; z < viewDistance; z++) {
				chunks[x][z].remove();
				chunks[x][z] = null;
			}
		}
	}

	public void test() {
		if (Mouse.isButtonDown(0)) {
			Kernel.gameResources.mouse.update();
			if (Kernel.gameResources.mouse.getCurrentRay().x
					+ Kernel.gameResources.camera.getPosition().x >= 0
					&& Kernel.gameResources.mouse.getCurrentRay().y
							+ Kernel.gameResources.camera.getPosition().y >= 0
					&& Kernel.gameResources.mouse.getCurrentRay().z
							+ Kernel.gameResources.camera.getPosition().z >= 0) {
				blocks[(int) (Kernel.gameResources.mouse.getCurrentRay().x + Kernel.gameResources.camera
						.getPosition().x)][(int) (Kernel.gameResources.mouse
						.getCurrentRay().y + Kernel.gameResources.camera
						.getPosition().y)][(int) (Kernel.gameResources.mouse
						.getCurrentRay().z + Kernel.gameResources.camera
						.getPosition().z)] = 0;
				chunks[(int) (Kernel.gameResources.camera.getPosition().x / 16)][(int) (Kernel.gameResources.camera
						.getPosition().z / 16)].update();
			}
		}
	}

	public void update(Camera camera) {
		time++;

		if (time % 10 == 0) {

			int xPlayChunk = (int) (camera.getPosition().x / 16);
			int zPlayChunk = (int) (camera.getPosition().z / 16);
			int radius = 2;
			for (int zr = -radius; zr <= radius; zr++) {
				int zz = zPlayChunk + zr;
				if (zz < 0)
					zz = 0;
				if (zz > viewDistance)
					zz = viewDistance;

				for (int xr = -radius; xr <= radius; xr++) {
					int xx = xPlayChunk + xr;
					if (xx < 0)
						xx = 0;
					if (xx > viewDistance)
						xx = viewDistance;

					if (zr * zr + xr * xr < radius * radius) {
						if (chunks[xx][zz] != null) {
						} else {
							chunks[xx][zz] = new Chunk(new Vector3f(xx
									* Chunk.CHUNK_SIZE, 0, zz
									* Chunk.CHUNK_SIZE), true);
						}
					}
				}
			}
			time = 0;
		}
	}

	public byte getBlock(int x, int y, int z) {
		return blocks[x][y][z];
	}

	public double distanceFromPlayer(int x, int z, int i, int k) {
		int xx = x - i;
		int zz = z - k;
		return Math.sqrt(xx * xx + zz * zz);
	}

}
