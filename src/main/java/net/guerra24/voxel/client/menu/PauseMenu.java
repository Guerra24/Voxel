package net.guerra24.voxel.client.menu;

import net.guerra24.voxel.universal.util.vector.Vector2f;

public class PauseMenu {
	private Button backToMain;

	public PauseMenu() {
		backToMain = new Button(new Vector2f(537, 0), new Vector2f(215, 100));
	}

	public Button getBackToMain() {
		return backToMain;
	}
}