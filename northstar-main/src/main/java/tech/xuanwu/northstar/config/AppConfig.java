package tech.xuanwu.northstar.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

import tech.xuanwu.northstar.common.model.ContractManager;
import tech.xuanwu.northstar.domain.account.TradeDayAccount;
import tech.xuanwu.northstar.engine.event.FastEventEngine;
import tech.xuanwu.northstar.gateway.api.AbstractGatewayFactory;
import tech.xuanwu.northstar.gateway.sim.SimGatewayFactory;
import tech.xuanwu.northstar.gateway.sim.SimMarket;
import tech.xuanwu.northstar.gateway.sim.persistence.SimAccountRepository;
import tech.xuanwu.northstar.interceptor.AuthorizationInterceptor;
import tech.xuanwu.northstar.manager.BarBufferManager;
import tech.xuanwu.northstar.manager.GatewayAndConnectionManager;
import tech.xuanwu.northstar.persistence.MarketDataRepository;
import tech.xuanwu.northstar.utils.MongoClientAdapter;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.CtpGatewayFactory;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.CtpTradeNowFactory;

/**
 * 配置转换器
 * @author KevinHuangwl
 *
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		/**
		 * 调整转换器优先级
		 */
		List<HttpMessageConverter<?>> jacksonConverters = new ArrayList<>();
		Iterator<HttpMessageConverter<?>> itCvt = converters.iterator();
		while(itCvt.hasNext()) {
			HttpMessageConverter<?> cvt = itCvt.next();
			if(cvt instanceof MappingJackson2HttpMessageConverter) {
				jacksonConverters.add(cvt);
				itCvt.remove();
			}
		}
		for(HttpMessageConverter<?> cvt : jacksonConverters) {
			converters.add(0, cvt);
		}
	}
	
	@Bean
    public CorsFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();
        // 设置允许跨域请求的域名
        config.addAllowedOrigin("*");
        // 是否允许证书 不再默认开启
         config.setAllowCredentials(true);
        // 设置允许的方法
        config.addAllowedMethod("*");
        // 允许任何头
        config.addAllowedHeader("*");
        config.addExposedHeader("token");
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", config);
        return new CorsFilter(configSource);
    }
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new AuthorizationInterceptor()).addPathPatterns("/**").excludePathPatterns("/auth/login");
	}
	
	@Bean
	public MongoClientOptions mongoClientOptions() {
		return MongoClientOptions.builder().maxConnectionIdleTime(120000).build();
	}

	@Bean
	public MongoClientAdapter createMongoClientAdapter(MongoClient mongo) {
		return new MongoClientAdapter(mongo);
	}
	
	@Bean
	public GatewayAndConnectionManager gatewayAndConnectionManager() {
		return new GatewayAndConnectionManager();
	}
	
	@Bean
	public BarBufferManager barBufferManager(MarketDataRepository mdRepo) {
		return new BarBufferManager(mdRepo);
	}
	
	@Value("${northstar.contracts.canHandle}")
	private String[] productClassTypes;
	
	@Bean
	public ContractManager contractManager() {
		return new ContractManager(productClassTypes);
	}
	
	@Bean
	public ConcurrentHashMap<String, TradeDayAccount> accountMap(){
		return new ConcurrentHashMap<>();
	}
	
	@Bean
	public SimMarket simMarket(SimAccountRepository simAccRepo) {
		return new SimMarket(simAccRepo);
	}
	
	@Bean
	public AbstractGatewayFactory ctpGatewayFactory(FastEventEngine fastEventEngine) {
		return new CtpGatewayFactory(fastEventEngine);
	}
	
	@Bean
	public AbstractGatewayFactory ctpTradeNowFactory(FastEventEngine fastEventEngine) {
		return new CtpTradeNowFactory(fastEventEngine);
	}
	
	@Bean
	public AbstractGatewayFactory simGatewayFactory(FastEventEngine fastEventEngine, SimMarket simMarket, ContractManager contractMgr) {
		return new SimGatewayFactory(fastEventEngine, simMarket, contractMgr);
	}
	
}
