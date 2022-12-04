package jsettlers;

import jsettlers.common.material.EMaterialType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.BiConsumer;

import static jsettlers.common.material.EMaterialType.*;

public class ProductionStatisticsConverter {

	private static void readAll(EMaterialType material, String[] args) throws IOException {
		Arrays.fill(RESOURCES, 0);
		readResources(args[0], material);

		int sum = 0;
		for(int i = 0; i != STEP; i++) {
			int preSum = sum + RESOURCES[i];
			RESOURCES[i] += sum;

			sum = preSum;
		}

		write("productionStats/" + material + "-" + args[2] + ".log");


		Arrays.fill(RESOURCES, 0);

		readResources(args[1], material);


		sum = 0;
		for(int i = 0; i != STEP; i++) {
			int preSum = sum + RESOURCES[i];
			RESOURCES[i] += sum;

			sum = preSum;
		}

		write("productionStats/" + material + "-" + args[3] + ".log");
	}

	private static void write(String name) throws FileNotFoundException {
		PrintStream out = new PrintStream(name);

		for(int i = 0; i != STEP; i++) {
			out.println(i + "," + RESOURCES[i]);
		}

		out.flush();
	}

	private static void readResources(String name, EMaterialType material) throws IOException{
		read(name, (time, mat) -> {
			if(mat != material) return;


			int minute = time/100/60;
			RESOURCES[minute]++;
		});

	}

	private static void read(String name, BiConsumer<Integer, EMaterialType> readFunc) throws IOException {
		try(BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(name)))) {
			String line;
			while ((line = read.readLine()) != null) {

				String[] split = line.split(",");

				int time = Integer.parseInt(split[0]);
				EMaterialType mat = EMaterialType.valueOf(split[1]);

				readFunc.accept(time, mat);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		STEP = Integer.parseInt(args[4])*10;
		RESOURCES = new int[STEP + 10];

		for(EMaterialType mat : EMaterialType.values()) {
			if(!mat.isDroppable() && mat != NO_MATERIAL) {
				continue;
			}

			readAll(mat, args);
		}
	}

	private static int STEP;

	private static int[] RESOURCES;
}
