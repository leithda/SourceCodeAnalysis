package cn.leithda.spring.mytest;

/**
 * Created with IntelliJ IDEA.
 * User: 长歌
 * Date: 2020/7/24
 * Description: 测试 bean
 */
public class MyTestBean {
	private String name = "leithda";

	public MyTestBean(String name) {
		this.name = name;
	}

	public MyTestBean() {
	}

	@Override
	public String toString() {
		return "MyTestBean{" +
				"name='" + name + '\'' +
				'}';
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
