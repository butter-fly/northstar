package tech.xuanwu.northstar.strategy.cta.module.risk;

import tech.xuanwu.northstar.strategy.common.RiskControlRule;
import tech.xuanwu.northstar.strategy.common.Signal;
import tech.xuanwu.northstar.strategy.common.annotation.Label;
import tech.xuanwu.northstar.strategy.common.annotation.StrategicComponent;
import tech.xuanwu.northstar.strategy.common.constants.RiskAuditResult;
import tech.xuanwu.northstar.strategy.common.model.ModuleAgent;
import tech.xuanwu.northstar.strategy.common.model.meta.DynamicParams;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreField.TickField;

@StrategicComponent("日内开仓次数限制")
public class DailyDealLimitedRule implements RiskControlRule {
	
	protected int dailyDealLimit;
	
	protected Signal curSignal;
	

	@Override
	public short canDeal(TickField tick, ModuleAgent agent) {
		if(agent.numOfOpeningForToday() < dailyDealLimit) {
			return RiskAuditResult.ACCEPTED;
		}
		return RiskAuditResult.REJECTED;
	}
	
	@Override
	public RiskControlRule onSubmitOrderReq(SubmitOrderReqField orderReq) {
		//该风控规则不需要参考订单状态，不需要做任何处理
		return this;
	}
	
	@Override
	public DynamicParams getDynamicParams() {
		return new InitParams();
	}

	@Override
	public void initWithParams(DynamicParams params) {
		InitParams initParams = (InitParams) params;
		this.dailyDealLimit = initParams.dailyDealLimit;
	}
	
	public static class InitParams extends DynamicParams{
		
		@Label(value="日内开仓限制", unit="次")
		private int dailyDealLimit;
		
	}

}
