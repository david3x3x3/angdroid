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

package com.scoreloop.client.android.ui.component.challenge;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import org.angdroid.angband.R;
import com.scoreloop.client.android.ui.component.base.ComponentHeaderActivity;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.component.base.StringFormatter;
import com.scoreloop.client.android.ui.framework.ValueStore;

public class ChallengeHeaderActivity extends ComponentHeaderActivity implements OnClickListener {

	@Override
	public void onClick(final View view) {
		if (getSession().isAuthenticated()) {
			display(getFactory().createChallengePaymentScreenDescription());
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.sl_header_default);

		setCaption(getGame().getName());

		getImageView().setImageDrawable(getResources().getDrawable(R.drawable.sl_header_icon_challenges));
		setTitle(getResources().getString(R.string.sl_challenges));
		showControlIcon(R.drawable.sl_button_add_coins);

		addObservedKeys(ValueStore.concatenateKeys(Constant.USER_VALUES, Constant.USER_BALANCE));
	}

	private void showControlIcon(final int resId) {
		final ImageView icon = (ImageView) findViewById(R.id.sl_control_icon);
		icon.setImageResource(resId);
		icon.setEnabled(true);
		icon.setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		getUserValues().setDirty(Constant.USER_BALANCE);
	}

	@Override
	public void onValueChanged(final ValueStore valueStore, final String key, final Object oldValue, final Object newValue) {
		setSubTitle(StringFormatter.getBalanceSubTitle(this, getUserValues(), getConfiguration()));
	}

	@Override
	public void onValueSetDirty(final ValueStore valueStore, final String key) {
		if (Constant.USER_BALANCE.equals(key)) {
			getUserValues().retrieveValue(key, ValueStore.RetrievalMode.NOT_DIRTY, null);
		}
	}
}
