package org.mltds.sargeras.namespace;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SagaNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("saga", new SagaApplicationDefinitionParser());
    }

}