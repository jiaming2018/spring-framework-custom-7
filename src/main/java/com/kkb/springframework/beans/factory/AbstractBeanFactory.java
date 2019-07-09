package com.kkb.springframework.beans.factory;

public abstract class AbstractBeanFactory implements BeanFactory {

	@Override
	public Object getBean(String beanName) {
		return null;
	}

	@Override
	public Object getBean(Class<?> clazz) {
		return null;
	}

}
