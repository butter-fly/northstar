package tech.xuanwu.northstar.domain.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import tech.xuanwu.northstar.common.exception.InsufficientException;
import tech.xuanwu.northstar.common.model.ContractManager;
import tech.xuanwu.northstar.common.model.OrderRequest;
import tech.xuanwu.northstar.common.model.OrderRequest.TradeOperation;
import tech.xuanwu.northstar.domain.account.PositionDescription;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreEnum.OffsetFlagEnum;
import xyz.redtorch.pb.CoreEnum.PositionDirectionEnum;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

public class PositionDescriptionTest {
	
	PositionDescription pd;
	ContractField contract = ContractField.newBuilder()
			.setContractId("rb2102@SHFE")
			.setExchange(ExchangeEnum.SHFE)
			.setGatewayId("testGateway")
			.setSymbol("rb2102")
			.setUnifiedSymbol("rb2102@SHFE")
			.setMultiplier(10)
			.setLongMarginRatio(0.08)
			.setShortMarginRatio(0.08)
			.build();
	ContractField contract2 = ContractField.newBuilder()
			.setContractId("AP2102@ZCE")
			.setExchange(ExchangeEnum.CZCE)
			.setGatewayId("testGateway")
			.setSymbol("AP2102")
			.setUnifiedSymbol("AP2102@CZCE")
			.setMultiplier(10)
			.setLongMarginRatio(0.08)
			.setShortMarginRatio(0.08)
			.build();
	
	@Before
	public void prepare() {
		pd = new PositionDescription(mock(ContractManager.class));
		when(pd.contractMgr.getContract("AP2102@CZCE")).thenReturn(contract2);
		when(pd.contractMgr.getContract("rb2102@SHFE")).thenReturn(contract);
	}

	@Test
	public void testUpdate() {
		
		PositionField pf = PositionField.newBuilder()
				.setAccountId("testGateway")
				.setContract(contract)
				.setFrozen(2)
				.setPosition(5)
				.setTdPosition(3)
				.setTdFrozen(1)
				.setYdPosition(2)
				.setYdFrozen(1)
				.setPrice(1234)
				.setPositionDirection(PositionDirectionEnum.PD_Long)
				.build();
		
		PositionField pf2 = PositionField.newBuilder()
				.setAccountId("testGateway")
				.setContract(contract2)
				.setFrozen(2)
				.setPosition(5)
				.setTdPosition(3)
				.setTdFrozen(1)
				.setYdPosition(2)
				.setYdFrozen(1)
				.setPrice(1234)
				.setPositionDirection(PositionDirectionEnum.PD_Long)
				.build();
		
		pd.update(pf);
		pd.update(pf2);
		assertThat(pd.getPositions().size()).isEqualTo(2);
	}

	@Test
	public void testGenerateCloseOrderReqForSHFE() throws InsufficientException {
		testUpdate();
		
		OrderRequest orderReq = OrderRequest.builder()
				.contractUnifiedSymbol("rb2102@SHFE")
				.gatewayId("testGateway")
				.price("1234")
				.volume(1)
				.tradeOpr(TradeOperation.SP)
				.build();
		
		List<SubmitOrderReqField> result = pd.generateCloseOrderReq(orderReq);
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0).getOffsetFlag()).isEqualTo(OffsetFlagEnum.OF_CloseToday);
	}
	
	@Test
	public void testGenerateCloseOrderReqForSHFE2() throws InsufficientException {
		testUpdate();
		
		OrderRequest orderReq = OrderRequest.builder()
				.contractUnifiedSymbol("rb2102@SHFE")
				.gatewayId("testGateway")
				.price("1234")
				.volume(3)
				.tradeOpr(TradeOperation.SP)
				.build();
		
		List<SubmitOrderReqField> result = pd.generateCloseOrderReq(orderReq);
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0).getOffsetFlag()).isEqualTo(OffsetFlagEnum.OF_CloseToday);
		assertThat(result.get(1).getOffsetFlag()).isEqualTo(OffsetFlagEnum.OF_CloseYesterday);
	}
	
	@Test
	public void testGenerateCloseOrderReq() throws InsufficientException {
		testUpdate();
		
		OrderRequest orderReq = OrderRequest.builder()
				.contractUnifiedSymbol("AP2102@CZCE")
				.gatewayId("testGateway")
				.price("1234")
				.volume(1)
				.tradeOpr(TradeOperation.SP)
				.build();
		
		List<SubmitOrderReqField> result = pd.generateCloseOrderReq(orderReq);
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0).getOffsetFlag()).isEqualTo(OffsetFlagEnum.OF_Close);
	}
	
	@Test
	public void testGenerateCloseOrderReq2() throws InsufficientException {
		testUpdate();
		
		OrderRequest orderReq = OrderRequest.builder()
				.contractUnifiedSymbol("AP2102@CZCE")
				.gatewayId("testGateway")
				.price("1234")
				.volume(3)
				.tradeOpr(TradeOperation.SP)
				.build();
		
		List<SubmitOrderReqField> result = pd.generateCloseOrderReq(orderReq);
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0).getOffsetFlag()).isEqualTo(OffsetFlagEnum.OF_Close);
	}

	@Test(expected = InsufficientException.class)
	public void testGenerateCloseOrderReqWithException1() throws InsufficientException {
		testUpdate();
		
		OrderRequest orderReq = OrderRequest.builder()
				.contractUnifiedSymbol("rb2102@SHFE")
				.gatewayId("testGateway")
				.price("1234")
				.volume(4)
				.tradeOpr(TradeOperation.SP)
				.build();
		
		pd.generateCloseOrderReq(orderReq);
	}
	
}
