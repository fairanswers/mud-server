package mud.api;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

import java.util.List;

import mud.objects.Player;

public interface MUDServerAPI {
	public List<Player> getPlayers();
}