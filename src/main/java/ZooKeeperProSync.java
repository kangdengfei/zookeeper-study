import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * @program: zookeeper-study
 * @author: KDF
 * @create: 2019-03-06 14:21
 **/
public class ZooKeeperProSync implements Watcher {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    private static ZooKeeper zk = null;

    private static Stat stat = new Stat();

    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected){// 判断是否连接
            connectedSemaphore.countDown(); //计数器减一；
            if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged){//子节点变化时间
                try {
                    System.out.println("配置已修改，新值为："+ new String(zk.getData(watchedEvent.getPath(),true,stat)));
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public static void main(String[] args) throws Exception {
        String path = "/zoo";
        String ip = "127.0.0.1:2181";
        Integer port = 2181;
        zk = new ZooKeeper(ip,5000,new ZooKeeperProSync());
        connectedSemaphore.await();
        System.out.println(new String(zk.getData(path,true,stat)));
        Thread.sleep(Integer.MAX_VALUE);
    }
}



