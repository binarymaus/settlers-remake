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

package jsettlers.main.android.gameplay.controlsmenu.selection;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import jsettlers.common.action.AskCastSpellAction;
import jsettlers.common.action.IAction;
import jsettlers.common.movable.ESpellType;
import jsettlers.common.player.IInGamePlayer;
import jsettlers.common.player.IMannaInformation;
import jsettlers.common.player.IPlayer;
import jsettlers.graphics.localization.Labels;
import jsettlers.main.android.R;
import jsettlers.main.android.core.controls.ActionControls;
import jsettlers.main.android.core.controls.ActionListener;
import jsettlers.main.android.core.controls.ControlsResolver;
import jsettlers.main.android.core.controls.TaskControls;
import jsettlers.main.android.core.events.DrawEvents;
import jsettlers.main.android.core.resources.OriginalImageProvider;
import jsettlers.main.android.gameplay.navigation.MenuNavigator;

@EFragment(R.layout.menu_selection_priests)
public class PriestsSelectionFragment extends SelectionFragment implements ActionListener {
	public static PriestsSelectionFragment newInstance() {
		return new PriestsSelectionFragment_();
	}

	@ViewById(R.id.recyclerView)
	RecyclerView recyclerView;

	private ActionControls actionControls;
	private TaskControls taskControls;
	private MenuNavigator menuNavigator;

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ControlsResolver controlsResolver = new ControlsResolver(getActivity());
		actionControls = controlsResolver.getActionControls();
		taskControls = controlsResolver.getTaskControls();
		menuNavigator = (MenuNavigator) getParentFragment();

		IPlayer player = getSelection().get(0).getPlayer();
		SpellAdapter productionAdapter = new SpellAdapter(getActivity(), player);
		recyclerView.setHasFixedSize(true);
		recyclerView.setAdapter(productionAdapter);

		new DrawEvents(controlsResolver.getDrawControls()).observe(this, x -> {
			if(!(player instanceof IInGamePlayer)) return;
			IMannaInformation mannaInfo = ((IInGamePlayer)player).getMannaInformation();

			int count = recyclerView.getChildCount();
			for(int i = 0; i < count; i++) {
				((SpellViewHolder)recyclerView.getChildViewHolder(recyclerView.getChildAt(i))).update(mannaInfo);
			}
		});

		actionControls.addActionListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		actionControls.removeActionListener(this);
	}

	private Snackbar snackbar;

	@Override
	public void actionFired(IAction action) {
		switch (action.getActionType()) {
			case ASK_CAST_SPELL:
				snackbar = Snackbar
						.make(getView(), R.string.ask_spell, Snackbar.LENGTH_INDEFINITE)
						.setAction(R.string.cancel, view -> taskControls.endTask());
				snackbar.show();
				break;
			case CAST_SPELL:
			case ABORT:
				dismissSnackbar();
				break;
		}
	}

	private void dismissSnackbar() {
		if (snackbar != null) {
			snackbar.dismiss();
			snackbar = null;
		}
	}

	/**
	 * RecyclerView adapter
	 */
	private class SpellAdapter extends RecyclerView.Adapter<SpellViewHolder> {

		private final LayoutInflater layoutInflater;

		private final ESpellType[] spells;

		SpellAdapter(Activity activity, IPlayer player) {
			layoutInflater = LayoutInflater.from(activity);
			spells = ESpellType.getSpellsForCivilisation(player.getCivilisation());
		}

		@NonNull
		@Override
		public SpellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = layoutInflater.inflate(R.layout.view_spell, parent, false);
			return new SpellViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull SpellViewHolder holder, int position) {
			holder.bind(spells[position]);
		}

		@Override
		public int getItemCount() {
			return spells.length;
		}
	}

	private class SpellViewHolder extends RecyclerView.ViewHolder {

		private final ImageView imageView;
		private final Button spellName;
		private final TextView spellCost;
		private ESpellType spell;

		SpellViewHolder(View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.image_view_spell);
			spellName = itemView.findViewById(R.id.text_view_spell_name);
			spellCost = itemView.findViewById(R.id.text_view_spell_cost);

			spellName.setOnClickListener(v -> {
				actionControls.fireAction(new AskCastSpellAction(this.spell));
				menuNavigator.dismissMenu();
			});
		}

		void bind(ESpellType spell) {
			this.spell = spell;

			OriginalImageProvider.get(spell.getImageLink()).setAsImage(imageView);
			spellName.setText(Labels.getName(spell));
			update(null);
		}

		void update(@Nullable IMannaInformation mannaInfo) {
			int cost = (mannaInfo != null)? mannaInfo.getSpellCost(spell) : -1;

			spellCost.setText(Labels.getString("spell_cost", cost));
			spellName.setEnabled(mannaInfo!=null&&mannaInfo.canUseSpell(spell));
		}
	}
}
