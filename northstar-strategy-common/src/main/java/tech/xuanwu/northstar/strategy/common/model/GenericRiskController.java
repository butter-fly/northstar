package tech.xuanwu.northstar.strategy.common.model;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import tech.xuanwu.northstar.strategy.common.RiskControlRule;
import tech.xuanwu.northstar.strategy.common.RiskController;
import tech.xuanwu.northstar.strategy.common.annotation.StrategicComponent;
import tech.xuanwu.northstar.strategy.common.constants.ModuleState;
import tech.xuanwu.northstar.strategy.common.constants.RiskAuditResult;
import tech.xuanwu.northstar.strategy.common.event.ModuleEvent;
import tech.xuanwu.northstar.strategy.common.event.ModuleEventBus;
import tech.xuanwu.northstar.strategy.common.event.ModuleEventType;
import xyz.redtorch.pb.CoreEnum.OffsetFlagEnum;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreField.TickField;

@Slf4j
public class GenericRiskController implements RiskController{
	
	private ModuleAgent agent;
	
	private ModuleEventBus moduleEventBus;
	
	private List<RiskControlRule> rules;
	
	private SubmitOrderReqField currentOrderReq;
	
	public GenericRiskController(List<RiskControlRule> rules) {
		this.rules = rules;
	}

	@Override
	public void onEvent(ModuleEvent event) {
		if(event.getEventType() == ModuleEventType.ORDER_REQ_CREATED) {
			currentOrderReq = (SubmitOrderReqField) event.getData();
			rules.stream().forEach(rule -> rule.onSubmitOrderReq(currentOrderReq));
		}
	}

	@Override
	public void setEventBus(ModuleEventBus moduleEventBus) {
		this.moduleEventBus = moduleEventBus;
	}

	@Override
	public void onTick(TickField tick) {
		short result = RiskAuditResult.ACCEPTED;
		if(agent.getModuleState() == ModuleState.PLACING_ORDER) {
			// 只有开仓请求才需要风控审核
			if(currentOrderReq.getOffsetFlag() == OffsetFlagEnum.OF_Open) {				
				for(RiskControlRule rule : rules) {
					result |= rule.canDeal(tick, agent);
				}
			}
			if((result & RiskAuditResult.REJECTED) > 0) {
				rejectOrder();
				explain(tick, result);
			} else if(result == RiskAuditResult.ACCEPTED) {
				approveOrder();
			} else {
				explain(tick, result);
			}
		} else if(agent.getModuleState() == ModuleState.PENDING_ORDER) {
			// 不管开仓还是平仓都需要检查是否需要追单
			for(RiskControlRule rule : rules) {
				result |= rule.canDeal(tick, agent);
			}
			if((result & RiskAuditResult.RETRY) > 0) {
				retryOrder();
				explain(tick, result);
			} 
		}
	}
	
	private void explain(TickField tick, short result) {
		log.warn("模组-[{}]，风控状态码为{}", agent.getName(), result);
		for(RiskControlRule rule : rules) {
			String name = rule.getClass().getAnnotation(StrategicComponent.class).value();
			log.warn("风控规则-[{}]，状态码为{}", name, rule.canDeal(tick, agent));
		}
	}

	@Override
	public void approveOrder() {
		log.info("通过风控审核，转发下单请求");
		moduleEventBus.post(ModuleEvent.builder()
				.eventType(ModuleEventType.ORDER_REQ_ACCEPTED)
				.data(currentOrderReq)
				.build());
	}

	@Override
	public void rejectOrder() {
		log.warn("模组-[{}]，下单请求被风控拒绝：{}", agent.getName(), currentOrderReq);
		moduleEventBus.post(ModuleEvent.builder()
				.eventType(ModuleEventType.ORDER_REQ_REJECTED)
				.data(currentOrderReq)
				.build());
	}

	@Override
	public void retryOrder() {
		log.info("风控模块发出追单信号");
		moduleEventBus.post(ModuleEvent.builder()
				.eventType(ModuleEventType.ORDER_RETRY)
				.data(currentOrderReq)
				.build());
	}

	@Override
	public void setModuleAgent(ModuleAgent agent) {
		this.agent = agent;
	}

}
