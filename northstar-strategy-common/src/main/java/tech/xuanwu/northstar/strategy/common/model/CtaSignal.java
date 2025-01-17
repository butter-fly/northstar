package tech.xuanwu.northstar.strategy.common.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import tech.xuanwu.northstar.strategy.common.Signal;
import tech.xuanwu.northstar.strategy.common.SignalPolicy;
import tech.xuanwu.northstar.strategy.common.constants.SignalState;

@Builder
@Data
public class CtaSignal implements Signal{
	
	/**
	 * 信号ID
	 */
	private UUID id;
	/**
	 * 信号状态
	 */
	private SignalState state;
	/**
	 * 信号价格
	 */
	private double signalPrice;
	/**
	 * 信号产生时间
	 */
	private long timestamp;
	/**
	 * 关联信号策略
	 */
	private Class<? extends SignalPolicy> signalClass;
	
}
