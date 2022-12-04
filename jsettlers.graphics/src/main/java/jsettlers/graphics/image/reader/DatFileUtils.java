/*
 * Copyright (c) 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package jsettlers.graphics.image.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class DatFileUtils {

	public static String generateOriginalVersionId(File gfxDirectory) {
		File[] gfxDatFiles = gfxDirectory.listFiles();
		List<File> distinctGfxDatFiles = distinctFileNames(gfxDatFiles);

		// F-1 because we dont know the dat file index
		List<Long> list = new ArrayList<>();
		for (File file : distinctGfxDatFiles) {
			if (file.getName().toLowerCase().endsWith(".dat")) {
				AdvancedDatFileReader reader = new AdvancedDatFileReader(file, DatFileType.getForPath(file), "F-1");

				list.addAll(reader.getSettlersHashes().getHashes());
				list.addAll(reader.getGuiHashes().getHashes());
			}
		}
		Hashes hashes = new Hashes(list);

		return Long.toString(hashes.hash());
	}

	public static List<File> distinctFileNames(File[] files) {
		Arrays.sort(files, Comparator.comparing(file -> file.getName().toUpperCase()));
		LinkedList<File> distinct = new LinkedList<>();
		for (File file : files) {
			if (distinct.isEmpty() || !getDatFileName(distinct.getLast()).equalsIgnoreCase(getDatFileName(file))) {
				distinct.add(file);
			}
		}
		return distinct;
	}

	public static String getDatFileName(File file) {
		return file.getName().split("\\.")[0].toLowerCase();
	}

	public static int getDatFileIndex(File datFile) {
		return Integer.valueOf(getDatFileName(datFile).split("_")[1]);
	}
}
