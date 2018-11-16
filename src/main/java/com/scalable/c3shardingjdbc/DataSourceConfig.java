package com.scalable.c3shardingjdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class DataSourceConfig {

	private static final String DATASOURCE_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";
	@Autowired
	private YmlConfig dataSourceSet;

	@Bean(name = "shardingDataSource", destroyMethod = "close")
    @Qualifier("shardingDataSource")
    public DataSource getShardingDataSource() {
        // 配置真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<>(2);

        // 配置第一个数据源
        DataSource dataSource1 = buildDataSource(DATASOURCE_TYPE_DEFAULT, dataSourceSet.getTest0());
        dataSourceMap.put("test_msg0", dataSource1);

        // 配置第二个数据源
        DataSource dataSource2 = buildDataSource(DATASOURCE_TYPE_DEFAULT, dataSourceSet.getTest1());
        dataSourceMap.put("test_msg1", dataSource2);

        // 配置Order表规则
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualDataNodes("test_msg${0..1}.t_order_${0..1}");
        //orderTableRuleConfig.setActualDataNodes("db0.t_order_0,db0.t_order_1,db1.t_order_0,db1.t_order_1,db2.t_order_0,db2.t_order_1");

        // 配置分库策略（Groovy表达式配置db规则）
        orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("userId", "test_msg${userId % 2}"));

        // 配置分表策略（Groovy表达式配置表路由规则）
        orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("orderId", "t_order_${orderId % 2}"));

        // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);

        // 配置order_items表规则...

        // 获取数据源对象
        DataSource dataSource = null;
        try {
            dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new ConcurrentHashMap(), new Properties());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSource;
    }

	private DataSource buildDataSource(String type, Map<String, String> dsMap) {
        try {
            Class<? extends DataSource> dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);
            String driverClassName = dsMap.get("driverClassName");
            String url = dsMap.get("url");
            String username = dsMap.get("username");
            String password = dsMap.get("password");

            DataSourceBuilder factory = DataSourceBuilder.create().driverClassName(driverClassName).url(url).username(username).password(password).type(dataSourceType);
            return factory.build();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
