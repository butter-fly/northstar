package tech.xuanwu.northstar.strategy.common.model;

import lombok.Builder;
import lombok.Data;
import xyz.redtorch.pb.CoreEnum.PositionDirectionEnum;

@Builder
@Data
public class DealRecord {
	
	private String contractName;
	
	private PositionDirectionEnum direction;
	
	private String tradingDay;
	
	private long dealTimestamp;
	
	private int volume;
	
	private double openPrice;
	
	private double closePrice;
	
	private int closeProfit;
	
}
