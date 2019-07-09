package com.kkb.springframework.beans.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kkb.springframework.beans.config.BeanDefinition;
import com.kkb.springframework.beans.config.ClassPathResource;
import com.kkb.springframework.beans.config.PropertyValue;
import com.kkb.springframework.beans.config.Resource;
import com.kkb.springframework.beans.config.RuntimeBeanReference;
import com.kkb.springframework.beans.config.TypedStringValue;
import com.kkb.springframework.beans.config.XmlBeanDefinationParser;
import com.kkb.springframework.beans.converter.IntegerTypeConverter;
import com.kkb.springframework.beans.converter.StringTypeConverter;
import com.kkb.springframework.beans.converter.TypeConverter;
import com.kkb.springframework.beans.utils.ReflectUtils;

public class DefaultListableBeanFactory extends AbstractBeanFactory {
	/**
	 * 读取xml之后获得的BeanDefinition信息
	 */
	private Map<String, BeanDefinition> beanDefinitions = new HashMap<>();// Map中的数据怎么来？

	private Map<String, Object> singletonObjects = new HashMap<>();// Map中的数据怎么来？

	private List<Resource> resources = new ArrayList<>();
	private List<TypeConverter> converters = new ArrayList<>();

	public DefaultListableBeanFactory(String location) {

		// 注册资源类
		registeResources();
		registeTypeConverters();

		XmlBeanDefinationParser xmlBeanDefinationReader = new XmlBeanDefinationParser();

		// 因为不清楚location字符串到底代表的是类路径下的xml还是D盘下的xml还是网络中的xml
		Resource resource = getResource(location);

		xmlBeanDefinationReader.loadBeanDefinations(this, resource);
	}

	private void registeTypeConverters() {
		converters.add(new StringTypeConverter());
		converters.add(new IntegerTypeConverter());

	}

	private void registeResources() {
		// 提前注册好resource
		resources.add(new ClassPathResource());
	}

	private Resource getResource(String location) {

		for (Resource resource : resources) {
			if (resource.isCanRead(location)) {
				return resource;
			}
		}
		return null;
	}

	@Override
	public Object getBean(String beanName) {
		// 优化方案
		// 给对象起个名，在xml配置文件中，建立名称和对象的映射关系
		// 1.如果singletonObjects中已经包含了我们要找的对象，就不需要再创建了。
		// 2.如果singletonObjects中已经没有包含我们要找的对象，那么根据传递过来的beanName参数去BeanDefinition集合中查找对应的BeanDefinition信息
		// 3.根据BeanDefinition中的信息去创建Bean的实例。
		// a)根据class信息包括里面的constructor-arg通过反射进行实例化
		// b)根据BeanDefinition中封装的属性信息集合去挨个给对象赋值。
		// 类型转换
		// 反射赋值
		// c)根据initMethod方法去调用对象的初始化操作

		Object instance = singletonObjects.get(beanName);
		if (instance != null) {
			return instance;
		}
		// 只处理单例
		// 先获取BeanDefinition
		BeanDefinition beanDefinition = this.getBeanDefinitions().get(beanName);
		String beanClassName = beanDefinition.getBeanClassName();

		// 通过构造参数创建Bean的实例
		instance = createBeanInstance(beanClassName, null);

		// 设置参数
		setProperty(instance, beanDefinition);

		// 初始化
		initBean(instance, beanDefinition);

		//把bean对象放入缓存
		registerSingletonObjects(beanName,instance);

		return instance;

	}

	private void initBean(Object instance, BeanDefinition beanDefinition) {
		String initMethod = beanDefinition.getInitMethod();
		if (initMethod == null || initMethod.equals("")) {
			return;
		}
		// 执行初始化方法
		ReflectUtils.invokeMethod(instance, initMethod);
	}

	private void setProperty(Object instance, BeanDefinition beanDefinition) {
		List<PropertyValue> propertyValues = beanDefinition.getPropertyValues();
		for (PropertyValue propertyValue : propertyValues) {
			String name = propertyValue.getName();
			// value的类型可能是TypedStringValue或者是RuntimeBeanReference
			Object value = propertyValue.getValue();

			Object valueToUse = null;
			if (value instanceof TypedStringValue) {
				TypedStringValue typedStringValue = (TypedStringValue) value;
				String stringValue = typedStringValue.getValue();
				Class<?> targetType = typedStringValue.getTargetType();

				for (TypeConverter converter : converters) {
					if (converter.isType(targetType)) {
						valueToUse = converter.convert(stringValue);
					}
				}
			} else if (value instanceof RuntimeBeanReference) {
				RuntimeBeanReference runtimeBeanReference = (RuntimeBeanReference) value;
				String ref = runtimeBeanReference.getRef();
				// 递归调用getBean方法
				valueToUse = getBean(ref);
			}

			// 调用反射进行赋值
			ReflectUtils.setProperty(instance, name, valueToUse);
		}
	}

	/**
	 * 创建Bean的实例
	 * 
	 * @param beanClassName
	 * @param object
	 */
	private Object createBeanInstance(String beanClassName, Object... args) {
		return ReflectUtils.createObject(beanClassName, args);
	}

	public Map<String, BeanDefinition> getBeanDefinitions() {
		return beanDefinitions;
	}

	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
		this.beanDefinitions.put(beanName, beanDefinition);
	}

	public Map<String, Object> getSingletonObjects() {
		return singletonObjects;
	}

	public void registerSingletonObjects(String beanName,Object singletonObjects) {
		this.singletonObjects.put(beanName,singletonObjects);
	}

}
