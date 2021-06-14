package tech.xuanwu.northstar.strategy.common.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lombok.Builder;
import tech.xuanwu.northstar.gateway.api.Gateway;
import tech.xuanwu.northstar.strategy.common.Dealer;
import tech.xuanwu.northstar.strategy.common.ModuleAccount;
import tech.xuanwu.northstar.strategy.common.ModuleOrder;
import tech.xuanwu.northstar.strategy.common.ModulePosition;
import tech.xuanwu.northstar.strategy.common.ModuleTrade;
import tech.xuanwu.northstar.strategy.common.RiskControlRule;
import tech.xuanwu.northstar.strategy.common.SignalPolicy;
import tech.xuanwu.northstar.strategy.common.constants.ModuleState;
import tech.xuanwu.northstar.strategy.common.model.data.BarData;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;

@Builder
public class StrategyModule {
	/**
	 * unifiedSymbol -> barData
	 */
	private Map<String,BarData> barDataMap;
	
	private ModuleAccount mAccount;
	
	private ModulePosition mPosition;
	
	private ModuleOrder mOrder;
	
	private ModuleTrade mTrade;
	
	private SignalPolicy signalPolicy;
	
	private List<RiskControlRule> riskControlRules;
	
	private Dealer dealer;
	
	private ModuleState state;
	
	private Gateway gateway;
	
	private String name;
	
	private boolean enabled;
	
	public void onTick(TickField tick) {
		
	}
	
	public void onBar(BarField bar) {
		
	}
	
	public void onOrder(OrderField order) {
		
	}
	
	public void onTrade(TradeField trade) {
		
	}
	
	public void onAccount(AccountField account) {
		
	}
	
	public String getName() {
		return name;
	}
	
	public ModuleState getState() {
		return state;
	}
	
	public void toggleRunningState() {
		enabled = !enabled;
	}
	
	public ModuleStatus getModuleStatus() {
		ModuleStatus status = new ModuleStatus();
		status.setModuleName(name);
		status.setState(state);
		List<TradeField> trades = mPosition.getOpenningTrade();
		if(trades.size() > 0) {
			status.setLastOpenTrade(trades.stream().map(trade -> trade.toByteArray()).collect(Collectors.toList()));
		}
		return status;
	}
	
	public ModulePerformance getPerformance() {
		ModulePerformance mp = new ModulePerformance();
		mp.setModuleName(name);
		Map<String, List<byte[]>> byteMap = new HashMap<>();
		for(Entry<String, BarData> e : barDataMap.entrySet()) {
			byteMap.put(e.getKey(), e.getValue().getRefBarList().stream().map(bar -> bar.toByteArray()).collect(Collectors.toList()));
		}
		mp.setRefBarDataMap(byteMap);
		mp.setTotalProfit(mTrade.getTotalProfit());
		mp.setDealRecords(mTrade.getDealRecords());
		return mp;
	}
}