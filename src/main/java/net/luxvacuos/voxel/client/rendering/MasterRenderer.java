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

package net.luxvacuos.voxel.client.rendering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;

import net.luxvacuos.igl.vector.Matrix4f;
import net.luxvacuos.voxel.client.core.VoxelVariables;
import net.luxvacuos.voxel.client.rendering.api.opengl.WaterRenderer;
import net.luxvacuos.voxel.client.resources.GameResources;
import net.luxvacuos.voxel.client.resources.models.TexturedModel;
import net.luxvacuos.voxel.client.world.block.BlockEntity;
import net.luxvacuos.voxel.client.world.entities.GameEntity;

public abstract class MasterRenderer {

	/**
	 * Master Renderer Data
	 */
	protected Matrix4f projectionMatrix;
	protected Map<TexturedModel, List<GameEntity>> entities = new HashMap<TexturedModel, List<GameEntity>>();
	protected Map<TexturedModel, List<BlockEntity>> blockEntities = new HashMap<TexturedModel, List<BlockEntity>>();

	/**
	 * Constructor, Initializes the OpenGL code, creates the projection matrix,
	 * entity renderer and skybox renderer
	 * 
	 * @param loader
	 *            Game Loader
	 */
	public MasterRenderer(GameResources gm) {
		init();
	}

	/**
	 * Init
	 * 
	 */
	public abstract void init();

	/**
	 * Render the Chunk
	 * 
	 * @param cubes1temp
	 *            A list of BlockEntity
	 * @param lights1
	 *            A list of Lights
	 * @param camera
	 *            A Camera
	 */
	public abstract void processChunk(List<Object> cubes, GameResources gm);

	/**
	 * Render the Entity's
	 * 
	 * @param list.get(index)
	 *            A list of Entity's
	 * @param lights
	 *            A list of Lights
	 * @param camera
	 *            A Camera
	 */
	public abstract void renderEntity(ImmutableArray<Entity> immutableArray, GameResources gm);

	protected abstract void renderChunk(GameResources gm);

	/**
	 * Entity's Rendering PipeLine
	 * 
	 * @param lights
	 *            A list of Lights
	 * @param camera
	 *            A Camera
	 */
	protected abstract void renderEntity(GameResources gm);

	/**
	 * Add the BlockEntity to the batcher map
	 * 
	 * @param BlockEntity
	 *            An Entity
	 */
	protected abstract void processBlockEntity(BlockEntity entity);

	/**
	 * Add the Entity to the batcher map
	 * 
	 * @param entity
	 *            An Entity
	 */
	protected abstract void processEntity(GameEntity entity);

	/**
	 * Clear the Buffers
	 * 
	 */
	public abstract void prepare();

	public void update(GameResources gm) {
		projectionMatrix = createProjectionMatrix(projectionMatrix, gm.getDisplay().getDisplayWidth(),
				gm.getDisplay().getDisplayHeight(), VoxelVariables.FOV, VoxelVariables.NEAR_PLANE,
				VoxelVariables.FAR_PLANE);
	}

	public static Matrix4f createProjectionMatrix(int width, int height, float fov, float nearPlane, float farPlane) {
		return createProjectionMatrix(new Matrix4f(), width, height, fov, nearPlane, farPlane);
	}

	public static Matrix4f createProjectionMatrix(Matrix4f proj, int width, int height, float fov, float nearPlane,
			float farPlane) {
		float aspectRatio = (float) width / (float) height;
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(fov / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustrum_length = farPlane - nearPlane;

		proj.setIdentity();
		proj.m00 = x_scale;
		proj.m11 = y_scale;
		proj.m22 = -((farPlane + nearPlane) / frustrum_length);
		proj.m23 = -1;
		proj.m32 = -((2 * nearPlane * farPlane) / frustrum_length);
		proj.m33 = 0;
		return proj;
	}

	/**
	 * Clear the Shader
	 * 
	 */
	public abstract void cleanUp();

	/**
	 * Gets the Projection matrix
	 * 
	 * @return A Projection Matrix
	 */
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public void setProjectionMatrix(Matrix4f matrix) {
		projectionMatrix = matrix;
	}

	public abstract WaterRenderer getWaterRenderer();
}