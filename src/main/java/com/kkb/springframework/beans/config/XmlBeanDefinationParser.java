package com.kkb.springframework.beans.config;

import java.io.InputStream;

import org.dom4j.Document;

import com.kkb.springframework.beans.factory.DefaultListableBeanFactory;
import com.kkb.springframework.beans.utils.DocumentReader;

public class XmlBeanDefinationParser {

	public void loadBeanDefinations(DefaultListableBeanFactory beanFactory, Resource resource) {
		// 1.读取配置文件的bean信息
		InputStream inputStream = resource.getInputStream();
		Document document = DocumentReader.createDocument(inputStream);
		XmlBeanDefinationDocumentParser documentReader = new XmlBeanDefinationDocumentParser(beanFactory);
		documentReader.loadBeanDefinations(document.getRootElement());
	}

}
