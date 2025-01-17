package tech.xuanwu.northstar.common.model;

import lombok.Data;

@Data
public class CtpSettings implements GatewaySettings{

	private String userId;
	private String password;
	private String brokerId;
	private String tdHost;
	private String tdPort;
	private String mdHost;
	private String mdPort;
	private String authCode;
	private String userProductInfo; 
	private String appId;
}
