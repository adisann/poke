package com.github.adisann.pokemon.util;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * @author hydrozoa
 */
public class TexturePackerTool {
	
	public static void main(String[] args) {
		TexturePacker.process(
				"graphics_unpacked/ui/", 
				"graphics_packed/ui/", 
				"uipack");
		TexturePacker.process(
				"graphics_unpacked/tiles/", 
				"graphics_packed/tiles/", 
				"tilepack");
		TexturePacker.process(
				"graphics_unpacked/battle/", 
				"graphics_packed/battle/", 
				"battlepack");
	}

}


