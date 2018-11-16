package com.scalable.c3split;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import com.scalable.c3split.core.SimpleSplitJdbcTemplate;
import com.scalable.c3split.core.SplitDb;
import com.scalable.c3split.core.SplitTable;
import com.scalable.c3split.core.SplitTablesHolder;

import javax.sql.DataSource;

import java.util.*;

@Configuration
public class DataSourceConfig implements EnvironmentAware {

	private static final String DATASOURCE_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";
	@Autowired
	private ApplicationContext applicationContext;

	/**
     * 加载多数据源配置
     * 凡是被Spring管理的类，实现接口 EnvironmentAware 重写方法 setEnvironment 可以在工程启动时，获取到系统环境变量和application配置文件中的变量。
     */
    @Override
    public void setEnvironment(Environment env) {
    	shardingDataSource(env);
    }

    /**
     * 初始化更多数据源
     *
     */
    @Bean
    public List<DataSource> shardingDataSource(Environment env) {
    	List<DataSource> dataSources = new ArrayList<>();

        // 在 Spring Boot 1.x 中，可以通过下面代码绑定参数到对象：
    	// RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(environment);
    	// Spring Boot 2.x 中，绑定更简单，如下：
    	Binder binder = Binder.get(env);
    	String names = binder.bind("split.datasource.names", String.class).get();
    	for (String dsPrefix : names.split(",")) {// 多个数据源
    		Map<String, String> dsMap = binder.bind("split.datasource." + dsPrefix, Map.class).get();
            DataSource dataSource = buildDataSource(DATASOURCE_TYPE_DEFAULT, dsMap);
            dataSources.add(dataSource);
        }

    	return dataSources;
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

    @Bean
    public SimpleSplitJdbcTemplate simpleSplitJdbcTemplate(){
    	SimpleSplitJdbcTemplate simpleSplitJdbcTemplate = new SimpleSplitJdbcTemplate();
    	simpleSplitJdbcTemplate.setReadWriteSeparate(getSplitTable().isReadWriteSeparate());
    	simpleSplitJdbcTemplate.setSplitTablesHolder(splitTablesHolder());
    	return simpleSplitJdbcTemplate;
    }

    private SplitTablesHolder splitTablesHolder(){
    	List<SplitTable> splitTables = new ArrayList<>();
    	splitTables.add(getSplitTable());
    	SplitTablesHolder splitTablesHolder = new SplitTablesHolder(splitTables);
    	return splitTablesHolder;
    }

    /**1个数据库实例，1个实例里2个数据库，1个数据库里2个表
     * @return
     */
    @Bean
    public SplitTable splitTable(){
    	SplitTable splitTable = new SplitTable();
    	splitTable.setDbNamePrefix("test_msg");
    	splitTable.setTableNamePrefix("order_entity");
    	List<DataSource> dataSources = getShardingDataSource();
    	splitTable.setDbNum(dataSources.size());
    	splitTable.setTableNum(2);
    	splitTable.setReadWriteSeparate(true);
    	List<SplitDb> splitDbs = new ArrayList<>();
    	for (DataSource ds : dataSources) {
    		splitDbs.add(getSplitDb(ds));
		}
    	splitTable.setSplitDbs(splitDbs);
    	splitTable.init();
    	return splitTable;
    }

    private SplitDb getSplitDb(DataSource ds){
    	SplitDb splitDb = new SplitDb();
    	splitDb.setMasterTemplate(masterTemplate(ds));
    	List<JdbcTemplate> slaveTemplates = new ArrayList<>();
    	slaveTemplates.add(masterTemplate(ds));
    	splitDb.setSlaveTemplates(slaveTemplates);
    	return splitDb;
    }

    private JdbcTemplate masterTemplate(DataSource ds){
    	JdbcTemplate jdbcTemplate = new JdbcTemplate();
    	jdbcTemplate.setLazyInit(false);
    	jdbcTemplate.setDataSource(ds);
    	return jdbcTemplate;
    }

	private SplitTable getSplitTable(){
		return (SplitTable) applicationContext.getBean("splitTable");
	}

	private List<DataSource> getShardingDataSource(){
		return (List<DataSource>) applicationContext.getBean("shardingDataSource");
	}

}
