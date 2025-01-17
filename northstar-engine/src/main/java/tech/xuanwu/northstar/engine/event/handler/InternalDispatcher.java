package tech.xuanwu.northstar.engine.event.handler;

import java.util.HashSet;
import java.util.Set;

import tech.xuanwu.northstar.common.event.InternalEventBus;
import tech.xuanwu.northstar.common.event.NorthstarEvent;
import tech.xuanwu.northstar.common.event.NorthstarEventType;
import tech.xuanwu.northstar.engine.event.FastEventEngine.NorthstarEventDispatcher;

public class InternalDispatcher implements NorthstarEventDispatcher {

	private InternalEventBus eb;
	
	private Set<NorthstarEventType> canHandleEvents = new HashSet<>() {
		private static final long serialVersionUID = 1L;

		{
			add(NorthstarEventType.TICK);
			add(NorthstarEventType.LOGGED_IN);
			add(NorthstarEventType.LOGGING_IN);
			add(NorthstarEventType.LOGGED_OUT);
			add(NorthstarEventType.LOGGING_OUT);
			add(NorthstarEventType.ACCOUNT);
			add(NorthstarEventType.POSITION);
			add(NorthstarEventType.TRADE);
			add(NorthstarEventType.ORDER);
			add(NorthstarEventType.CONNECTING);
			add(NorthstarEventType.CONNECTED);
			add(NorthstarEventType.DISCONNECTED);
			add(NorthstarEventType.DISCONNECTING);
			add(NorthstarEventType.IDX_CONTRACT);
			add(NorthstarEventType.CONTRACT);
			add(NorthstarEventType.CONTRACT_LOADED);
			add(NorthstarEventType.PLACE_ORDER);
			add(NorthstarEventType.WITHDRAW_ORDER);
		}
	};
	
	public InternalDispatcher(InternalEventBus eb){
		this.eb = eb;
	}
	
	@Override
	public void onEvent(NorthstarEvent event, long sequence, boolean endOfBatch) throws Exception {
		if(canHandleEvents.contains(event.getEvent())) {			
			eb.post(event);
		}
	}

}
