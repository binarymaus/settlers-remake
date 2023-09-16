package jsettlers.mapcreator.main.window.newmap;

import jsettlers.common.Color;
import jsettlers.common.landscape.ELandscapeType;
import jsettlers.logic.map.loading.newmap.MapFileHeader;
import jsettlers.mapcreator.data.MapData;
import jsettlers.mapcreator.tools.buffers.ByteMapArea;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageImportPanel extends Box implements InitialMapProvider {

	private BufferedImage currentImage;


	private final FilePicker picker = new FilePicker() {
		@Override
		protected boolean checkFile(File file) {
			return checkImageFile(file);
		}
	};

	private final JLabel imagePreview = new JLabel();

	public ImageImportPanel() {
		super(BoxLayout.Y_AXIS);
		add(picker);

		imagePreview.setMaximumSize(new Dimension(256, 256));
		add(imagePreview);
	}

	private boolean checkImageFile(File file) {
		if(!file.exists()) return false;

		BufferedImage content;
		try {
			content = ImageIO.read(file);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, e);
			return false;
		}

		currentImage = content;

		imagePreview.setIcon(new ImageIcon(currentImage));
		return true;
	}

	@Override
	public MapData getMapData(MapFileHeader header) {
		if(currentImage == null) return null;

		int width = header.getWidth();
		int height = header.getHeight();

		Image scaledImg = currentImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage templateImg = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics graph = templateImg.getGraphics();
		graph.drawImage(scaledImg, 0, 0, null);
		graph.dispose();

		MapData data = new MapData(header, ELandscapeType.WATER8);

		byte[][] grassArea = new byte[width][height];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if((templateImg.getRGB(x, y)&0x0000FF00) > 0) {
					grassArea[x][y] = Byte.MAX_VALUE;
				}
			}
		}

		data.fill(ELandscapeType.GRASS, new ByteMapArea(grassArea));

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				Color c = Color.getABGR(templateImg.getRGB(x, y));
				if(c.getGreen() > 0.1f) {
					data.setHeightUnsafe(x, y, (int) ((1.f-c.toGreyScale().getGreen())*255));
				}
			}
		}

		return data;
	}
}
