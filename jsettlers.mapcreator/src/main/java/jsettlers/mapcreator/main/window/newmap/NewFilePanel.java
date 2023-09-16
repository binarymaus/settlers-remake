/*******************************************************************************
 * Copyright (c) 2015 - 2016
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
 *******************************************************************************/
package jsettlers.mapcreator.main.window.newmap;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;

import jsettlers.logic.map.loading.newmap.MapFileHeader;
import jsettlers.logic.map.loading.newmap.MapFileHeader.MapType;
import jsettlers.mapcreator.data.MapData;
import jsettlers.mapcreator.localization.EditorLabels;
import jsettlers.mapcreator.main.window.MapHeaderEditorPanel;

/**
 * Panel to create a new file
 * 
 * @author Andreas Butti
 *
 */
public class NewFilePanel extends Box {
	private static final long serialVersionUID = 1L;

	/**
	 * Default header values
	 */
	private static final MapFileHeader DEFAULT = new MapFileHeader(MapType.NORMAL, "new map", null, "", (short) 300, (short) 300, (short) 1,
			(short) 10, null, new short[MapFileHeader.PREVIEW_IMAGE_SIZE * MapFileHeader.PREVIEW_IMAGE_SIZE]);

	private final FillNewFilePanel fillPanel = new FillNewFilePanel();
	private final ImageImportPanel imgImport = new ImageImportPanel();
	private final JTabbedPane creationType = new JTabbedPane();

	/**
	 * Header editor panel
	 */
	private final MapHeaderEditorPanel headerEditor;

	/**
	 * Constructor
	 */
	public NewFilePanel() {
		super(BoxLayout.Y_AXIS);
		this.headerEditor = new MapHeaderEditorPanel(DEFAULT, true);
		headerEditor.setBorder(BorderFactory.createTitledBorder(EditorLabels.getLabel("newfile.map-settings")));
		add(headerEditor);
		headerEditor.getNameField().selectAll();

		creationType.addTab(EditorLabels.getLabel("newfile.ground-type"), fillPanel);
		creationType.addTab(EditorLabels.getLabel("newfile.image"), imgImport);

		add(creationType);

	}

	/**
	 * @return The selected ground type
	 */
	public MapData getMapData() {
		Component current = creationType.getSelectedComponent();
		if(current == null) {
			current = fillPanel;
		}

		return ((InitialMapProvider) current).getMapData(getHeader());
	}

	/**
	 * @return The configured map header
	 */
	public MapFileHeader getHeader() {
		return headerEditor.getHeader();
	}
}
