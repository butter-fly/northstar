package tech.xuanwu.northstar.handler.data;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import lombok.extern.slf4j.Slf4j;
import tech.xuanwu.northstar.common.event.AbstractEventHandler;
import tech.xuanwu.northstar.common.event.GenericEventHandler;
import tech.xuanwu.northstar.common.event.NorthstarEvent;
import tech.xuanwu.northstar.common.event.NorthstarEventType;
import tech.xuanwu.northstar.common.utils.BarGenerator;
import tech.xuanwu.northstar.engine.event.FastEventEngine;
import tech.xuanwu.northstar.manager.BarBufferManager;
import tech.xuanwu.northstar.persistence.po.MinBarDataPO;
import tech.xuanwu.northstar.utils.ProtoBeanUtils;
import xyz.redtorch.pb.CoreField.TickField;

/**
 * 处理行情相关操作
 * 
 * @author KevinHuangwl
 *
 */
@Slf4j
public class MarketBarDataHandler extends AbstractEventHandler implements GenericEventHandler {

	/**
	 * gateway -> unifiedSymbol -> generator
	 */
	private Table<String, String, BarGenerator> generatorTbl = HashBasedTable.create();

	private FastEventEngine feEngine;
	
	private BarBufferManager bbMgr;
	
	public MarketBarDataHandler(FastEventEngine feEngine, BarBufferManager bbMgr) {
		this.feEngine = feEngine;
		this.bbMgr = bbMgr;
	}
	
	
	@Override
	public boolean canHandle(NorthstarEventType eventType) {
		return NorthstarEventType.TICK == eventType || NorthstarEventType.IDX_TICK == eventType;
	}

	@Override
	protected void doHandle(NorthstarEvent e) {
		TickField tick = (TickField) e.getData();
		String unifiedSymbol = tick.getUnifiedSymbol();
		String gatewayId = tick.getGatewayId();
		if (!generatorTbl.contains(gatewayId, unifiedSymbol)) {
			generatorTbl.put(gatewayId, unifiedSymbol, new BarGenerator(unifiedSymbol, (bar, ticks) -> {
				feEngine.emitEvent(NorthstarEventType.BAR, bar);
				try {					
					MinBarDataPO barPO = ProtoBeanUtils.toPojoBean(MinBarDataPO.class, bar);
					barPO.setNumOfTicks(ticks.size());
					bbMgr.addBar(barPO);
				}catch(Exception ex) {
					log.warn("############ 详细Tick数据 ###########");
					for(TickField t : ticks) {
						log.info("[tick] - time:{}, vol:{}, volDelta:{}", t.getActionTime(), t.getVolume(), t.getVolumeDelta());
					}
					log.info("[bar] - vol:{}, volDelta:{}", bar.getVolume(), bar.getVolumeDelta());
					log.warn("#######################");
					throw new IllegalStateException(ex);
				}
			}));
		}
		
		generatorTbl.get(gatewayId, unifiedSymbol).updateTick(tick);
	}
}
