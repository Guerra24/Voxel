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

package net.guerra24.voxel.client.world.block;

import net.guerra24.voxel.client.core.VoxelVariables;
import net.guerra24.voxel.client.resources.Loader;
import net.guerra24.voxel.client.resources.models.ModelTexture;
import net.guerra24.voxel.client.resources.models.RawModel;
import net.guerra24.voxel.client.resources.models.TexturedModel;

public class BlocksResources {
	public static TexturedModel cubeIndesUP;
	public static TexturedModel cubeGrassUP;
	public static TexturedModel cubeStoneUP;
	public static TexturedModel cubeSandUP;
	public static TexturedModel cubeGlassUP;
	public static TexturedModel cubeDirtUP;
	public static TexturedModel cubeDiamondOreUP;
	public static TexturedModel cubeGoldOreUP;
	public static TexturedModel cubeWoodUP;
	public static TexturedModel cubeIceUP;

	public static TexturedModel cubeIndesDOWN;
	public static TexturedModel cubeGrassDOWN;
	public static TexturedModel cubeStoneDOWN;
	public static TexturedModel cubeSandDOWN;
	public static TexturedModel cubeGlassDOWN;
	public static TexturedModel cubeDirtDOWN;
	public static TexturedModel cubeDiamondOreDOWN;
	public static TexturedModel cubeGoldOreDOWN;
	public static TexturedModel cubeWoodDOWN;
	public static TexturedModel cubeIceDOWN;

	public static TexturedModel cubeIndesEAST;
	public static TexturedModel cubeGrassEAST;
	public static TexturedModel cubeStoneEAST;
	public static TexturedModel cubeSandEAST;
	public static TexturedModel cubeGlassEAST;
	public static TexturedModel cubeDirtEAST;
	public static TexturedModel cubeDiamondOreEAST;
	public static TexturedModel cubeGoldOreEAST;
	public static TexturedModel cubeWoodEAST;
	public static TexturedModel cubeIceEAST;

	public static TexturedModel cubeIndesWEST;
	public static TexturedModel cubeGrassWEST;
	public static TexturedModel cubeStoneWEST;
	public static TexturedModel cubeSandWEST;
	public static TexturedModel cubeGlassWEST;
	public static TexturedModel cubeDirtWEST;
	public static TexturedModel cubeDiamondOreWEST;
	public static TexturedModel cubeGoldOreWEST;
	public static TexturedModel cubeWoodWEST;
	public static TexturedModel cubeIceWEST;

	public static TexturedModel cubeIndesNORTH;
	public static TexturedModel cubeGrassNORTH;
	public static TexturedModel cubeStoneNORTH;
	public static TexturedModel cubeSandNORTH;
	public static TexturedModel cubeGlassNORTH;
	public static TexturedModel cubeDirtNORTH;
	public static TexturedModel cubeDiamondOreNORTH;
	public static TexturedModel cubeGoldOreNORTH;
	public static TexturedModel cubeWoodNORTH;
	public static TexturedModel cubeIceNORTH;

	public static TexturedModel cubeIndesSOUTH;
	public static TexturedModel cubeGrassSOUTH;
	public static TexturedModel cubeStoneSOUTH;
	public static TexturedModel cubeSandSOUTH;
	public static TexturedModel cubeGlassSOUTH;
	public static TexturedModel cubeDirtSOUTH;
	public static TexturedModel cubeDiamondOreSOUTH;
	public static TexturedModel cubeGoldOreSOUTH;
	public static TexturedModel cubeWoodSOUTH;
	public static TexturedModel cubeIceSOUTH;

	public static TexturedModel cubeTorch;
	public static TexturedModel cubePortal;
	public static TexturedModel cubeLeaves;

	public static void createBlocks(Loader loader) {

		RawModel up = loader.getObjLoader().loadObjModel("FACE_UP", loader);
		RawModel down = loader.getObjLoader().loadObjModel("FACE_DOWN", loader);
		RawModel east = loader.getObjLoader().loadObjModel("FACE_EAST", loader);
		RawModel west = loader.getObjLoader().loadObjModel("FACE_WEST", loader);
		RawModel nort = loader.getObjLoader().loadObjModel("FACE_NORTH", loader);
		RawModel south = loader.getObjLoader().loadObjModel("FACE_SOUTH", loader);
		RawModel torch = loader.getObjLoader().loadObjModel("Torch", loader);
		RawModel portal = loader.getObjLoader().loadObjModel("Portal", loader);
		RawModel leaves = loader.getObjLoader().loadObjModel("Leaves", loader);

		ModelTexture texture7 = null;
		ModelTexture texture = null;
		ModelTexture texture2 = null;
		ModelTexture texture10 = null;

		if (VoxelVariables.christmas) {
			texture7 = new ModelTexture(loader.loadTextureBlocks("GrassSideSnow"));
			texture = new ModelTexture(loader.loadTextureBlocks("GrassSnow"));
			texture2 = new ModelTexture(loader.loadTextureBlocks("SandSnow"));
			texture10 = new ModelTexture(loader.loadTextureBlocks("LeavesSnow"));
		} else {
			texture7 = new ModelTexture(loader.loadTextureBlocks("GrassSide"));
			texture = new ModelTexture(loader.loadTextureBlocks("Grass"));
			texture2 = new ModelTexture(loader.loadTextureBlocks("Sand"));
			texture10 = new ModelTexture(loader.loadTextureBlocks("Leaves"));
		}

		ModelTexture texture0 = new ModelTexture(loader.loadTextureBlocks("Indes"));
		ModelTexture texture1 = new ModelTexture(loader.loadTextureBlocks("Stone"));
		ModelTexture texture3 = new ModelTexture(loader.loadTextureBlocks("Glass"));
		ModelTexture texture4 = new ModelTexture(loader.loadTextureBlocks("Dirt"));
		ModelTexture texture5 = new ModelTexture(loader.loadTextureBlocks("Diamond-Ore"));
		ModelTexture texture6 = new ModelTexture(loader.loadTextureBlocks("Gold-Ore"));
		ModelTexture texture8 = new ModelTexture(loader.loadTextureBlocks("Torch"));
		ModelTexture texture9 = new ModelTexture(loader.loadTextureBlocks("Portal"));
		ModelTexture texture11 = new ModelTexture(loader.loadTextureBlocks("Wood"));
		ModelTexture texture12 = new ModelTexture(loader.loadTextureBlocks("Ice"));

		cubeIndesUP = new TexturedModel(up, texture0);
		cubeGrassUP = new TexturedModel(up, texture);
		cubeStoneUP = new TexturedModel(up, texture1);
		cubeSandUP = new TexturedModel(up, texture2);
		cubeGlassUP = new TexturedModel(up, texture3);
		cubeDirtUP = new TexturedModel(up, texture4);
		cubeDiamondOreUP = new TexturedModel(up, texture5);
		cubeGoldOreUP = new TexturedModel(up, texture6);
		cubeWoodUP = new TexturedModel(up, texture11);
		cubeIceUP = new TexturedModel(up, texture12);

		cubeIndesDOWN = new TexturedModel(down, texture0);
		cubeGrassDOWN = new TexturedModel(down, texture4);
		cubeStoneDOWN = new TexturedModel(down, texture1);
		cubeSandDOWN = new TexturedModel(down, texture2);
		cubeGlassDOWN = new TexturedModel(down, texture3);
		cubeDirtDOWN = new TexturedModel(down, texture4);
		cubeDiamondOreDOWN = new TexturedModel(down, texture5);
		cubeGoldOreDOWN = new TexturedModel(down, texture6);
		cubeWoodDOWN = new TexturedModel(down, texture11);
		cubeIceDOWN = new TexturedModel(down, texture12);

		cubeIndesEAST = new TexturedModel(east, texture0);
		cubeGrassEAST = new TexturedModel(east, texture7);
		cubeStoneEAST = new TexturedModel(east, texture1);
		cubeSandEAST = new TexturedModel(east, texture2);
		cubeGlassEAST = new TexturedModel(east, texture3);
		cubeDirtEAST = new TexturedModel(east, texture4);
		cubeDiamondOreEAST = new TexturedModel(east, texture5);
		cubeGoldOreEAST = new TexturedModel(east, texture6);
		cubeWoodEAST = new TexturedModel(east, texture11);
		cubeIceEAST = new TexturedModel(east, texture12);

		cubeIndesWEST = new TexturedModel(west, texture0);
		cubeGrassWEST = new TexturedModel(west, texture7);
		cubeStoneWEST = new TexturedModel(west, texture1);
		cubeSandWEST = new TexturedModel(west, texture2);
		cubeGlassWEST = new TexturedModel(west, texture3);
		cubeDirtWEST = new TexturedModel(west, texture4);
		cubeDiamondOreWEST = new TexturedModel(west, texture5);
		cubeGoldOreWEST = new TexturedModel(west, texture6);
		cubeWoodWEST = new TexturedModel(west, texture11);
		cubeIceWEST = new TexturedModel(west, texture12);

		cubeIndesNORTH = new TexturedModel(nort, texture0);
		cubeGrassNORTH = new TexturedModel(nort, texture7);
		cubeStoneNORTH = new TexturedModel(nort, texture1);
		cubeSandNORTH = new TexturedModel(nort, texture2);
		cubeGlassNORTH = new TexturedModel(nort, texture3);
		cubeDirtNORTH = new TexturedModel(nort, texture4);
		cubeDiamondOreNORTH = new TexturedModel(nort, texture5);
		cubeGoldOreNORTH = new TexturedModel(nort, texture6);
		cubeWoodNORTH = new TexturedModel(nort, texture11);
		cubeIceNORTH = new TexturedModel(nort, texture12);

		cubeIndesSOUTH = new TexturedModel(south, texture0);
		cubeGrassSOUTH = new TexturedModel(south, texture7);
		cubeStoneSOUTH = new TexturedModel(south, texture1);
		cubeSandSOUTH = new TexturedModel(south, texture2);
		cubeGlassSOUTH = new TexturedModel(south, texture3);
		cubeDirtSOUTH = new TexturedModel(south, texture4);
		cubeDiamondOreSOUTH = new TexturedModel(south, texture5);
		cubeGoldOreSOUTH = new TexturedModel(south, texture6);
		cubeWoodSOUTH = new TexturedModel(south, texture11);
		cubeIceSOUTH = new TexturedModel(south, texture12);

		cubeTorch = new TexturedModel(torch, texture8);
		cubePortal = new TexturedModel(portal, texture9);
		cubeLeaves = new TexturedModel(leaves, texture10);
	}

}