package com.itheima.config;

import com.alibaba.druid.pool.DruidDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/*****
 * @Author: 黑马训练营
 * @Description: com.itheima.config
 ****/
@Configuration
public class DataSourceProxyConfig {

    /****
     * 手动配置DataSouce
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DruidDataSource dataSouce(){
        return new DruidDataSource();
    }

    /***
     * 创建DataSourceProxy对象,交给SpringIOC容器
     */
    @Bean
    public DataSourceProxy dataSourceProxy(DataSource dataSource){
        return new DataSourceProxy(dataSource);
    }


    /****
     * 所有涉及到数据库操作的对象的数据源一律换成DataSourceProxy
     * Mybatis:SqlSessionFactory->DataSource换成DataSourceProxy
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSourceProxy dataSourceProxy) throws Exception{
        //SqlSessionFactoryBean
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        //注入数据源
        sqlSessionFactoryBean.setDataSource(dataSourceProxy);
        return sqlSessionFactoryBean.getObject();
    }
    /****
     * <bean class="org.mybatis.spring.SqlSessionFactoryBean">
     *      <property name="dataSource" ref="dataSource" />
     * </bean>
     */

}
