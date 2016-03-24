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

package net.luxvacuos.voxel.client.world.entities;

import static net.luxvacuos.voxel.client.input.Keyboard.KEY_A;
import static net.luxvacuos.voxel.client.input.Keyboard.KEY_D;
import static net.luxvacuos.voxel.client.input.Keyboard.KEY_I;
import static net.luxvacuos.voxel.client.input.Keyboard.KEY_LSHIFT;
import static net.luxvacuos.voxel.client.input.Keyboard.KEY_O;
import static net.luxvacuos.voxel.client.input.Keyboard.KEY_S;
import static net.luxvacuos.voxel.client.input.Keyboard.KEY_SPACE;
import static net.luxvacuos.voxel.client.input.Keyboard.KEY_T;
import static net.luxvacuos.voxel.client.input.Keyboard.KEY_W;
import static net.luxvacuos.voxel.client.input.Keyboard.isKeyDown;
import static net.luxvacuos.voxel.client.input.Mouse.getDX;
import static net.luxvacuos.voxel.client.input.Mouse.getDY;
import static net.luxvacuos.voxel.client.input.Mouse.isButtonDown;
import static net.luxvacuos.voxel.client.input.Mouse.setCursorPosition;
import static net.luxvacuos.voxel.client.input.Mouse.setGrabbed;

import net.luxvacuos.igl.vector.Matrix4f;
import net.luxvacuos.igl.vector.Vector2f;
import net.luxvacuos.igl.vector.Vector3f;
import net.luxvacuos.igl.vector.Vector4f;
import net.luxvacuos.voxel.client.core.VoxelVariables;
import net.luxvacuos.voxel.client.input.Keyboard;
import net.luxvacuos.voxel.client.rendering.api.glfw.Display;
import net.luxvacuos.voxel.client.resources.GameResources;
import net.luxvacuos.voxel.client.util.Maths;
import net.luxvacuos.voxel.client.world.IWorld;
import net.luxvacuos.voxel.client.world.block.Block;
import net.luxvacuos.voxel.universal.resources.UniversalResources;

public class PlayerCamera extends Camera {

	private float speed;
	private float multiplierMouse = 14;
	private boolean underWater = false;
	private int mouseSpeed = 2;
	private final int maxLookUp = 90;
	private final int maxLookDown = -90;
	private Vector2f center;
	private int clickTime;

	public PlayerCamera(Matrix4f proj, Display display) {
		super(proj);
		center = new Vector2f(display.getDisplayWidth() / 2, display.getDisplayHeight() / 2);
		this.speed = 3f;
	}

	public void update(float delta, GameResources gm, IWorld world) {
		isMoved = false;
		float mouseDX = getDX() * delta * mouseSpeed * 0.16f * multiplierMouse;
		float mouseDY = getDY() * delta * mouseSpeed * 0.16f * multiplierMouse;
		if (yaw + mouseDX >= 360) {
			yaw = yaw + mouseDX - 360;
		} else if (yaw + mouseDX < 0) {
			yaw = 360 - yaw + mouseDX;
		} else {
			yaw += mouseDX;
		}
		if (pitch - mouseDY >= maxLookDown && pitch - mouseDY <= maxLookUp) {
			pitch += -mouseDY;
		} else if (pitch - mouseDY < maxLookDown) {
			pitch = maxLookDown;
		} else if (pitch - mouseDY > maxLookUp) {
			pitch = maxLookUp;
		}

		Vector3f v = this.getPosition();

		float tempx = (v.x);
		int tempX = (int) tempx;
		if (v.x < 0) {
			tempx = (v.x);
			tempX = (int) tempx - 1;
		}

		float tempz = (v.z);
		int tempZ = (int) tempz;
		if (v.z > 0) {
			tempz = (v.z);
			tempZ = (int) tempz + 1;
		}

		float tempy = (v.y);
		int tempY = (int) tempy - 1;

		int bx = (int) tempX;
		int by = (int) tempY;
		int bz = (int) tempZ;

		if (world.getGlobalBlock(bx, by + 1, bz) == Block.Water.getId())
			underWater = true;
		else
			underWater = false;

		if (isKeyDown(KEY_W)) {
			velocityComponent.velocity.z += -Math.cos(Math.toRadians(yaw)) * speed;
			velocityComponent.velocity.x += Math.sin(Math.toRadians(yaw)) * speed;
			isMoved = true;

		} else if (isKeyDown(KEY_S)) {
			velocityComponent.velocity.z -= -Math.cos(Math.toRadians(yaw)) * speed;
			velocityComponent.velocity.x -= Math.sin(Math.toRadians(yaw)) * speed;
			isMoved = true;
		}

		if (isKeyDown(KEY_D)) {
			velocityComponent.velocity.z += Math.sin(Math.toRadians(yaw)) * speed;
			velocityComponent.velocity.x += Math.cos(Math.toRadians(yaw)) * speed;
			isMoved = true;
		} else if (isKeyDown(KEY_A)) {
			velocityComponent.velocity.z -= Math.sin(Math.toRadians(yaw)) * speed;
			velocityComponent.velocity.x -= Math.cos(Math.toRadians(yaw)) * speed;
			isMoved = true;
		}
		if (isKeyDown(KEY_SPACE) && !jump) {
			velocityComponent.velocity.y = 5;
			jump = true;
		}
		if (velocityComponent.velocity.y == 0)
			jump = false;
		if (isKeyDown(KEY_LSHIFT)) {
			speed = 0.5f;
		} else {
			speed = 3;
		}

		if (isKeyDown(Keyboard.KEY_Y)) {
			System.out.println(positionComponent.toString());
			System.out.println(velocityComponent.toString());
		}

		if (isKeyDown(KEY_T)) {
			gm.getPhysicsEngine()
					.addEntity(new GameEntity(UniversalResources.player,
							new Vector3f(this.getPosition().x, this.getPosition().y + 2, this.getPosition().z),
							velocityComponent.velocity.x, velocityComponent.velocity.y, velocityComponent.velocity.z, 0,
							0, 0, 1));
		}

		if (clickTime > 0)
			clickTime--;

		if (clickTime == 0)
			if (isButtonDown(0)) {
				clickTime = 10;
				setBlock(gm.getDisplay().getDisplayWidth(), gm.getDisplay().getDisplayHeight(), (byte) 0, world, gm);
			} else if (isButtonDown(1)) {
				clickTime = 10;
				setBlock(gm.getDisplay().getDisplayWidth(), gm.getDisplay().getDisplayHeight(), (byte) 9, world, gm);
			}
		if (isKeyDown(Keyboard.KEY_Y))
			world.setGlobalBlock(bx, by - 1, bz, Block.Lava.getId());

		updateDebug(world);
		updateRay(gm.getRenderer().getProjectionMatrix(), gm.getDisplay().getDisplayWidth(),
				gm.getDisplay().getDisplayHeight(), center);
	}

	public void updateDebug(IWorld world) {
		if (isKeyDown(KEY_I)) {
			VoxelVariables.radius++;
		}
		if (isKeyDown(KEY_O)) {
			VoxelVariables.radius--;
		}
	}

	private void setBlock(int ww, int wh, byte block, IWorld world, GameResources gm) {
		Vector4f viewport = new Vector4f(0, 0, ww, wh);
		Vector3f wincoord = new Vector3f(ww / 2, wh / 2, depth);
		Vector3f objcoord = new Vector3f();
		Matrix4f mvp = new Matrix4f();
		Matrix4f.mul(gm.getRenderer().getProjectionMatrix(), Maths.createViewMatrix(this), mvp);
		objcoord = mvp.unproject(wincoord, viewport, objcoord);

		float tempx = (objcoord.x);
		int tempX = (int) tempx;
		if (objcoord.x < 0) {
			tempx = (objcoord.x);
			tempX = (int) tempx - 1;
		}

		float tempz = (objcoord.z);
		int tempZ = (int) tempz;
		if (objcoord.z > 0) {
			tempz = (objcoord.z);
			tempZ = (int) tempz + 1;
		}

		float tempy = (objcoord.y);
		int tempY = (int) tempy;

		int bx = (int) tempX;
		int by = (int) tempY;
		int bz = (int) tempZ;

		if (block == Block.Torch.getId())
			world.lighting(bx, by, bz, 14);
		if (block == Block.Air.getId() && world.getGlobalBlock(bx, by, bz) != Block.Air.getId())
			gm.getPhysicsEngine().addEntity(Block.getBlock(world.getGlobalBlock(bx, by, bz)).getDrop(gm,
					new Vector3f(bx + 0.5f, by + 0.5f, bz - 0.5f)));
		world.setGlobalBlock(bx, by, bz, block);
	}

	public void invertPitch() {
		pitch = -pitch;
	}

	public void setMouse(Display display) {
		setCursorPosition(display.getDisplayWidth() / 2, display.getDisplayHeight() / 2);
		setGrabbed(true);
	}

	public void unlockMouse() {
		setGrabbed(false);
	}

	public boolean isUnderWater() {
		return underWater;
	}

}