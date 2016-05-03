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

package net.luxvacuos.voxel.client.core;

import net.luxvacuos.voxel.client.util.Maths;

public class WorldSimulation {

	private float moveFactor = 0;
	private float time = 0;
	private float globalTime = 0;
	private float rainFactor;

	private static final float TIME_MULTIPLIER = 10;

	public WorldSimulation() {
		time = 12000;
	}

	public float update(float delta) {
		moveFactor += VoxelVariables.WAVE_SPEED * delta;
		moveFactor %= 6.3f;
		time += delta * TIME_MULTIPLIER;
		time %= 24000;
		globalTime += delta * TIME_MULTIPLIER;
		float res = time * 0.015f;

		if (VoxelVariables.raining) {
			rainFactor += 0.2f * delta;
		} else
			rainFactor -= 0.2f * delta;

		rainFactor = Maths.clamp(rainFactor, 0f, 1f);

		return res - 90;
	}

	public float getMoveFactor() {
		return moveFactor;
	}

	public float getGlobalTime() {
		return globalTime;
	}

	public float getRainFactor() {
		return rainFactor;
	}

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

}