package jsettlers.main.android.mainmenu.gamesetup;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.android.core.AndroidPreferences;
import jsettlers.main.android.core.GameStarter;

public class NewMultiPlayerSetupViewModel extends MultiPlayerSetupViewModel {

	public NewMultiPlayerSetupViewModel(GameStarter gameStarter, AndroidPreferences androidPreferences, IJoinPhaseMultiplayerGameConnector connector, MapLoader mapLoader) {
		super(gameStarter, androidPreferences, connector, mapLoader);

		setAllPlayerSlotsEnabled(true);
	}

	@Override
	public void playerCountSelected(PlayerCount item) {
		if(realPlayerCount != item.getNumberOfPlayers()) {
			long minPlayerCount = playerSlotPresenters.stream().filter(p -> p.getPlayerSettings().getPlayerType().equals(EPlayerType.HUMAN)).count();

			if(item.getNumberOfPlayers() < minPlayerCount) {
				item = new PlayerCount(realPlayerCount);
			} else {
				connector.setPlayerCount(item.getNumberOfPlayers());
			}
		}
		super.playerCountSelected(item);
	}

	@Override
	protected void abort() {
		super.abort();
		connector.abort();
	}

	/**
	 * ViewModel factory
	 */
	public static class Factory implements ViewModelProvider.Factory {

		private final Activity activity;
		private final String mapId;

		public Factory(Activity activity, String mapId) {
			this.activity = activity;
			this.mapId = mapId;
		}

		@Override
		public <T extends ViewModel> T create(Class<T> modelClass) {
			GameStarter gameStarter = (GameStarter) activity.getApplication();
			MapLoader mapLoader = gameStarter.getMapList().getMapById(mapId);
			IJoinPhaseMultiplayerGameConnector joinPhaseMultiplayerGameConnector = gameStarter.getJoinPhaseMultiplayerConnector();

			if (joinPhaseMultiplayerGameConnector == null) {
				throw new MultiPlayerConnectorUnavailableException();
			}

			if (modelClass == NewMultiPlayerSetupViewModel.class) {
				return (T) new NewMultiPlayerSetupViewModel(gameStarter, new AndroidPreferences(activity), joinPhaseMultiplayerGameConnector, mapLoader);
			}
			throw new RuntimeException("NewMultiPlayerSetupViewModel.Factory doesn't know how to create a: " + modelClass.toString());
		}
	}

	@Override
	protected boolean amITheHost() {
		return true;
	}
}
