package org.mltds.sargeras.rpc.httpjson.network.httpjson;

import java.util.List;
import java.util.Random;

import org.mltds.sargeras.api.exception.SagaNetworkException;
import org.mltds.sargeras.common.rpc.RpcRequest;
import org.mltds.sargeras.common.rpc.RpcResponse;
import org.mltds.sargeras.utils.HttpClientUtils;
import org.mltds.sargeras.utils.Utils;
import org.mltds.sargeras.rpc.httpjson.network.Network;

import com.alibaba.fastjson.JSON;

/**
 * @author sunyi.
 */
public class HttpJsonNetwork implements Network {

    private static final String JSON_MEDIA_TYPE = "application/json";
    private static final String CHARSET = "charset=utf-8";

    private String protocol;
    private List<String> addressList;
    private String path;

    private String localIP = Utils.getLocalIPv4();

    public HttpJsonNetwork(String protocol, List<String> addressList, String path) {
        this.protocol = protocol;
        this.addressList = addressList;
        this.path = path;
    }

    private String selectAddress() {
        int size = addressList.size();
        Random random = new Random();
        int i = random.nextInt(size);
        return addressList.get(i);
    }

    @Override
    public RpcResponse request(RpcRequest rpcRequest) throws SagaNetworkException {

        String address = selectAddress();

        String url = buildUrl(protocol, address, path);

        RpcResponse response = request(url, rpcRequest);
        return response;
    }

    private String buildUrl(String protocol, String address, String path) {
        StringBuilder url = new StringBuilder();
        url.append(protocol);
        url.append("://");
        url.append(address);
        if (!path.startsWith("/")) {
            url.append("/");
        }
        url.append(path);

        return url.toString();
    }

    private RpcResponse request(String url, RpcRequest rpcRequest) {

        String json = JSON.toJSONString(rpcRequest);

        try {
            String responseBody = HttpClientUtils.post(url, json, JSON_MEDIA_TYPE, CHARSET, 200, 200);
            RpcResponse rpcResponse = JSON.parseObject(responseBody, RpcResponse.class);
            return rpcResponse;
        } catch (Exception e) {
            throw new SagaNetworkException(url, e);
        }
    }

}
