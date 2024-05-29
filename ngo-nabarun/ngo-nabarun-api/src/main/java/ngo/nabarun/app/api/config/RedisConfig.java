package ngo.nabarun.app.api.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

	@Value("${redis.server}")
	private String server;

	@Value("${redis.port}")
	private String port;

	@Value("${redis.username}")
	private String username;

	@Value("${redis.password}")
	private String password;

	@Value("${redis.database}")
	private String database;

	@Value("${redis.timeout}")
	private String timeout;
	
	@Value("${redis.globalTtl}")
	private String globalTtl;

	@Bean
	RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}

	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
		// System.out.println(server +" "+port+" "+database+" "+password+" "+username+"
		// "+timeout);
		redisConfiguration.setHostName(server);
		redisConfiguration.setPort(Integer.parseInt(port));
		redisConfiguration.setDatabase(Integer.parseInt(database));
		redisConfiguration.setPassword(RedisPassword.of(password));
		redisConfiguration.setUsername(username);

		JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();
		jedisClientConfiguration.connectTimeout(Duration.ofSeconds(Integer.parseInt(timeout)));
		return new JedisConnectionFactory(redisConfiguration, jedisClientConfiguration.build());
	}

//	@Bean
//	CacheManager cacheManager() {
//		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(Integer.parseInt(globalTtl)));
//		return RedisCacheManager.builder(jedisConnectionFactory()).cacheDefaults(config).transactionAware().build();
//	}
}
