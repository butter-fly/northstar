package tech.xuanwu.northstar.gateway.api;

import xyz.redtorch.pb.CoreField.ContractField;

public interface MarketGateway extends Gateway {

	/**
	 * 订阅
	 * 
	 * @param subscribeReq
	 */
	boolean subscribe(ContractField contract);

	/**
	 * 退订
	 * 
	 * @param subscribeReq
	 */
	boolean unsubscribe(ContractField contract);
	
	/**
	 * 检测是否有行情数据
	 * @return
	 */
	boolean isActive();
}
