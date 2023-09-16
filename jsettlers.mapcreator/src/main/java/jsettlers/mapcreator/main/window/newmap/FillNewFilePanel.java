package jsettlers.mapcreator.main.window.newmap;

import jsettlers.common.landscape.ELandscapeType;
import jsettlers.logic.map.loading.newmap.MapFileHeader;
import jsettlers.mapcreator.data.MapData;
import jsettlers.mapcreator.localization.EditorLabels;
import jsettlers.mapcreator.main.window.sidebar.RectIcon;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;

public class FillNewFilePanel extends JPanel implements InitialMapProvider {


	/**
	 * Available ground types
	 */
	private static final ELandscapeType[] GROUND_TYPES = new ELandscapeType[] { ELandscapeType.WATER8, ELandscapeType.GRASS,
			ELandscapeType.DRY_GRASS, ELandscapeType.SNOW, ELandscapeType.DESERT };

	/**
	 * Selected ground type
	 */
	private final JComboBox<ELandscapeType> groundTypes;

	public FillNewFilePanel() {
		this.groundTypes = new JComboBox<>(GROUND_TYPES);
		add(groundTypes);
		groundTypes.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				ELandscapeType type = (ELandscapeType) value;
				setIcon(new RectIcon(22, new Color(type.color.getARGB()), Color.GRAY));
				setText(EditorLabels.getLabel("landscape." + type.name()));

				return this;
			}
		});
	}

	@Override
	public MapData getMapData(MapFileHeader header) {
		return new MapData(header, (ELandscapeType) groundTypes.getSelectedItem());
	}
}
