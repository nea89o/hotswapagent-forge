package moe.nea.hotswapagentforge.forge;

/**
 * Called when a hotswap is finished. This isn't a functionality the Hotswap Agent enables, since it has no concept of
 * an entire hotswap, but rather is fired shortly after there are no more new redefinitions. In theory this event could
 * be implemented by just subscribing to {@link ClassDefinitionEvent.Redefinition} and waiting about 500 milliseconds to
 * be elapsed, resetting that timer with each new event, however this event is called slightly earlier due to the timer
 * starting upon the event being queued by Hotswap Agent, instead of after the event has been dispatched first by
 * Hotswap Agent and then by Forge, which adds additional delay.
 */
public class HotswapFinishedEvent extends HotswapEvent {
    @Override
    public String toString() {
        return "HotswapFinishedEvent";
    }
}
