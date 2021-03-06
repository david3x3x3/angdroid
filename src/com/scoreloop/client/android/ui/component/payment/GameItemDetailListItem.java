/*
 * In derogation of the Scoreloop SDK - License Agreement concluded between
 * Licensor and Licensee, as defined therein, the following conditions shall
 * apply for the source code contained below, whereas apart from that the
 * Scoreloop SDK - License Agreement shall remain unaffected.
 * 
 * Copyright: Scoreloop AG, Germany (Licensor)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.scoreloop.client.android.ui.component.payment;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scoreloop.client.android.core.model.GameItem;
import org.angdroid.angband.R;
import com.scoreloop.client.android.ui.component.base.ComponentActivity;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.component.base.StringFormatter;
import com.scoreloop.client.android.ui.framework.BaseListItem;

public class GameItemDetailListItem extends BaseListItem {

	private final GameItem	_gameItem;

	public GameItemDetailListItem(final ComponentActivity activity, final Drawable drawable, final GameItem game) {
		super(activity, drawable, game.getDescription());
		_gameItem = game;
	}

	public ComponentActivity getComponentActivity() {
		return (ComponentActivity) getContext();
	}

	@Override
	public int getType() {
		return Constant.LIST_ITEM_TYPE_GAME_DETAIL;
	}

	@Override
	public View getView(View view, final ViewGroup parent) {
		if (view == null) {
			view = getLayoutInflater().inflate(R.layout.sl_list_item_game_detail, null);
		}

		final TextView textView = (TextView) view.findViewById(R.id.sl_list_item_game_detail_text);
		final String description = _gameItem.getDescription();
		String text = "";
		if (description != null) {
			text += description;
		} else {
			text += _gameItem.getName(); // fall-back to name if we don't have a description
		}
		if (_gameItem.isCoinPack()) {
			text += "\n" + StringFormatter.formatMoney(_gameItem.getCoinPackValue(), getComponentActivity().getConfiguration());
		}
		textView.setText(text.replace("\r", ""));

		return view;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

}
