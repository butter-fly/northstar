package tech.xuanwu.northstar.manager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import tech.xuanwu.northstar.common.exception.NoSuchElementException;
import tech.xuanwu.northstar.domain.GatewayConnection;
import tech.xuanwu.northstar.gateway.api.Gateway;

/**
 * 用于管理与缓存网关对象
 * @author KevinHuangwl
 *
 */
public class GatewayAndConnectionManager {

	private Map<String, Entry> gatewayMap = new ConcurrentHashMap<>();

	public void createPair(GatewayConnection conn, Gateway gateway) {
		Assert.isTrue(StringUtils.equals(conn.getGwDescription().getGatewayId(), gateway.getGatewaySetting().getGatewayId()),
				"网关名称不一致");
		gatewayMap.put(conn.getGwDescription().getGatewayId(), new Entry(conn, gateway));
	}

	public void removePair(GatewayConnection conn) {
		gatewayMap.remove(conn.getGwDescription().getGatewayId());
	}

	public void removePair(Gateway gateway) {
		gatewayMap.remove(gateway.getGatewaySetting().getGatewayId());
	}

	public void onGatewayUpdate(Gateway gateway) {
		String gatewayId = gateway.getGatewaySetting().getGatewayId();
		checkExist(gatewayId);
		gatewayMap.get(gatewayId).gw = gateway;
	}

	public void onGatewayConnectionUpdate(GatewayConnection conn) {
		String gatewayId = conn.getGwDescription().getGatewayId();
		checkExist(gatewayId);
		gatewayMap.get(gatewayId).conn = conn;
	}

	public Gateway getGatewayByConnection(GatewayConnection conn) {
		return getGatewayById(conn.getGwDescription().getGatewayId());
	}

	public GatewayConnection getConnectionByGateway(Gateway gateway) {
		return getGatewayConnectionById(gateway.getGatewaySetting().getGatewayId());
	}

	public Gateway getGatewayById(String gatewayId) {
		checkExist(gatewayId);
		return gatewayMap.get(gatewayId).gw;
	}

	public GatewayConnection getGatewayConnectionById(String gatewayId) {
		checkExist(gatewayId);
		return gatewayMap.get(gatewayId).conn;
	}

	private void checkExist(String gatewayId) {
		if (!gatewayMap.containsKey(gatewayId)) {
			throw new NoSuchElementException("找不到网关：" + gatewayId);
		}
	}

	public boolean exist(String gatewayId) {
		return gatewayMap.containsKey(gatewayId);
	}

	public List<GatewayConnection> getAllConnections() {

		return Collections.unmodifiableList(
				gatewayMap
					.values()
					.stream()
					.map(e -> e.conn)
					.collect(Collectors.toList())
				);
	}

	public int size() {
		return gatewayMap.size();
	}

	private class Entry {

		GatewayConnection conn;
		Gateway gw;

		Entry(GatewayConnection conn, Gateway gw) {
			this.conn = conn;
			this.gw = gw;
		}
	}
}
