package transport.zookeeper.example;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 16:17
 */
public class zookeeperExample {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    static CuratorFramework zkClient;
    static {
// Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                // the server to connect to (can be a server list)
                .connectString("127.0.0.1:2181")
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
    }
    @Test
    public void f() throws Exception {
        zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/node1/00001");
    }
}
