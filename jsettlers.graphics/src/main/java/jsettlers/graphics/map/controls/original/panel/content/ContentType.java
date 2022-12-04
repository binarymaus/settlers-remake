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
package jsettlers.graphics.map.controls.original.panel.content;

import jsettlers.common.player.IInGamePlayer;
import jsettlers.graphics.map.controls.original.panel.content.buildings.BuildingBuildContent;
import jsettlers.graphics.map.controls.original.panel.content.buildings.EBuildingsCategory;
import jsettlers.graphics.map.controls.original.panel.content.material.distribution.DistributionPanel;
import jsettlers.graphics.map.controls.original.panel.content.material.inventory.InventoryPanel;
import jsettlers.graphics.map.controls.original.panel.content.material.priorities.MaterialPriorityContent;
import jsettlers.graphics.map.controls.original.panel.content.material.production.MaterialsProductionPanel;
import jsettlers.graphics.map.controls.original.panel.content.settlers.profession.ProfessionPanel;
import jsettlers.graphics.map.controls.original.panel.content.settlers.statistics.SettlersStatisticsPanel;
import jsettlers.graphics.map.controls.original.panel.content.settlers.warriors.WarriorsPanel;
import jsettlers.graphics.ui.UIPanel;

/**
 * This are the main content types
 * 
 * @author michael
 */
public final class ContentType {
	public static final AbstractContentProvider EMPTY = new AbstractContentProvider() {
		@Override
		public UIPanel getPanel() {
			return new UIPanel();
		}

		@Override
		public boolean isForSelection() {
			// This is the empty selection content.
			return true;
		}
	};

	public static final BuildingBuildContent BUILD_NORMAL = new BuildingBuildContent(EBuildingsCategory.BUILDINGS_CATEGORY_NORMAL);
	public static final BuildingBuildContent BUILD_FOOD = new BuildingBuildContent(EBuildingsCategory.BUILDINGS_CATEGORY_FOOD);
	public static final BuildingBuildContent BUILD_MILITARY = new BuildingBuildContent(EBuildingsCategory.BUILDINGS_CATEGORY_MILITARY);
	public static final BuildingBuildContent BUILD_SOCIAL = new BuildingBuildContent(EBuildingsCategory.BUILDINGS_CATEGORY_SOCIAL);

	public static final InventoryPanel STOCK = new InventoryPanel();
	public static final MaterialsProductionPanel TOOLS = new MaterialsProductionPanel();
	public static final DistributionPanel GOODS_SPREAD = new DistributionPanel();
	public static final MaterialPriorityContent GOODS_TRANSPORT = new MaterialPriorityContent();

	public static final SettlersStatisticsPanel SETTLER_STATISTIC = new SettlersStatisticsPanel();
	public static final ProfessionPanel PROFESSION = new ProfessionPanel();
	public static final WarriorsPanel WARRIORS = new WarriorsPanel();
	public static final AbstractContentProvider PRODUCTION = EMPTY;

	private ContentType() {
	}

	public static void setPlayer(IInGamePlayer player) {
		BUILD_NORMAL.setPlayer(player);
		BUILD_FOOD.setPlayer(player);
		BUILD_MILITARY.setPlayer(player);
		BUILD_SOCIAL.setPlayer(player);

		STOCK.setPlayer(player);
		TOOLS.setPlayer(player);
		GOODS_SPREAD.setPlayer(player);

		SETTLER_STATISTIC.setPlayer(player);
		PROFESSION.setPlayer(player);
		WARRIORS.setPlayer(player);
	}
}
