/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016 Guerra24
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

package net.guerra24.voxel.client.resources;

import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;

import java.util.Random;

import com.badlogic.ashley.core.Engine;
import com.esotericsoftware.kryo.Kryo;

import net.guerra24.voxel.client.core.GameSettings;
import net.guerra24.voxel.client.core.GlobalStates;
import net.guerra24.voxel.client.core.Voxel;
import net.guerra24.voxel.client.core.VoxelVariables;
import net.guerra24.voxel.client.graphics.DeferredShadingRenderer;
import net.guerra24.voxel.client.graphics.Frustum;
import net.guerra24.voxel.client.graphics.MasterRenderer;
import net.guerra24.voxel.client.graphics.MasterShadowRenderer;
import net.guerra24.voxel.client.graphics.OcclusionRenderer;
import net.guerra24.voxel.client.graphics.SkyboxRenderer;
import net.guerra24.voxel.client.graphics.VectorsRendering;
import net.guerra24.voxel.client.graphics.nanovg.Timers;
import net.guerra24.voxel.client.graphics.opengl.ContextFormat;
import net.guerra24.voxel.client.graphics.opengl.Display;
import net.guerra24.voxel.client.graphics.shaders.ShaderProgram;
import net.guerra24.voxel.client.graphics.shaders.TessellatorBasicShader;
import net.guerra24.voxel.client.graphics.shaders.TessellatorShader;
import net.guerra24.voxel.client.input.Keyboard;
import net.guerra24.voxel.client.input.Mouse;
import net.guerra24.voxel.client.menu.Menu;
import net.guerra24.voxel.client.network.VoxelClient;
import net.guerra24.voxel.client.particle.ParticleMaster;
import net.guerra24.voxel.client.particle.ParticleTexture;
import net.guerra24.voxel.client.sound.LibraryLWJGLOpenAL;
import net.guerra24.voxel.client.sound.soundsystem.SoundSystem;
import net.guerra24.voxel.client.sound.soundsystem.SoundSystemConfig;
import net.guerra24.voxel.client.sound.soundsystem.SoundSystemException;
import net.guerra24.voxel.client.sound.soundsystem.codecs.CodecJOgg;
import net.guerra24.voxel.client.util.CustomLog;
import net.guerra24.voxel.client.util.Logger;
import net.guerra24.voxel.client.util.LoggerSoundSystem;
import net.guerra24.voxel.client.world.block.Block;
import net.guerra24.voxel.client.world.entities.Camera;
import net.guerra24.voxel.client.world.entities.PlayerCamera;
import net.guerra24.voxel.client.world.entities.SunCamera;
import net.guerra24.voxel.client.world.physics.PhysicsSystem;
import net.guerra24.voxel.universal.resources.UniversalResources;
import net.guerra24.voxel.universal.util.vector.Vector3f;

/**
 * Game Resources
 * 
 * @author Guerra24 <pablo230699@hotmail.com>
 * @category Assets
 */
public class GameResources {

	private static GameResources instance = null;

	public static GameResources instance() {
		if (instance == null)
			instance = new GameResources();
		return instance;
	}

	/**
	 * GameResources Data
	 */
	private Display display;
	private Random rand;
	private Loader loader;
	private Camera camera;
	private Camera sun_Camera;
	private MasterRenderer renderer;
	private SkyboxRenderer skyboxRenderer;
	private GlobalStates globalStates;
	private DeferredShadingRenderer deferredShadingRenderer;
	private MasterShadowRenderer masterShadowRenderer;
	private OcclusionRenderer occlusionRenderer;

	private Engine physicsEngine;
	private PhysicsSystem physicsSystem;
	private VoxelClient voxelClient;

	private SoundSystem soundSystem;
	private Frustum frustum;
	private Kryo kryo;
	private Menu menuSystem;
	private GameSettings gameSettings;

	private Vector3f sunRotation = new Vector3f(5, 0, -45);
	private Vector3f lightPos = new Vector3f(0, 0, 0);
	private Vector3f invertedLightPosition = new Vector3f(0, 0, 0);
	private ParticleTexture torchTexture;

	/**
	 * Constructor
	 * 
	 */
	private GameResources() {
		gameSettings = new GameSettings();
		display = new Display();
		display.create(VoxelVariables.WIDTH, VoxelVariables.HEIGHT, "Voxel", VoxelVariables.VSYNC, false, false,
				new ContextFormat(3, 3, GLFW_OPENGL_API, GLFW_OPENGL_CORE_PROFILE, true),
				new String[] { "assets/icon/icon32.png", "assets/icon/icon64.png" });
		Keyboard.setDisplay(display);
		Mouse.setDisplay(display);
		VectorsRendering.setDisplay(display);
		Timers.setDisplay(display);
		ShaderProgram.setDisplay(display);
	}

	/**
	 * Initialize the Game Objects
	 * 
	 */
	public void init(Voxel voxel) {
		loader = new Loader(display);
		rand = new Random();
		masterShadowRenderer = new MasterShadowRenderer(display);
		renderer = new MasterRenderer(this);
		sun_Camera = new SunCamera(masterShadowRenderer.getProjectionMatrix());
		sun_Camera.setPosition(new Vector3f(0, 0, 0));
		sun_Camera.setYaw(sunRotation.x);
		sun_Camera.setPitch(sunRotation.y);
		sun_Camera.setRoll(sunRotation.z);
		camera = new PlayerCamera(renderer.getProjectionMatrix(), display);
		kryo = new Kryo();
		occlusionRenderer = new OcclusionRenderer(renderer.getProjectionMatrix());
		skyboxRenderer = new SkyboxRenderer(loader, renderer.getProjectionMatrix());
		deferredShadingRenderer = new DeferredShadingRenderer(this);
		TessellatorShader.getInstance();
		TessellatorBasicShader.getInstance();
		ParticleMaster.getInstance().init(loader, renderer.getProjectionMatrix());
		frustum = new Frustum();

		physicsEngine = new Engine();
		physicsSystem = new PhysicsSystem(voxel.getWorldsHandler().getActiveWorld());
		physicsEngine.addSystem(physicsSystem);
		physicsEngine.addEntity(camera);
		CustomLog.getInstance();
		voxelClient = new VoxelClient(this);

		try {
			SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
			SoundSystemConfig.setCodec("ogg", CodecJOgg.class);
			SoundSystemConfig.setSoundFilesPackage("assets/sounds/");
			SoundSystemConfig.setLogger(new LoggerSoundSystem());
		} catch (SoundSystemException e) {
			Logger.error("Unable to setting up Sound System Configuration");
			e.printStackTrace();
			System.exit(-1);
		}
		soundSystem = new SoundSystem();
		globalStates = new GlobalStates();
		Block.initBasicBlocks();
		UniversalResources.loadUniversalResources(this);
		menuSystem = new Menu(this);
	}

	/**
	 * Load Resources
	 * 
	 */
	public void loadResources() {
		soundSystem.backgroundMusic("menu1", "menu/menu1.ogg", false);
		soundSystem.backgroundMusic("menu2", "menu/menu2.ogg", false);
		loader.loadNVGFont("Roboto-Bold", "Roboto-Bold");
		loader.loadNVGFont("Roboto-Regular", "Roboto-Regular");
		loader.loadNVGFont("Entypo", "Entypo", 40);
		torchTexture = new ParticleTexture(loader.loadTextureParticle("fire0"), 4);
	}

	public void update(float rot) {
		sunRotation.setY(rot);
		sun_Camera.setYaw(sunRotation.x);
		sun_Camera.setPitch(sunRotation.y);
		sun_Camera.setRoll(sunRotation.z);
		((SunCamera) sun_Camera).updateShadowRay(this, false);
		lightPos = new Vector3f(sun_Camera.getRay().direction.x * 1000, sun_Camera.getRay().direction.y * 1000,
				sun_Camera.getRay().direction.z * 1000);
		Vector3f.add(sun_Camera.getPosition(), lightPos, lightPos);

		((SunCamera) sun_Camera).updateShadowRay(this, true);
		invertedLightPosition = new Vector3f(sun_Camera.getRay().direction.x * 1000,
				sun_Camera.getRay().direction.y * 1000, sun_Camera.getRay().direction.z * 1000);
		Vector3f.add(sun_Camera.getPosition(), invertedLightPosition, invertedLightPosition);
	}

	public void reload(Voxel voxel) {
		cleanUp();
		init(voxel);
		loadResources();
	}

	/**
	 * Disposes all objects
	 * 
	 */
	public void cleanUp() {
		gameSettings.save();
		TessellatorShader.getInstance().cleanUp();
		TessellatorBasicShader.getInstance().cleanUp();
		masterShadowRenderer.cleanUp();
		occlusionRenderer.cleanUp();
		ParticleMaster.getInstance().cleanUp();
		deferredShadingRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		soundSystem.cleanup();
		voxelClient.dispose();
	}

	public Random getRand() {
		return rand;
	}

	public Loader getLoader() {
		return loader;
	}

	public Kryo getKryo() {
		return kryo;
	}

	public Camera getCamera() {
		return camera;
	}

	public MasterRenderer getRenderer() {
		return renderer;
	}

	public SkyboxRenderer getSkyboxRenderer() {
		return skyboxRenderer;
	}

	public SoundSystem getSoundSystem() {
		return soundSystem;
	}

	public DeferredShadingRenderer getDeferredShadingRenderer() {
		return deferredShadingRenderer;
	}

	public Frustum getFrustum() {
		return frustum;
	}

	public GlobalStates getGlobalStates() {
		return globalStates;
	}

	public MasterShadowRenderer getMasterShadowRenderer() {
		return masterShadowRenderer;
	}

	public Camera getSun_Camera() {
		return sun_Camera;
	}

	public Vector3f getLightPos() {
		return lightPos;
	}

	public Vector3f getInvertedLightPosition() {
		return invertedLightPosition;
	}

	public GameSettings getGameSettings() {
		return gameSettings;
	}

	public OcclusionRenderer getOcclusionRenderer() {
		return occlusionRenderer;
	}

	public Menu getMenuSystem() {
		return menuSystem;
	}

	public ParticleTexture getTorchTexture() {
		return torchTexture;
	}

	public Vector3f getSunRotation() {
		return sunRotation;
	}

	public Engine getPhysicsEngine() {
		return physicsEngine;
	}

	public Display getDisplay() {
		return display;
	}

	public VoxelClient getVoxelClient() {
		return voxelClient;
	}

}