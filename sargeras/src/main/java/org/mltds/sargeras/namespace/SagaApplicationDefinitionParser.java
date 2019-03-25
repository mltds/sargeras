package org.mltds.sargeras.namespace;

import java.util.Map;
import java.util.Set;

import org.mltds.sargeras.api.SagaApplication;
import org.mltds.sargeras.api.annotation.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Element;

public class SagaApplicationDefinitionParser extends AbstractSingleBeanDefinitionParser implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SagaApplicationDefinitionParser.class);

    protected Class getBeanClass(Element element) {
        return SagaApplication.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder bean) {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Saga.class);

        Set<Map.Entry<String, Object>> entries = beansWithAnnotation.entrySet();

        for (Map.Entry<String, Object> entry : entries) {
            String beanId = entry.getKey();
            Object sagaObject = entry.getValue();

            Saga annotation = sagaObject.getClass().getAnnotation(Saga.class);
            String appName = annotation.appName();
            String bizName = annotation.bizName();

        }


    }
}