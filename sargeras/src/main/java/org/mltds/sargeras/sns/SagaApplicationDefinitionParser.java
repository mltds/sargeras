package org.mltds.sargeras.sns;

import org.apache.commons.lang3.StringUtils;
import org.mltds.sargeras.api.exception.SagaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class SagaApplicationDefinitionParser implements BeanDefinitionParser {

    private static final Logger logger = LoggerFactory.getLogger(SagaApplicationDefinitionParser.class);

    private static final String MANAGER_ATTRIBUTE_NAME = "manager";
    private static final String MANAGER_DEFAULT = "org.mltds.sargeras.spi.manager.rdbms.RdbmsManager";

    private static final String POLL_RETRY_ATTRIBUTE_NAME = "poll-retry";
    private static final String POLL_RETRY_DEFAULT = "org.mltds.sargeras.spi.pollretry.schedule.ScheduledPollRetry";

    private static final String SERIALIZER_ATTRIBUTE_NAME = "serializer";
    private static final String SERIALIZER_DEFAULT = "org.mltds.sargeras.spi.serializer.hessian.HessianSerializer";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        String manager = element.getAttribute(MANAGER_ATTRIBUTE_NAME);
        String pollRetry = element.getAttribute(POLL_RETRY_ATTRIBUTE_NAME);
        String serializer = element.getAttribute(SERIALIZER_ATTRIBUTE_NAME);


        if (StringUtils.isBlank(manager)) {

        } else {
            if (!parserContext.getRegistry().containsBeanDefinition(manager)) {
                throw new SagaException("创建一个 Manager 或者使用默认的");
            }

            BeanDefinition beanDefinition = parserContext.getRegistry().getBeanDefinition(manager);


        }




        return null;
    }
}