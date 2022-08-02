/*
 * Copyright (c) 2015 - 2018
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
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.image.Image;
import jsettlers.graphics.image.NullImage;
import jsettlers.graphics.image.SettlerImage;
import jsettlers.graphics.image.reader.bytereader.ByteReader;
import jsettlers.graphics.image.reader.shadowmap.IdentityShadowMapping;
import jsettlers.graphics.image.reader.shadowmap.ShadowMapping;
import jsettlers.graphics.image.reader.translator.DatBitmapTranslator;
import jsettlers.graphics.image.reader.translator.GuiTranslator;
import jsettlers.graphics.image.reader.translator.LandscapeTranslator;
import jsettlers.graphics.image.reader.translator.SettlerTranslator;
import jsettlers.graphics.image.reader.translator.ShadowTranslator;
import jsettlers.graphics.image.reader.translator.TorsoTranslator;
import jsettlers.graphics.image.reader.versions.DefaultGfxFolderMapping.DefaultDatFileMapping;
import jsettlers.graphics.image.sequence.ArraySequence;
import jsettlers.graphics.image.sequence.Sequence;
import jsettlers.graphics.image.sequence.SequenceList;

import static jsettlers.graphics.image.reader.versions.GfxFolderMapping.DatFileMapping;

/**
 * This is an advanced dat file reader. It can read the file, but it only reads needed sequences.
 * <p>
 * The format of a dat file is (all numbers in little endian):
 * <table>
 * <tr>
 * <td>Bytes 0..47:</td>
 * <td>Always the same</td>
 * </tr>
 * <tr>
 * <td>Bytes 48 .. 51:</td>
 * <td>file size</td>
 * </tr>
 * <tr>
 * <td>Bytes 52 .. 55:</td>
 * <td>Unknown Pointer, seems not to be a sequence.</td>
 * </tr>
 * <tr>
 * <td>Bytes 56 .. 59:</td>
 * <td>Start position of landscape sequence pointers.</td>
 * </tr>
 * <tr>
 * <td>Bytes 60 .. 63:</td>
 * <td>Unneeded Pointer</td>
 * </tr>
 * <tr>
 * <td>Bytes 64 .. 67:</td>
 * <td>Settler/Building/.. pointers</td>
 * </tr>
 * <tr>
 * <td>Bytes 68 .. 71:</td>
 * <td>Torso pointers</td>
 * </tr>
 * <tr>
 * <td>Bytes 72 .. 75:</td>
 * <td>Position after above</td>
 * </tr>
 * <tr>
 * <td>Bytes 76 .. 79:</td>
 * <td>Position after above</td>
 * </tr>
 * <tr>
 * <td>Bytes 80 .. 83:</td>
 * <td>Something, seems to be like 52..55</td>
 * </tr>
 * <tr>
 * <td>Bytes 84 .. 87:</td>
 * <td>{04 19 00 00}</td>
 * </tr>
 * <tr>
 * <td>Bytes 88 .. 91:</td>
 * <td>{0c 00 00 00}</td>
 * </tr>
 * <tr>
 * <td>Bytes 92 .. 95:</td>
 * <td>{00 00 00 00}</td>
 * </tr>
 * <tr>
 * <td>e.g. Bytes 102 .. 103:</td>
 * <td>Image count of image sequences for one type</td>
 * </tr>
 * <tr>
 * <td>e.g. Bytes 104 .. 107:</td>
 * <td>Start position of fist image sequence list.</td>
 * </tr>
 * </table>
 *
 * @author michael
 */
public class AdvancedDatFileReader implements DatFileReader {
	/**
	 * Every dat file seems to have to start with this sequence.
	 */
	private static final byte[] FILE_START1 = {
		0x04,
		0x13,
		0x04,
		0x00,
		0x0c,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00,
		0x54,
		0x00,
		0x00,
		0x00,
		0x20,
		0x00,
		0x00,
		0x00,
		0x40,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00,
		0x10,
		0x00,
		0x00,
		0x00,
		0x00,
		};
	private static final byte[] FILE_START2 = {
		0x00,
		0x00,
		0x1f,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00
	};

	private static final byte[] FILE_HEADER_END = {
		0x04,
		0x19,
		0x00,
		0x00,
		0x0c,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00,
		0x00
	};

	private static final int SEQUENCE_TYPE_COUNT = 6;
	private static final int ID_SETTLERS         = 0x106;
	private static final int ID_TORSOS           = 0x3112;
	private static final int ID_LANDSCAPE        = 0x2412;
	private static final int ID_SHADOWS          = 0x5982;
	// fullscreen images
	private static final int ID_GUIS             = 0x11306;

	private final DatBitmapTranslator<SettlerImage>   settlerTranslator;
	private final DatBitmapTranslator<SingleImage>     torsoTranslator;
	private final DatBitmapTranslator<SingleImage> landscapeTranslator;
	private final DatBitmapTranslator<SingleImage>    shadowTranslator;
	private final DatBitmapTranslator<SingleImage>       guiTranslator;

	private final DatFileMapping mapping;
	private final ShadowMapping shadowMapping;

	private       ByteReader reader = null;
	private final File       file;

	/**
	 * This is a list of file positions where the settler sequences start.
	 */
	private int[] settlerStarts;

	/**
	 * A list of loaded settler sequences.
	 */
	private Sequence<Image>[] settlerSequences = null;
	/**
	 * An array with the same length as settlers.
	 */
	private int[]             torsoStarts;
	/**
	 * An array with the same length as settlers.
	 */
	private int[]             shadowStarts;

	/**
	 * A list of loaded landscae images.
	 */
	private       SingleImage[]         landscapeImages   = null;
	private final Sequence<SingleImage> landscapeSequence = new LandscapeImageSequence();
	private       int[]                    landscapeStarts;

	private       SingleImage[]         guiImages   = null;
	private       int[]              guiStarts;
	private final Sequence<SingleImage> guiSequence = new GuiImageSequence();

	private final SequenceList<Image> directSettlerList;

	private static final byte[] START = new byte[]{
		0x02,
		0x14,
		0x00,
		0x00,
		0x08,
		0x00,
		0x00
	};

	private final DatFileType type;
	private final String file_name;

	public AdvancedDatFileReader(File file, DatFileType type, String file_name) {
		this(file, type, new DefaultDatFileMapping(), new IdentityShadowMapping(), file_name);
	}

	public AdvancedDatFileReader(File file, DatFileType type, DatFileMapping mapping, ShadowMapping shadowMapping, String file_name) {
		this.file = file;
		this.type = type;
		this.mapping = mapping;
		this.shadowMapping = shadowMapping;
		this.file_name = file_name;

		directSettlerList = new DirectSettlerSequenceList();
		settlerTranslator = new SettlerTranslator(type);
		torsoTranslator = new TorsoTranslator();
		landscapeTranslator = new LandscapeTranslator(type);
		shadowTranslator = new ShadowTranslator();
		guiTranslator = new GuiTranslator(type);
	}

	public Hashes getSettlersHashes() {
		SequenceList<Image> settlers = getSettlers();

		return new Hashes(IntStream.range(0, settlers.size())
				.mapToObj(settlers::get)
				.map(sequence -> sequence.getImage(0, null))
				.filter(image -> image instanceof SingleImage)
				.map(image -> (SingleImage) image)
				.map(SingleImage::hash)
				.collect(Collectors.toList()));
	}

	public Hashes getGuiHashes() {
		Sequence<SingleImage> sequence = getGuis();

		return new Hashes(IntStream.range(0, sequence.length())
				.mapToObj(index -> sequence.getImage(index, null))
				.map(SingleImage::hash)
				.collect(Collectors.toList()));
	}

	/**
	 * Initializes the reader, reads the index.
	 */
	@SuppressWarnings("unchecked")
	public void initialize() {
		try {
			reader = new ByteReader(new RandomAccessFile(file, "r"));
			initFromReader(file, reader);

		} catch (IOException e) {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) { /* nothing to do */ }
				reader = null;
			}
			System.out.println("Could not read dat file " + file + " due to: " + e.getMessage());
		}
		initializeNullFile();

		landscapeImages = new SingleImage[landscapeStarts.length];

		guiImages = new SingleImage[guiStarts.length];
		settlerSequences = new Sequence[settlerStarts.length];

		int torsoDifference = settlerStarts.length - torsoStarts.length;
		if (torsoDifference > 0) {
			int[] oldTorsos = torsoStarts;
			torsoStarts = new int[settlerStarts.length];
			System.arraycopy(oldTorsos, 0, torsoStarts, torsoDifference, oldTorsos.length);
			for (int i = 0; i < torsoDifference; i++) {
				torsoStarts[i] = -1;
			}
		}

		int shadowDifference = settlerStarts.length - shadowStarts.length;
		int i;
		if (shadowDifference > 0) {
			int[] oldShadows = shadowStarts;
			shadowStarts = new int[settlerStarts.length];
			if (shadowDifference == 8 || shadowDifference == 7) {
				// push shadows to end of settler images
				for (i = 0; i < shadowDifference; i++) {
					shadowStarts[i] = -1;
				}
				for (; i < settlerStarts.length; i++) {
					shadowStarts[i] = oldShadows[i - shadowDifference];
				}
			} else {
				// push shadows to beginning of settler images
				for (i = 0; i < oldShadows.length; i++) {
					shadowStarts[i] = oldShadows[i];
				}
				for (; i < settlerStarts.length; i++) {
					shadowStarts[i] = -1;
				}
			}
			if (shadowDifference == 26) { // change shadows in file 13:
				for (i = 0; i < 27; i++) {
					shadowStarts[i] = shadowStarts[i + 3];
				}
				for (i = 27; i < 36; i++) {
					shadowStarts[i] = shadowStarts[i + 2];
				}
				shadowStarts[28] = -1; // market place gets no shadow (has it already)
				shadowStarts[44] = shadowStarts[38]; // dock
				shadowStarts[45] = shadowStarts[39]; // harbour
				for (i = 36; i < 44; i++) {
					shadowStarts[i] = -1; // rest has no shadow
				}
				for (i = 46; i < shadowStarts.length; i++) {
					shadowStarts[i] = -1; // rest has no shadow
				}
			} else if (shadowDifference == 28) { // change shadows in file 36:
				shadowStarts[4] = shadowStarts[1]; // roman ferry
				shadowStarts[6] = -1; // roman ferry front has no extra shadow
				shadowStarts[2] = -1; // roman cargo ship front has no extra shadow
			}
		} else if (shadowDifference == 0) {
			if (settlerStarts.length == 239) { // change shadows in file 11:
				for (i = 171; i >= 13; i--) {
					shadowStarts[i] = shadowStarts[i - 13]; // several specialists
				}
			}
		}
	}

	private void initFromReader(File file, ByteReader reader) throws IOException {
		int[] sequenceIndexStarts = readSequenceIndexStarts(file.length(), reader);

		for (int i = 0; i < SEQUENCE_TYPE_COUNT; i++) {
			try {
				readSequencesAt(reader, sequenceIndexStarts[i]);
			} catch (IOException e) {
				System.err.println("Error while loading sequence" + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private int[] readSequenceIndexStarts(long fileLength, ByteReader reader) throws IOException {
		reader.assumeToRead(FILE_START1);
		reader.assumeToRead(type.getFileStartMagic());
		reader.assumeToRead(FILE_START2);
		int fileSize = reader.read32();

		if (fileSize != fileLength) {
			throw new IOException(
				"The length stored in the dat file is not the file length.");
		}

		// ignore unknown bytes.
		reader.read32();

		// read settler image pointer
		int[] sequenceIndexStarts = new int[SEQUENCE_TYPE_COUNT];
		for (int i = 0; i < SEQUENCE_TYPE_COUNT; i++) {
			sequenceIndexStarts[i] = reader.read32();
		}

		// ignore unknown bytes.
		reader.read32();
		reader.assumeToRead(FILE_HEADER_END);
		return sequenceIndexStarts;
	}

	/**
	 * reads all sequence starts at a given position.
	 * <p>
	 * Does not align torsos and shadows.
	 *
	 * @param reader
	 * 		The reader to read from.
	 * @param sequenceIndexStart
	 * 		The position to start at.
	 * @throws IOException
	 * 		if an read error occurred.
	 */
	private void readSequencesAt(ByteReader reader, int sequenceIndexStart) throws IOException {
		// read data index 0
		reader.skipTo(sequenceIndexStart);

		int sequenceType = reader.read32();

		int byteCount = reader.read16();
		int pointerCount = reader.read16();

		if (byteCount != pointerCount * 4 + 8) {
			throw new IOException("Sequence index block length (" + pointerCount + ") and " + "bytecount (" + byteCount + ") are not consistent.");
		}

		int[] sequenceIndexPointers = new int[pointerCount];
		for (int i = 0; i < pointerCount; i++) {
			sequenceIndexPointers[i] = reader.read32();
		}

		if (sequenceType == ID_SETTLERS) {
			settlerStarts = sequenceIndexPointers;
		} else if (sequenceType == ID_TORSOS) {
			torsoStarts = sequenceIndexPointers;

		} else if (sequenceType == ID_LANDSCAPE) {
			landscapeStarts = sequenceIndexPointers;

		} else if (sequenceType == ID_SHADOWS) {
			shadowStarts = sequenceIndexPointers;

		} else if (sequenceType == ID_GUIS) {
			guiStarts = sequenceIndexPointers;
		}
	}

	private void initializeNullFile() {
		if (settlerStarts == null) {
			settlerStarts = new int[0];
		}
		if (torsoStarts == null) {
			torsoStarts = new int[0];
		}
		if (shadowStarts == null) {
			shadowStarts = new int[0];
		}
		if (landscapeStarts == null) {
			landscapeStarts = new int[0];
		}
		if (guiStarts == null) {
			guiStarts = new int[0];
		}
	}

	private void initializeIfNeeded() {
		if (settlerSequences == null) {
			initialize();
		}
	}

	@Override
	public SequenceList<Image> getSettlers() {
		return directSettlerList;
	}

	private static final Sequence<Image> NULL_SETTLER_SEQUENCE = new ArraySequence<>(new SettlerImage[0]);

	private class DirectSettlerSequenceList implements SequenceList<Image> {
		@Override
		public Sequence<Image> get(int index) {
			initializeIfNeeded();
			if (settlerSequences[index] == null) {
				settlerSequences[index] = NULL_SETTLER_SEQUENCE;
				try {
					loadSettlers(index, file_name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return settlerSequences[index];
		}

		@Override
		public int size() {
			initializeIfNeeded();
			return settlerSequences.length;
		}
	}

	@Override
	public synchronized <T extends Image> long readImageHeader(DatBitmapTranslator<T> translator,
															   ImageMetadata metadata,
															   long offset) throws IOException {
		initializeIfNeeded();
		reader.skipTo(offset);
		DatBitmapReader.readImageHeader(reader, translator, metadata);
		return reader.getReadBytes();
	}

	@Override
	public synchronized <T extends Image> void readCompressedData(DatBitmapTranslator<T> translator,
																  ImageMetadata metadata,
																  ImageArrayProvider array,
																  long offset) throws IOException {
		initializeIfNeeded();
		reader.skipTo(offset);
		DatBitmapReader.readCompressedData(reader, translator, metadata.width, metadata.height, array);
	}

	private synchronized void loadSettlers(int goldIndex, String name) throws IOException {
		initializeIfNeeded();

		int realSettlerIndex = mapping.mapSettlersSequence(goldIndex);
		int realShadowIndex = mapping.mapSettlersSequence(shadowMapping.getShadowIndex(goldIndex));

		int position = settlerStarts[realSettlerIndex];
		long[] framePositions = readSequenceHeader(position);

		SettlerImage[] images = new SettlerImage[framePositions.length];
		for (int i = 0; i < framePositions.length; i++) {
			images[i] = DatBitmapReader.getImage(settlerTranslator, this, framePositions[i], name + "-S" + goldIndex + ":" + i);
		}

		int torsoPosition = torsoStarts[realSettlerIndex];
		if (torsoPosition >= 0) {
			long[] torsoPositions = readSequenceHeader(torsoPosition);
			for (int i = 0; i < torsoPositions.length && i < framePositions.length; i++) {
				SingleImage torso = DatBitmapReader.getImage(torsoTranslator, this, torsoPositions[i], name + "-T" + goldIndex + ":" + i);
				images[i].setTorso(torso);
			}
		}

		int shadowPosition = realShadowIndex != -1 ? shadowStarts[realShadowIndex] : -1;
		if (shadowPosition >= 0) {
			long[] shadowPositions = readSequenceHeader(shadowPosition);
			for (int i = 0; i < shadowPositions.length
				&& i < framePositions.length; i++) {
				SingleImage shadow = DatBitmapReader.getImage(shadowTranslator, this, shadowPositions[i], name + "-SH" + goldIndex + ":" + i);
				images[i].setShadow(shadow);
			}
		}

		settlerSequences[goldIndex] = new ArraySequence<>(images);
	}

	private long[] readSequenceHeader(int position) throws IOException {
		reader.skipTo(position);

		reader.assumeToRead(START);
		int frameCount = reader.read8();

		long[] framePositions = new long[frameCount];
		for (int i = 0; i < frameCount; i++) {
			framePositions[i] = reader.read32() + position;
		}
		return framePositions;
	}

	@Override
	public Sequence<SingleImage> getLandscapes() {
		return landscapeSequence;
	}

	@Override
	public Sequence<SingleImage> getGuis() {
		return guiSequence;
	}

	/**
	 * This landscape image list loads the landscape images.
	 *
	 * @author michael
	 */
	private class LandscapeImageSequence implements Sequence<SingleImage> {
		/**
		 * Forces a get of the image.
		 */
		@Override
		public SingleImage getImage(int index, Supplier<String> custom_name) {
			initializeIfNeeded();
			if (landscapeImages[index] == null) {
				String str_custom_name = custom_name != null ? custom_name.get() : null;
				if(str_custom_name == null) str_custom_name = "";
				loadLandscapeImage(index, str_custom_name+file_name+"-L"+index);
			}
			return landscapeImages[index];
		}

		@Override
		public int length() {
			initializeIfNeeded();
			return landscapeImages.length;
		}

		@Override
		public SingleImage getImageSafe(int index, Supplier<String> custom_name) {
			initializeIfNeeded();
			if (index < 0 || index >= length()) {
				return NullImage.getInstance();
			} else {
				if (landscapeImages[index] == null) {
					String str_custom_name = custom_name != null ? custom_name.get() : null;
					if(str_custom_name == null) str_custom_name = "";
					loadLandscapeImage(index, str_custom_name+file_name+"-L"+index);
				}
				return landscapeImages[index];
			}
		}
	}

	@Override
	public long getOffsetForLandscape(int index) {
		initializeIfNeeded();

		return landscapeStarts[index];
	}

	private void loadLandscapeImage(int index, String name) {
		initializeIfNeeded();

		try {
			SingleImage image = DatBitmapReader.getImage(landscapeTranslator, this, landscapeStarts[index], name);
			landscapeImages[index] = image;
		} catch (IOException e) {
			landscapeImages[index] = NullImage.getForLandscape();
		}
	}

	/**
	 * This landscape image list loads the landscape images.
	 *
	 * @author michael
	 */
	private class GuiImageSequence implements Sequence<SingleImage> {
		/**
		 * Forces a get of the image.
		 */
		@Override
		public SingleImage getImage(int index, Supplier<String> custom_name) {
			initializeIfNeeded();
			if (guiImages[index] == null) {
				String str_custom_name = custom_name != null ? custom_name.get() : null;
				if(str_custom_name == null) str_custom_name = "";
				loadGuiImage(index, str_custom_name+file_name+"-G"+index);
			}
			return guiImages[index];
		}

		@Override
		public int length() {
			initializeIfNeeded();
			return guiImages.length;
		}

		@Override
		public SingleImage getImageSafe(int index, Supplier<String> custom_name) {
			initializeIfNeeded();
			if (index < 0 || index >= length()) {
				return NullImage.getInstance();
			} else {
				if (guiImages[index] == null) {
					String str_custom_name = custom_name != null ? custom_name.get() : null;
					if(str_custom_name == null) str_custom_name = "";
					loadGuiImage(index, str_custom_name+file_name+"-G"+index);
				}
				return guiImages[index];
			}
		}
	}

	private void loadGuiImage(int goldIndex, String name) {
		initializeIfNeeded();
		try {
			int theseGraphicsFilesIndex = mapping.mapGuiImage(goldIndex);
			SingleImage image = DatBitmapReader.getImage(guiTranslator, this, guiStarts[theseGraphicsFilesIndex], name);
			guiImages[goldIndex] = image;
		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			guiImages[goldIndex] = NullImage.getForGui();
		}
	}

	public long[] getSettlerPointers(int seqIndex) throws IOException {
		initializeIfNeeded();
		return readSequenceHeader(settlerStarts[seqIndex]);
	}

	public long[] getTorsoPointers(int seqIndex) throws IOException {
		initializeIfNeeded();
		int position = torsoStarts[seqIndex];
		if (position >= 0) {
			return readSequenceHeader(position);
		} else {
			return null;
		}
	}

	public DatBitmapTranslator<SettlerImage> getSettlerTranslator() {
		return settlerTranslator;
	}

	public DatBitmapTranslator<SingleImage> getTorsoTranslator() {
		return torsoTranslator;
	}

	@Override
	public DatBitmapTranslator<SingleImage> getLandscapeTranslator() {
		return landscapeTranslator;
	}
}