package tech.xuanwu.northstar.domain.account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import tech.xuanwu.northstar.common.exception.InsufficientException;
import tech.xuanwu.northstar.common.exception.NoSuchElementException;
import tech.xuanwu.northstar.common.model.ContractManager;
import tech.xuanwu.northstar.common.model.OrderRequest;
import tech.xuanwu.northstar.common.utils.OrderUtil;
import xyz.redtorch.pb.CoreEnum.ContingentConditionEnum;
import xyz.redtorch.pb.CoreEnum.DirectionEnum;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreEnum.ForceCloseReasonEnum;
import xyz.redtorch.pb.CoreEnum.HedgeFlagEnum;
import xyz.redtorch.pb.CoreEnum.OffsetFlagEnum;
import xyz.redtorch.pb.CoreEnum.OrderPriceTypeEnum;
import xyz.redtorch.pb.CoreEnum.PositionDirectionEnum;
import xyz.redtorch.pb.CoreEnum.TimeConditionEnum;
import xyz.redtorch.pb.CoreEnum.VolumeConditionEnum;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

/**
 * 持仓信息描述
 * @author KevinHuangwl
 *
 */
public class PositionDescription {

	/**
	 * 数据结构
	 * symbol:	{ [0]空头持仓， [1]多头持仓 }
	 */
	protected ConcurrentHashMap<String, PositionField[]> posMap = new ConcurrentHashMap<>();
	
	protected ContractManager contractMgr;
	
	public PositionDescription(ContractManager contractMgr) {
		this.contractMgr = contractMgr;
	}
	
	/**
	 * 更新持仓
	 * @param pos
	 */
	public void update(PositionField pos) {
		String unifiedSymbol = pos.getContract().getUnifiedSymbol();
		if(!posMap.containsKey(unifiedSymbol)) {
			posMap.put(unifiedSymbol, new PositionField[2]);			
		}
		if(pos.getPositionDirection() == PositionDirectionEnum.PD_Long) {
			posMap.get(unifiedSymbol)[1] = pos;
		} else if(pos.getPositionDirection() == PositionDirectionEnum.PD_Short) {
			posMap.get(unifiedSymbol)[0] = pos;
		}
	}
	
	private PositionField acquireTargetPosition(OrderRequest orderReq) {
		if(!OrderUtil.isClosingOrder(orderReq.getTradeOpr())) {
			throw new IllegalStateException("该委托并非平仓委托");
		}
		DirectionEnum orderDir = OrderUtil.resolveDirection(orderReq.getTradeOpr());
		PositionDirectionEnum targetPosDir = OrderUtil.getClosingDirection(orderDir);
		int i = targetPosDir == PositionDirectionEnum.PD_Long ? 1 : targetPosDir == PositionDirectionEnum.PD_Short ? 0 : -1;
		if(!posMap.containsKey(orderReq.getContractUnifiedSymbol()) || posMap.get(orderReq.getContractUnifiedSymbol())[i] == null) {
			throw new NoSuchElementException("找不到可平仓的持仓");
		}
		return posMap.get(orderReq.getContractUnifiedSymbol())[i];
	}
	
	/**
	 * 生成平仓请求
	 * @param orderReq
	 * @return
	 * @throws InsufficientException 
	 */
	public List<SubmitOrderReqField> generateCloseOrderReq(OrderRequest orderReq) throws InsufficientException{
		ContractField contract = contractMgr.getContract(orderReq.getContractUnifiedSymbol());
		if(contract == null) {
			throw new NoSuchElementException("不存在此合约：" + orderReq.getContractUnifiedSymbol());
		}
		PositionField pos = acquireTargetPosition(orderReq);
		int totalAvailable = pos.getPosition() - pos.getFrozen();
		int tdAvailable = pos.getTdPosition() - pos.getTdFrozen();
		if(totalAvailable < orderReq.getVolume()) {
			throw new InsufficientException("持仓不足，无法平仓");
		}
		DirectionEnum orderDir = OrderUtil.resolveDirection(orderReq.getTradeOpr());
		List<SubmitOrderReqField> result = new ArrayList<>();
		SubmitOrderReqField.Builder sb = SubmitOrderReqField.newBuilder();
		sb.setContract(contract)
		.setPrice(Double.parseDouble(orderReq.getPrice()))
		.setStopPrice(StringUtils.isNotBlank(orderReq.getStopPrice()) ? Double.parseDouble(orderReq.getStopPrice()) : 0D)
		.setOrderPriceType(OrderPriceTypeEnum.OPT_LimitPrice)
		.setDirection(orderDir)
		.setHedgeFlag(HedgeFlagEnum.HF_Speculation)
		.setTimeCondition(TimeConditionEnum.TC_GFD)
		.setVolumeCondition(VolumeConditionEnum.VC_AV)
		.setForceCloseReason(ForceCloseReasonEnum.FCR_NotForceClose)
		.setContingentCondition(ContingentConditionEnum.CC_Immediately)
		.setMinVolume(1)
		.setGatewayId(orderReq.getGatewayId());
		
		if(pos.getContract().getExchange() == ExchangeEnum.SHFE && tdAvailable > 0) {
			if(tdAvailable >= orderReq.getVolume()) {
				result.add(sb.setVolume(orderReq.getVolume())
						.setOffsetFlag(OffsetFlagEnum.OF_CloseToday)
						.build());
				return result;
			}
			
			result.add(sb.setVolume(tdAvailable)
					.setOffsetFlag(OffsetFlagEnum.OF_CloseToday)
					.build());
			result.add(sb.setVolume(orderReq.getVolume() - tdAvailable)
					.setOffsetFlag(OffsetFlagEnum.OF_CloseYesterday)
					.build());
			return result;
		}
		
		result.add(sb.setVolume(orderReq.getVolume())
				.setOffsetFlag(OffsetFlagEnum.OF_Close)
				.build());
		return result;
	}
	
	public List<PositionField> getPositions(){
		List<PositionField> result = new ArrayList<>(posMap.size() * 2);
		posMap.forEach((k,v) -> {
			for(PositionField p : v) {
				if(p != null) {
					result.add(p);
				}
			}
		});
		return Collections.unmodifiableList(result);
	}
}
