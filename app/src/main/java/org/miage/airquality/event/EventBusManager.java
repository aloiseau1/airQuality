package org.miage.airquality.event;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

// Gestion des requête asynchrone
public class EventBusManager {

    public static Bus BUS = new Bus(ThreadEnforcer.ANY);
}
