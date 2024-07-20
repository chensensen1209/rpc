package rpc.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassDescription: 存放channel Channel用于在服务端和客户端之间传输数据
 * 管理channel
 * @Author: chensen
 * @Created: 2024/7/18 19:12
 */
@Slf4j
public class ChannelProvider {
    private final Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress){
        //InetSocketAddress 是一个封装了 IP 地址和端口号的类 ，格式 hostname/IP:port
        String key = inetSocketAddress.toString();
        if (channelMap.containsKey(key)){
            Channel channel = channelMap.get(key);
            if (channel != null && channel.isActive()){
                return channel;
            }
            else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress,Channel channel){
        channelMap.put(inetSocketAddress.toString(),channel);
    }

    public void remove(InetSocketAddress inetSocketAddress){
        channelMap.remove(inetSocketAddress.toString());
        log.info("Channel map size :[{}]", channelMap.size());
    }
}
