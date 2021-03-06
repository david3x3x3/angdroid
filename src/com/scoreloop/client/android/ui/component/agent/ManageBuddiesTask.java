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

package com.scoreloop.client.android.ui.component.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerException;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.model.Continuation;
import com.scoreloop.client.android.core.model.User;
import org.angdroid.angband.R;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.framework.BaseActivity;
import com.scoreloop.client.android.ui.framework.ValueStore;

public class ManageBuddiesTask implements RequestControllerObserver {

	private enum Mode {
		ADD, REMOVE
	}

	public static void addBuddies(final BaseActivity activity, final List<User> users, final ValueStore valueStore,
			final Continuation<Integer> continuation) {
		new ManageBuddiesTask(activity, Mode.ADD, users, valueStore, continuation);
	}

	public static void addBuddy(final BaseActivity activity, final User user, final ValueStore valueStore,
			final Continuation<Integer> continuation) {
		new ManageBuddiesTask(activity, Mode.ADD, Collections.singletonList(user), valueStore, continuation);
	}

	public static void removeBuddy(final BaseActivity activity, final User user, final ValueStore valueStore,
			final Continuation<Integer> continuation) {
		new ManageBuddiesTask(activity, Mode.REMOVE, Collections.singletonList(user), valueStore, continuation);
	}

	private final BaseActivity			_activity;
	private final Continuation<Integer>	_continuation;
	private final UserController		_controller;
	private int							_count;
	private final Mode					_mode;
	private final ValueStore			_sessionUserValues;
	private final List<User>			_users	= new ArrayList<User>();

	private ManageBuddiesTask(final BaseActivity activity, final Mode mode, final List<User> users, final ValueStore valueStore,
			final Continuation<Integer> continuation) {
		_activity = activity;
		_mode = mode;
		_users.addAll(users);
		_sessionUserValues = valueStore;
		_continuation = continuation;
		_controller = new UserController(this);

		processNextOrFinish();
	}

	private User popUser() {
		if (_users == null) {
			return null;
		}
		if (_users.isEmpty()) {
			return null;
		}
		return _users.remove(0);
	}

	private void processNextOrFinish() {
		final User sessionUser = ScoreloopManagerSingleton.get().getSession().getUser();
		final List<User> sessionUserBuddies = sessionUser.getBuddyUsers();
		User user;
		do {
			user = popUser();
		} while ((user != null)
				&& (sessionUser.equals(user) || ((sessionUserBuddies != null) && (((_mode == Mode.ADD) && sessionUserBuddies.contains(user)) || ((_mode == Mode.REMOVE) && !sessionUserBuddies
						.contains(user))))));

		if (user != null) {
			_controller.setUser(user);
			switch (_mode) {
			case ADD:
				_controller.addAsBuddy();
				break;

			case REMOVE:
				_controller.removeAsBuddy();
				break;
			}
			return;
		}

		// besides setting all value stores dirty, we also modify the value
		// immediately so that we minimize the time while the value is stale
		final Integer oldNumber = _sessionUserValues.getValue(Constant.NUMBER_BUDDIES);
		if (oldNumber != null) {
			final int newNumber = _mode == Mode.ADD ? oldNumber + _count : oldNumber - _count;
			_sessionUserValues.putValue(Constant.NUMBER_BUDDIES, newNumber);
		}

		_sessionUserValues.setAllDirty();

		if (_continuation != null) {
			_continuation.withValue(_count, null);
		}
	}

	@Override
	public void requestControllerDidFail(final RequestController aRequestController, final Exception anException) {
		// just work on remainder users
		if (anException instanceof RequestControllerException) {
			final RequestControllerException exception = (RequestControllerException) anException;
			final int code = exception.getErrorCode();
			switch (_mode) {
			case ADD:
				if (code == RequestControllerException.CODE_BUDDY_ADD_REQUEST_ALREADY_ADDED) {
					_activity.showToast(String.format(_activity.getString(R.string.sl_format_friend_already_added), _controller.getUser()
							.getDisplayName()));
				}
				break;
			case REMOVE:
				if (code == RequestControllerException.CODE_BUDDY_REMOVE_REQUEST_ALREADY_REMOVED) {
					_activity.showToast(String.format(_activity.getString(R.string.sl_format_friend_already_removed), _controller.getUser()
							.getDisplayName()));
				}
				break;
			}
		}
		processNextOrFinish();
	}

	@Override
	public void requestControllerDidReceiveResponse(final RequestController aRequestController) {
		++_count;
		processNextOrFinish();
	}
}
