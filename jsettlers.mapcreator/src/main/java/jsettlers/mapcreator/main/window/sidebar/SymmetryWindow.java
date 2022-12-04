package jsettlers.mapcreator.main.window.sidebar;

import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.utils.collections.map.ArrayListMap;
import jsettlers.common.utils.mutables.Mutable;
import jsettlers.mapcreator.data.symmetry.DefaultSymmetries;
import jsettlers.mapcreator.data.symmetry.SymmetryConfig;
import jsettlers.mapcreator.tools.shapes.ShapeIcon;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import java.util.Map;

public class SymmetryWindow extends JPanel {

	public SymmetryWindow() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public static JComponent createOverview(Mutable<SymmetryConfig> symmetry, Mutable<ShortPoint2D> symPoint) {
		JToolBar symInfo = new JToolBar();
		symInfo.setFloatable(false);

		ButtonGroup symGroup = new ButtonGroup();

		for(Map.Entry<SymmetryConfig, ShapeIcon> preset : PRESETS.entrySet()) {
			JButton sym = new JButton(preset.getValue());
			SymmetryConfig cfg = preset.getKey();
			sym.addActionListener(l -> {
				symmetry.object = cfg;
			});
			symGroup.add(sym);
			symInfo.add(sym);
		}

		symGroup.getElements().nextElement().setSelected(true);

		/*JButton openSymPanel = new JButton(Labels.getString("symmetry.open"));
		openSymPanel.addActionListener(l -> {
			SymmetryWindow sym = new SymmetryWindow();
		});

		symGroup.add(openSymPanel);
		symInfo.add(openSymPanel);*/
		return symInfo;
	}

	private static final Map<SymmetryConfig, ShapeIcon> PRESETS = new ArrayListMap<>();

	static {
		PRESETS.put(DefaultSymmetries.DEFAULT, ShapeIcon.NO_SYM);
		PRESETS.put(DefaultSymmetries.REPEAT4, ShapeIcon.SYM_REPEAT4);
		PRESETS.put(DefaultSymmetries.POINT, ShapeIcon.SYM_POINT);
	}
}
