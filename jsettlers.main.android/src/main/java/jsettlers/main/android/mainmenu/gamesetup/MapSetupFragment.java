/*
 * Copyright (c) 2017
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

package jsettlers.main.android.mainmenu.gamesetup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import jsettlers.main.android.R;
import jsettlers.main.android.core.resources.PreviewImageConverter;
import jsettlers.main.android.core.ui.FragmentUtil;
import jsettlers.main.android.core.ui.OnItemSelectedListener;
import jsettlers.main.android.mainmenu.gamesetup.playeritem.Civilisation;
import jsettlers.main.android.mainmenu.gamesetup.playeritem.PlayerSlotPresenter;
import jsettlers.main.android.mainmenu.gamesetup.playeritem.PlayerType;
import jsettlers.main.android.mainmenu.gamesetup.playeritem.StartPosition;
import jsettlers.main.android.mainmenu.gamesetup.playeritem.Team;
import jsettlers.main.android.mainmenu.navigation.MainMenuNavigator;

public abstract class MapSetupFragment extends Fragment {
	protected static final String ARG_MAP_ID = "mapid";

	protected Spinner numberOfPlayersSpinner;
	protected Spinner startResourcesSpinner;
	protected Spinner peacetimeSpinner;
	protected Toolbar toolbar;

	private RecyclerView recyclerView;

	protected String mapId;

	private MapSetupViewModel viewModel;

	PlayersAdapter adapter;
	ArrayAdapter<PlayerCount> playerCountsAdapter;
	ArrayAdapter<StartResources> startResourcesAdapter;
	ArrayAdapter<Peacetime> peaceTimesAdapter;

	protected abstract MapSetupViewModel createViewModel();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			mapId = getArguments().getString(ARG_MAP_ID);
			viewModel = createViewModel();
		} catch (MultiPlayerConnectorUnavailableException e) {
			MainMenuNavigator mainMenuNavigator = (MainMenuNavigator) getActivity();
			mainMenuNavigator.popToMenuRoot();
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_new_single_player_setup, container, false);

		toolbar = view.findViewById(R.id.toolbar);
		FragmentUtil.setActionBar(this, toolbar);

		recyclerView = view.findViewById(R.id.recycler_view);
		recyclerView.setHasFixedSize(true);

		view.findViewById(R.id.button_start_game).setOnClickListener(v -> viewModel.startGame());

		// Disable this for now, as these features are not implemented yet.
		startResourcesSpinner = view.findViewById(R.id.spinner_start_resources);
		startResourcesSpinner.setEnabled(false);
		startResourcesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				viewModel.startResourcesSelected(startResourcesAdapter.getItem(position));
			}
		});

		// Disable this for now, as these features are not implemented yet.
		peacetimeSpinner = view.findViewById(R.id.spinner_peacetime);
		peacetimeSpinner.setEnabled(false);
		peacetimeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				viewModel.peaceTimeSelected(peaceTimesAdapter.getItem(position));
			}
		});

		numberOfPlayersSpinner = view.findViewById(R.id.spinner_number_of_players);
		numberOfPlayersSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				viewModel.playerCountSelected(playerCountsAdapter.getItem(position));
			}
		});

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (viewModel == null) {
			return;
		}

		viewModel.getPlayerCountOptions().observe(this, playerCounts -> {
			playerCountsAdapter = getSpinnerAdapter(playerCounts);
			numberOfPlayersSpinner.setAdapter(playerCountsAdapter);
		});
		viewModel.getPlayerCount().observe(this, playerCount -> numberOfPlayersSpinner.setSelection(playerCountsAdapter.getPosition(playerCount)));

		viewModel.getStartResourcesOptions().observe(this, startResources -> {
			startResourcesAdapter = getSpinnerAdapter(startResources);
			startResourcesSpinner.setAdapter(startResourcesAdapter);
		});
		viewModel.getStartResources().observe(this, startResources -> startResourcesSpinner.setSelection(startResourcesAdapter.getPosition(startResources)));

		viewModel.getPeaceTimeOptions().observe(this, peacetimes -> {
			peaceTimesAdapter = getSpinnerAdapter(peacetimes);
			peacetimeSpinner.setAdapter(peaceTimesAdapter);
		});
		viewModel.getPeaceTime().observe(this, peacetime -> peacetimeSpinner.setSelection(peaceTimesAdapter.getPosition(peacetime)));

		ImageView mapPreviewImageView = getView().findViewById(R.id.image_view_map_preview);
		viewModel.getImage().observe(this, image -> mapPreviewImageView.setImageBitmap(PreviewImageConverter.convert(image)));
		viewModel.getTitle().observe(this, title -> toolbar.setTitle(title));
		viewModel.getPlayerSlots().observe(this, playerSlotPresenters -> {
			if (adapter == null) {
				adapter = new PlayersAdapter();
			}

			if (recyclerView.getAdapter() == null) {
				recyclerView.setAdapter(adapter);
			}

			adapter.setItems(playerSlotPresenters);
		});

		viewModel.getShowMapEvent().observe(this, z -> {
			MainMenuNavigator mainMenuNavigator = (MainMenuNavigator) getActivity();
			mainMenuNavigator.showGame();
		});
	}

	private <T> ArrayAdapter<T> getSpinnerAdapter(T[] items) {
		ArrayAdapter<T> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	protected int getListItemLayoutId() {
		return R.layout.item_playerslot;
	}

	/**
	 * Recyclerview adapter
	 */
	class PlayersAdapter extends RecyclerView.Adapter<PlayerHolder> {
		private final LayoutInflater layoutInflater;

		private PlayerSlotPresenter[] players = new PlayerSlotPresenter[0];

		PlayersAdapter() {
			this.layoutInflater = LayoutInflater.from(getActivity());
		}

		@Override
		public int getItemCount() {
			return players.length;
		}

		@Override
		public PlayerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = layoutInflater.inflate(getListItemLayoutId(), parent, false);
			return new PlayerHolder(view);
		}

		@Override
		public void onBindViewHolder(PlayerHolder holder, int position) {
			holder.bind(players[position]);
		}

		void setItems(PlayerSlotPresenter[] items) {
			// TODO use diffutil
			this.players = items;
			notifyDataSetChanged();
		}
	}

	class PlayerHolder extends RecyclerView.ViewHolder implements PlayerSlotView {
		private PlayerSlotPresenter presenter;

		private final TextView playerNameTextView;
		private final SwitchCompat readySwitch;
		private final Spinner civilisationSpinner;
		private final Spinner playerTypeSpinner;
		private final Spinner startPositionSpinner;
		private final Spinner teamSpinner;

		private ArrayAdapter<Civilisation> civilisationsAdapter;
		private ArrayAdapter<PlayerType> playerTypesAdapter;
		private ArrayAdapter<StartPosition> startPositionsAdapter;
		private ArrayAdapter<Team> teamsAdapter;

		PlayerHolder(View itemView) {
			super(itemView);
			this.playerNameTextView = itemView.findViewById(R.id.text_view_player_name);
			this.readySwitch = itemView.findViewById(R.id.switch_ready);
			this.civilisationSpinner = itemView.findViewById(R.id.spinner_civilisation);
			this.playerTypeSpinner = itemView.findViewById(R.id.spinner_type);
			this.startPositionSpinner = itemView.findViewById(R.id.spinner_slot);
			this.teamSpinner = itemView.findViewById(R.id.spinner_team);

			readySwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
				presenter.readyChanged(checked);
			});

			civilisationSpinner.setOnItemSelectedListener(new SpinnerListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
					Civilisation civilisation = civilisationsAdapter.getItem(position);
					presenter.setCivilisation(civilisation);
				}
			});

			playerTypeSpinner.setOnItemSelectedListener(new SpinnerListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
					PlayerType playerType = playerTypesAdapter.getItem(position);
					presenter.setPlayerType(playerType);
				}
			});

			startPositionSpinner.setOnItemSelectedListener(new SpinnerListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
					StartPosition slot = startPositionsAdapter.getItem(position);
					presenter.startPositionSelected(slot);
				}
			});

			teamSpinner.setOnItemSelectedListener(new SpinnerListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
					Team team = teamsAdapter.getItem(position);
					presenter.teamSelected(team);
				}
			});
		}

		@Override
		public void setName(String name) {
			playerNameTextView.setText(name);
		}

		@Override
		public void setReady(boolean ready) {
			readySwitch.setChecked(ready);
		}

		@Override
		public void setPossibleCivilisations(Civilisation[] possibleCivilisations) {
			civilisationsAdapter = getSpinnerAdapter(possibleCivilisations);
			civilisationSpinner.setAdapter(civilisationsAdapter);
		}

		@Override
		public void setCivilisation(Civilisation civilisation) {
			civilisationSpinner.setSelection(civilisationsAdapter.getPosition(civilisation));
		}

		@Override
		public void setPossiblePlayerTypes(PlayerType[] ePlayerTypes) {
			playerTypesAdapter = getSpinnerAdapter(ePlayerTypes);
			playerTypeSpinner.setAdapter(playerTypesAdapter);
		}

		@Override
		public void setPlayerType(PlayerType playerType) {
			playerTypeSpinner.setSelection(playerTypesAdapter.getPosition(playerType));

		}

		@Override
		public void setPossibleStartPositions(StartPosition[] possibleSlots) {
			startPositionsAdapter = getSpinnerAdapter(possibleSlots);
			startPositionSpinner.setAdapter(startPositionsAdapter);
		}

		@Override
		public void setStartPosition(StartPosition slot) {
			startPositionSpinner.setSelection(startPositionsAdapter.getPosition(slot));
		}

		@Override
		public void setPossibleTeams(Team[] possibleTeams) {
			teamsAdapter = getSpinnerAdapter(possibleTeams);
			teamSpinner.setAdapter(teamsAdapter);
		}

		@Override
		public void setTeam(Team team) {
			teamSpinner.setSelection(teamsAdapter.getPosition(team));
		}

		@Override
		public void showReadyControl() {
			readySwitch.setVisibility(View.VISIBLE);
		}

		@Override
		public void hideReadyControl() {
			readySwitch.setVisibility(View.GONE);
		}

		@Override
		public void setControlsEnabled() {
			readySwitch.setClickable(true);
			readySwitch.setFocusable(true);
			playerTypeSpinner.setEnabled(true);
			civilisationSpinner.setEnabled(true);
			startPositionSpinner.setEnabled(true);
			teamSpinner.setEnabled(true);
		}

		@Override
		public void setControlsDisabled() {
			readySwitch.setClickable(false);
			readySwitch.setFocusable(false);
			playerTypeSpinner.setEnabled(false);
			civilisationSpinner.setEnabled(false);
			startPositionSpinner.setEnabled(false);
			teamSpinner.setEnabled(false);
		}

		void bind(PlayerSlotPresenter playerSlotPresenter) {
			this.presenter = playerSlotPresenter;
			playerSlotPresenter.initView(this);
		}
	}

	private abstract class SpinnerListener implements AdapterView.OnItemSelectedListener {
		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			// No op
		}
	}
}
