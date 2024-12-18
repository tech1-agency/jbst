package jbst.foundation.domain.concurrent;

import jbst.foundation.domain.time.SchedulerConfiguration;
import jbst.foundation.domain.time.TimeAmount;

public abstract class AbstractInfiniteTimerTask extends AbstractTimerTask {

    protected AbstractInfiniteTimerTask(
            SchedulerConfiguration interval
    ) {
        super(
                interval,
                TimeAmount.forever()
        );
    }

    @Override
    public void onComplete() {
        // ignored on infinite timer task
    }
}
