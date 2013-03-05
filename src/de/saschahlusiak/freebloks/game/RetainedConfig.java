package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.view.model.Sounds;

class RetainedConfig {
	SpielClientThread clientThread;
	NET_SERVER_STATUS lastStatus;
	Sounds soundPool;
}