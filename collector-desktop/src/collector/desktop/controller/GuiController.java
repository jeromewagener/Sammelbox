package collector.desktop.controller;

import collector.desktop.model.GuiState;

public class GuiController {
	private static GuiState guiState = new GuiState();
	
	public static GuiState getGuiState() {
		return guiState;
	}
	
	public static void setGuiState(GuiState guiState) {
		GuiController.guiState = guiState;
	}
}
