package cn.leithda.spring.mytest.config;

import cn.leithda.spring.mytest.MyTestBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: 长歌
 * Date: 2020/8/6
 * Description: 注解开发配置类
 */
@Configuration
public class ApplicationConfig {

	@Bean(name = "bean1")
	public MyTestBean bean1(){
		return new MyTestBean("bean1");
	}

	@Bean(name = "bean2")
	public MyTestBean bean2(){
		return new MyTestBean("bean2");
	}
}
