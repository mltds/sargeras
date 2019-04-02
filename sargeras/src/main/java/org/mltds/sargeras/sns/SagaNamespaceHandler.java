package org.mltds.sargeras.sns;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SagaNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("application", new SagaApplicationDefinitionParser());
    }

}