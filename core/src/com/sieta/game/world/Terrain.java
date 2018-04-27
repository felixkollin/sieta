package com.sieta.game.world;

import com.sieta.game.utils.FastNoise;

import com.sieta.game.utils.FastNoise.FractalType;
import com.sieta.game.utils.FastNoise.Interp;
import com.sieta.game.utils.FastNoise.NoiseType;

public class Terrain {
	private static FastNoise caveNoise;
	private static FastNoise heightMap;
	private static FastNoise hills;
	private static FastNoise uneven;
		
	static{
		int seed = (int)Math.random() * 1000;
		caveNoise = new FastNoise(seed);
		caveNoise.SetNoiseType(NoiseType.SimplexFractal);
		caveNoise.SetFrequency(0.12f);
		caveNoise.SetFractalOctaves(5);
		caveNoise.SetFractalType(FractalType.Billow);
		caveNoise.SetInterp(Interp.Linear);
		
		heightMap = new FastNoise(seed);
		heightMap.SetNoiseType(NoiseType.Simplex);
		heightMap.SetFractalOctaves(10);
		heightMap.SetFrequency(0.05f);
		heightMap.SetInterp(Interp.Hermite);
		
		hills = new FastNoise(seed);
		hills.SetNoiseType(NoiseType.Simplex);
		hills.SetFractalOctaves(3);
		hills.SetFrequency(0.01f);
		hills.SetInterp(Interp.Hermite);
		
		uneven = new FastNoise(seed);
		uneven.SetNoiseType(NoiseType.Simplex);
		uneven.SetFractalOctaves(5);
		uneven.SetFrequency(0.15f);
		uneven.SetInterp(Interp.Hermite);
	}
	
	public static double getHeightValue(int worldX){
		return heightMap.GetValue(worldX, 0) * 10 + hills.GetValue(worldX + 100000, 0) * 20 + uneven.GetValue(worldX, 0) * 3;
	}
	
	public static double getCaveValue(int worldX, int worldY){
		return caveNoise.GetValue(worldX, worldY * 1.5f);
	}
}

