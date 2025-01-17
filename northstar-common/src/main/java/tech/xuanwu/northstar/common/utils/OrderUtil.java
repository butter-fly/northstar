package tech.xuanwu.northstar.common.utils;

import tech.xuanwu.northstar.common.model.OrderRequest.TradeOperation;
import xyz.redtorch.pb.CoreEnum.DirectionEnum;
import xyz.redtorch.pb.CoreEnum.OffsetFlagEnum;
import xyz.redtorch.pb.CoreEnum.PositionDirectionEnum;

public class OrderUtil {
	
	public static DirectionEnum resolveDirection(TradeOperation opr) {
		return opr.toString().charAt(0) == 'B' ? DirectionEnum.D_Buy : DirectionEnum.D_Sell;
	}
	
	public static boolean isOpenningOrder(TradeOperation opr) {
		return opr.toString().charAt(1) == 'K';
	}
	
	public static boolean isClosingOrder(TradeOperation opr) {
		return opr.toString().charAt(1) == 'P';
	}
	
	public static PositionDirectionEnum getClosingDirection(DirectionEnum dir) {
		if(dir == DirectionEnum.D_Buy) {
			return PositionDirectionEnum.PD_Short;
		} else if(dir == DirectionEnum.D_Sell) {
			return PositionDirectionEnum.PD_Long;
		} 
		
		throw new IllegalArgumentException("无法确定[" + dir + "]的对应持仓方向");
	}

	public static OffsetFlagEnum resolveOffsetFlag(TradeOperation opr) {
		return null;
	}
}
