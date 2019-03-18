package org.mltds.sargeras.rpc.httpjson.network.httpjson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mltds.sargeras.api.SagaConfig;
import org.mltds.sargeras.rpc.httpjson.network.Network;
import org.mltds.sargeras.rpc.httpjson.network.NetworkFactory;

/**
 * @author sunyi.
 */
public class HttpJsonNetworkFactory implements NetworkFactory {

    private static final String SERVER_PREFIX = "network.httpjson.";
    private static final String SERVER_PROTOCOL = SERVER_PREFIX + "protocol";
    private static final String SERVER_ADDRESSES = SERVER_PREFIX + "addresses";
    private static final String SERVER_PATH = SERVER_PREFIX + "path";

    private Network httpJsonNetwork;

    @Override
    public Network getObject() {

        if (httpJsonNetwork != null) {
            return httpJsonNetwork;
        }

        synchronized (HttpJsonNetworkFactory.class) {

            if (httpJsonNetwork != null) {
                return httpJsonNetwork;
            }

            String protocol = SagaConfig.getProperty(SERVER_PROTOCOL);

            String addresses = SagaConfig.getProperty(SERVER_ADDRESSES);
            List<String> addressList = new ArrayList<>();
            if (StringUtils.isNotEmpty(addresses)) {
                addressList.addAll(Arrays.asList(addresses.split(",")));
            }

            String path = SagaConfig.getProperty(SERVER_PATH);

            httpJsonNetwork = new HttpJsonNetwork(protocol, addressList, path);
            return httpJsonNetwork;
        }
    }

}
