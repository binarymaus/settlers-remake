/*******************************************************************************
 * Copyright (c) 2015 - 2017
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
package jsettlers.mapcreator.main.window.sidebar;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import jsettlers.common.landscape.ELandscapeType;
import jsettlers.common.landscape.EResourceType;
import jsettlers.graphics.map.controls.original.panel.content.buildings.EBuildingsCategory;
import jsettlers.logic.map.loading.data.objects.DecorationMapDataObject;
import jsettlers.logic.map.loading.data.objects.StoneMapDataObject;
import jsettlers.logic.map.loading.data.objects.MapTreeObject;
import jsettlers.common.mapobject.EMapObjectType;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.movable.EMovableType;
import jsettlers.exceptionhandler.ExceptionHandler;
import jsettlers.mapcreator.control.IPlayerSetter;
import jsettlers.mapcreator.data.symmetry.SymmetryConfig;
import jsettlers.mapcreator.localization.EditorLabels;
import jsettlers.mapcreator.main.tools.PlaceStackToolbox;
import jsettlers.mapcreator.main.tools.ToolTreeModel;
import jsettlers.mapcreator.presetloader.PresetLoader;
import jsettlers.mapcreator.tools.SetStartpointTool;
import jsettlers.mapcreator.tools.SetSymmetrypointTool;
import jsettlers.mapcreator.tools.Tool;
import jsettlers.mapcreator.tools.ToolBox;
import jsettlers.mapcreator.tools.ToolNode;
import jsettlers.mapcreator.tools.landscape.FixHeightsTool;
import jsettlers.mapcreator.tools.landscape.FlatLandscapeTool;
import jsettlers.mapcreator.tools.landscape.IncreaseDecreaseHeightAdder;
import jsettlers.mapcreator.tools.landscape.LandscapeHeightTool;
import jsettlers.mapcreator.tools.landscape.PlaceResource;
import jsettlers.mapcreator.tools.landscape.SetLandscapeTool;
import jsettlers.mapcreator.tools.objects.DeleteObjectTool;
import jsettlers.mapcreator.tools.objects.PlaceBuildingTool;
import jsettlers.mapcreator.tools.objects.PlaceMapObjectTool;
import jsettlers.mapcreator.tools.objects.PlaceMovableTool;
import jsettlers.mapcreator.tools.shapes.ShapeType;

/**
 * Sidebar with tools
 * 
 * @author Andreas Butti
 */
public abstract class ToolSidebar extends JPanel implements IPlayerSetter {
	private static final long serialVersionUID = 1L;

	/**
	 * Get active player
	 */
	private final IPlayerSetter playerSetter;

	/**
	 * Panel with the shape settings
	 */
	private final ToolSettingsPanel drawProperties = new ToolSettingsPanel();

	/**
	 * Presets, Templates: Loaded from .xml file
	 */
	private final ToolBox PRESETS = new ToolBox(EditorLabels.getLabel("presets"));

	// @formatter:off
	private final ToolNode TOOLBOX = new ToolBox("<toolbox root>, hidden", new ToolNode[]{
			new ToolBox(EditorLabels.getLabel("tools.category.landscape"), new ToolNode[]{
					new SetLandscapeTool(ELandscapeType.GRASS, false),
					new SetLandscapeTool(ELandscapeType.DRY_GRASS, false),
					new SetLandscapeTool(ELandscapeType.SAND, false),
					new SetLandscapeTool(ELandscapeType.FLATTENED, false),
					new SetLandscapeTool(ELandscapeType.DESERT, false),
					new SetLandscapeTool(ELandscapeType.EARTH, false),
					new SetLandscapeTool(ELandscapeType.DRY_EARTH, false),
					new SetLandscapeTool(ELandscapeType.WATER1, false),
					new SetLandscapeTool(ELandscapeType.WATER2, false),
					new SetLandscapeTool(ELandscapeType.WATER3, false),
					new SetLandscapeTool(ELandscapeType.WATER4, false),
					new SetLandscapeTool(ELandscapeType.WATER5, false),
					new SetLandscapeTool(ELandscapeType.WATER6, false),
					new SetLandscapeTool(ELandscapeType.WATER7, false),
					new SetLandscapeTool(ELandscapeType.WATER8, false),
					new SetLandscapeTool(ELandscapeType.RIVER1, true),
					new SetLandscapeTool(ELandscapeType.RIVER2, true),
					new SetLandscapeTool(ELandscapeType.RIVER3, true),
					new SetLandscapeTool(ELandscapeType.RIVER4, true),
					new SetLandscapeTool(ELandscapeType.MOUNTAIN, false),
					new SetLandscapeTool(ELandscapeType.SNOW, false),
					new SetLandscapeTool(ELandscapeType.MOOR, false),
					new SetLandscapeTool(ELandscapeType.FLATTENED_DESERT, false),
					new SetLandscapeTool(ELandscapeType.SHARP_FLATTENED_DESERT, false),
					new SetLandscapeTool(ELandscapeType.GRAVEL, false),
					new SetLandscapeTool(ELandscapeType.ROAD, false),
			}),
			new ToolBox(EditorLabels.getLabel("tools.category.heigths"), new ToolNode[]{
					new LandscapeHeightTool(),
					new IncreaseDecreaseHeightAdder(true),
					new IncreaseDecreaseHeightAdder(false),
					new FlatLandscapeTool(),
					new FixHeightsTool()
			}),
			new ToolBox(EditorLabels.getLabel("tools.category.land-resources"),
					PlaceResource.createArray(EResourceType.VALUES)
			),
			new ToolBox(EditorLabels.getLabel("tools.category.objects"), new ToolNode[]{
					new PlaceMapObjectTool(MapTreeObject.getInstance()),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(0)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(1)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(2)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(3)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(4)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(5)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(6)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(7)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(8)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(9)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(10)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(11)),
					new PlaceMapObjectTool(StoneMapDataObject.getInstance(12)),
					new PlaceMapObjectTool(new DecorationMapDataObject(EMapObjectType.PLANT_DECORATION)),
					new PlaceMapObjectTool(new DecorationMapDataObject(EMapObjectType.DESERT_DECORATION)),
					new PlaceMapObjectTool(new DecorationMapDataObject(EMapObjectType.SWAMP_DECORATION))
			}),
			new ToolBox(EditorLabels.getLabel("tools.category.settlers"), new ToolNode[]{
					new ToolBox(EditorLabels.getLabel("tools.category.worker"),
							PlaceMovableTool.createArray(this,
									EMovableType.BEARER,
									EMovableType.BRICKLAYER,
									EMovableType.DIGGER,
									EMovableType.BAKER,
									EMovableType.CHARCOAL_BURNER,
									EMovableType.FARMER,
									EMovableType.FISHERMAN,
									EMovableType.FORESTER,
									EMovableType.LUMBERJACK,
									EMovableType.MELTER,
									EMovableType.MILLER,
									EMovableType.MINER,
									EMovableType.PIG_FARMER,
									EMovableType.SAWMILLER,
									EMovableType.SLAUGHTERER,
									EMovableType.SMITH,
									EMovableType.STONECUTTER,
									EMovableType.WATERWORKER,
									EMovableType.DOCKWORKER,
									EMovableType.BREWER,
									EMovableType.RICE_FARMER,
									EMovableType.DISTILLER,
									EMovableType.ALCHEMIST,
									EMovableType.MEAD_BREWER
									)
					),
					new ToolBox(EditorLabels.getLabel("tools.category.specialist"),
							PlaceMovableTool.createArray(this,
									EMovableType.GEOLOGIST,
									EMovableType.PIONEER,
									EMovableType.THIEF,
									EMovableType.DONKEY)
					),
					new ToolBox(EditorLabels.getLabel("tools.category.ships"),
							PlaceMovableTool.createArray(this, EMovableType.SHIPS)),
					new ToolBox(EditorLabels.getLabel("tools.category.soldier"),
							PlaceMovableTool.createArray(this, EMovableType.SOLDIERS)
					)
			}),
			new ToolBox(EditorLabels.getLabel("tools.category.materials"), new ToolNode[]{
					new ToolBox(EditorLabels.getLabel("tools.category.mat-build"), new ToolNode[]{
							new PlaceStackToolbox(EMaterialType.PLANK, 8),
							new PlaceStackToolbox(EMaterialType.STONE, 8),
							new PlaceStackToolbox(EMaterialType.TRUNK, 8)
					}),
					new ToolBox(EditorLabels.getLabel("tools.category.mat-food"), new ToolNode[]{
							new PlaceStackToolbox(EMaterialType.BREAD, 8),
							new PlaceStackToolbox(EMaterialType.CROP, 8),
							new PlaceStackToolbox(EMaterialType.FISH, 8),
							new PlaceStackToolbox(EMaterialType.FLOUR, 8),
							new PlaceStackToolbox(EMaterialType.PIG, 8),
							new PlaceStackToolbox(EMaterialType.MEAT, 8),
							new PlaceStackToolbox(EMaterialType.WATER, 8),
							new PlaceStackToolbox(EMaterialType.RICE, 8),
							new PlaceStackToolbox(EMaterialType.WINE, 8),
							new PlaceStackToolbox(EMaterialType.HONEY, 8),
							new PlaceStackToolbox(EMaterialType.MEAD, 8),
							new PlaceStackToolbox(EMaterialType.LIQUOR, 8),
							new PlaceStackToolbox(EMaterialType.KEG, 8)
					}),
					new ToolBox(EditorLabels.getLabel("tools.category.mat-resources"), new ToolNode[]{
							new PlaceStackToolbox(EMaterialType.COAL, 8),
							new PlaceStackToolbox(EMaterialType.IRON, 8),
							new PlaceStackToolbox(EMaterialType.IRONORE, 8),
							new PlaceStackToolbox(EMaterialType.GOLD, 8),
							new PlaceStackToolbox(EMaterialType.GOLDORE, 8),
							new PlaceStackToolbox(EMaterialType.SULFUR, 8),
							new PlaceStackToolbox(EMaterialType.GEMS, 8),
					}),
					new ToolBox(EditorLabels.getLabel("tools.category.mat-tools"), new ToolNode[]{
							new PlaceStackToolbox(EMaterialType.HAMMER, 8),
							new PlaceStackToolbox(EMaterialType.BLADE, 8),
							new PlaceStackToolbox(EMaterialType.AXE, 8),
							new PlaceStackToolbox(EMaterialType.SAW, 8),
							new PlaceStackToolbox(EMaterialType.PICK, 8),
							new PlaceStackToolbox(EMaterialType.SCYTHE, 8),
							new PlaceStackToolbox(EMaterialType.FISHINGROD, 8)
					}),
					new ToolBox(EditorLabels.getLabel("tools.category.mat-weapons"), new ToolNode[]{
							new PlaceStackToolbox(EMaterialType.SWORD, 8),
							new PlaceStackToolbox(EMaterialType.BOW, 8),
							new PlaceStackToolbox(EMaterialType.SPEAR, 8),
							new PlaceStackToolbox(EMaterialType.GUN_POWDER, 8),
							new PlaceStackToolbox(EMaterialType.BALLISTA_AMMO, 6),
							new PlaceStackToolbox(EMaterialType.CATAPULT_AMMO, 6),
							new PlaceStackToolbox(EMaterialType.CANNON_AMMO, 6),
					}),
			}),
			new ToolBox(EditorLabels.getLabel("tools.category.buildings"), new ToolNode[]{
					new ToolBox(EditorLabels.getLabel("tools.category.resources"),
							PlaceBuildingTool.createArray(this, EBuildingsCategory.BUILDINGS_CATEGORY_NORMAL)
					),
					new ToolBox(EditorLabels.getLabel("tools.category.food"),
							PlaceBuildingTool.createArray(this, EBuildingsCategory.BUILDINGS_CATEGORY_FOOD)
					),
					new ToolBox(EditorLabels.getLabel("tools.category.military"),
							PlaceBuildingTool.createArray(this, EBuildingsCategory.BUILDINGS_CATEGORY_MILITARY)
					),
					new ToolBox(EditorLabels.getLabel("tools.category.social"),
							PlaceBuildingTool.createArray(this, EBuildingsCategory.BUILDINGS_CATEGORY_SOCIAL)
					)
			}),
			PRESETS,

			new SetStartpointTool(this),
			new SetSymmetrypointTool(drawProperties),
			new DeleteObjectTool(),
	});
	// @formatter:on

	/**
	 * Constructor
	 * 
	 * @param playerSetter
	 *            Interface to get current active player
	 */
	public ToolSidebar(IPlayerSetter playerSetter) {
		setLayout(new BorderLayout());
		this.playerSetter = playerSetter;

		loadPresets();

		final JTree toolshelf = new JTree(new ToolTreeModel(TOOLBOX));
		add(new JScrollPane(toolshelf), BorderLayout.CENTER);
		toolshelf.addTreeSelectionListener(e -> {
			TreePath path = e.getNewLeadSelectionPath();
			if (path == null) {
				changeTool(null);
				return;
			}
			Object lastPathComponent = path.getLastPathComponent();
			if (lastPathComponent instanceof ToolBox) {
				changeTool(null);
			} else if (lastPathComponent instanceof Tool) {
				Tool newTool = (Tool) lastPathComponent;
				changeTool(newTool);
			}

		});
		toolshelf.setCellRenderer(new ToolRenderer());
		toolshelf.setRootVisible(false);

		add(drawProperties, BorderLayout.NORTH);
	}

	/**
	 * Load presets, from internal .xml and additional external .xml file
	 */
	private void loadPresets() {
		PresetLoader loader = new PresetLoader(PRESETS, this);
		try {
			loader.load(PresetLoader.class.getResourceAsStream("preset.xml"));
		} catch (Exception e) {
			ExceptionHandler.displayError(e, "Could not load internal preset.xml file!");
		}

	}

	/**
	 * @return The active shape
	 */
	public ShapeType getActiveShape() {
		return drawProperties.getActiveShape();
	}

	protected abstract void changeTool(Tool lastPathComponent);

	@Override
	public int getActivePlayer() {
		return playerSetter.getActivePlayer();
	}

	/**
	 * Update the shape buttons
	 * 
	 * @param tool
	 *            Selected tool
	 */
	public void updateShapeSettings(Tool tool) {
		drawProperties.updateShapeSettings(tool);
	}

	public SymmetryConfig getSymmetry() {
		return drawProperties.getSymmetry();
	}
}
