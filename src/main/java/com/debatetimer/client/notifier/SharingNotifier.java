package com.debatetimer.client.notifier;

import com.debatetimer.event.sharing.SharingFinishedEvent;

public interface SharingNotifier {

    void sendSharingFinished(SharingFinishedEvent event);
}
