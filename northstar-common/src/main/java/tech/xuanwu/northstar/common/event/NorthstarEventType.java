package tech.xuanwu.northstar.common.event;

/**
 * 系统事件列表
 * @author KevinHuangwl
 *
 */
public enum NorthstarEventType {
	/**
	 * 行情TICK事件
	 */
	TICK,
	/**
	 * 指数TICK事件
	 */
	IDX_TICK,
	/**
	 * K线BAR事件
	 */
	BAR,
	/**
	 * 历史K线事件
	 */
	HIS_BAR,
	/**
	 * 账户回报事件
	 */
	ACCOUNT,
	/**
	 * 持仓回报事件
	 */
	POSITION,
	/**
	 * 成交回报事件
	 */
	TRADE,
	/**
	 * 委托回报事件
	 */
	ORDER,
	/**
	 * 消息事件
	 */
	NOTICE,
	/**
	 * 外部消息事件
	 */
	EXT_MSG,
	/**
	 * 合约事件
	 */
	CONTRACT,
	/**
	 * 指数合约事件
	 */
	IDX_CONTRACT,
	/**
	 * 合约加载完成事件
	 */
	CONTRACT_LOADED,
	/**
	 * 出入金事件
	 */
	BALANCE,
	/**
	 * 连线中
	 */
	CONNECTING,
	/**
	 * 连线成功
	 */
	CONNECTED,
	/**
	 * 登陆中
	 */
	LOGGING_IN,
	/**
	 * 登陆成功
	 */
	LOGGED_IN,
	/**
	 * 登出中
	 */
	LOGGING_OUT,
	/**
	 * 登出成功
	 */
	LOGGED_OUT,
	/**
	 * 断开中
	 */
	DISCONNECTING,
	/**
	 * 断开成功
	 */
	DISCONNECTED,
	/**
	 * 交易日更新事件
	 */
	TRADE_DATE,
	/**
	 * 下单事件
	 */
	PLACE_ORDER,
	/**
	 * 撤单事件
	 */
	WITHDRAW_ORDER
	
}
