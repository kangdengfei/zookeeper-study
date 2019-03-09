import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: zookeeper-study
 * @author: KDF
 * @create: 2019-03-06 15:47
 **/
public class CreateGroup implements Watcher {

    private static final int SESSION_TIMEOUT = 1000;//会话延时

    private ZooKeeper zk = null;

    private CountDownLatch countDownLatch = new CountDownLatch(1);//同步计数器

    /** 定义原子变量 */
    AtomicInteger seq = new AtomicInteger();

    public void process(WatchedEvent watchedEvent) {
        System.out.println("进入 process....event = " + watchedEvent);

        if (watchedEvent == null)
            return;
        Event.EventType type = watchedEvent.getType();//事件类型

        Event.KeeperState state = watchedEvent.getState();//连接状态

        String path = watchedEvent.getPath();

        String logPrefix = "[watcher-" + this.seq.incrementAndGet() + "]";

        System.out.println(logPrefix + "收到watcher通知");
        System.out.println(logPrefix + "连接状态:" + state.toString());
        System.out.println(logPrefix + "事件类型:" + type.toString());

        if (Event.KeeperState.SyncConnected == state){
            //成功连接服务器
            if (Event.EventType.None == type){
                System.out.println(logPrefix + "成功连接上zk服务器");
                countDownLatch.countDown();
            }
            else if (Event.EventType.NodeCreated == type){
                System.out.println(logPrefix + "节点创建");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.exists(path,true);
            }
            else if (Event.EventType.NodeDataChanged == type){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(logPrefix+ "节点数据更新");
                System.out.println(logPrefix + "数据内容: "+this.readData(path,true));
            }
            else if(Event.EventType.NodeChildrenChanged == type){
                System.out.println(logPrefix + "子节点更新");
                System.out.println(logPrefix + this.getChildren("/",false));
            }
            else if (Event.EventType.NodeDeleted == type){
                System.out.println(logPrefix + "节点 " + path +" 被删除");
            }
            else
                ;
        }
        else if (Event.KeeperState.Disconnected == state){
            System.out.println(logPrefix + "与zk服务器断开连接");
        }

        else if(Event.KeeperState.AuthFailed == state){
            System.out.println(logPrefix + " 权限检查失败");
        }
        else if(Event.KeeperState.Expired == state){
            System.out.println(logPrefix + "会话失效");
        }
        else ;


        if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
             countDownLatch.countDown(); //计数器减一；
        }
    }


    /**
     * 读取指定节点的内容
     * @param path
     * @param needWatch
     * @return
     */
    public String readData(String path, boolean needWatch){
        try {
            return new String(this.zk.getData(path,needWatch,null));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 更新指定节点的内容
     * @param path
     * @param data
     * @return
     */
    public boolean writeData(String path,String data){
        try {
            this.exists(path,true);
            System.out.println("更新数据成功，path:"+ path +",stat:"+ this.zk.setData(path,data.getBytes(),-1));
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除指定节点
     * @param path
     */
     public void deleteNode(String path){
         try {
             this.zk.delete(path,-1);
         } catch (Exception e) {
             e.printStackTrace();
         }
         System.out.println("删除节点成功，path：" +path);
     }

    /**
     * 判断制定节点是否存在
     * @param path
     * @param needWatch
     * @return
     */
    public Stat exists(String path,boolean needWatch){
        try {
            return this.zk.exists(path,needWatch);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取子节点
     * @param path
     * @param needWatch
     * @return
     */
    public List<String> getChildren(String path,boolean needWatch){
        try {
            return this.zk.getChildren(path, needWatch);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void deleteAllPath(){
        if (this.exists("/",false) != null){
            this.deleteNode("/");
        }
    }



    /**
     * 创建zk对象
     * 当客户端连接上zooleeper时会执行process（event）里的countDownLatch.countDown(),计数器的值变为0，则countDownLatch.await()方法返回。
     * @param host
     * @throws Exception
     */
    public void connect(String host) throws Exception{
        zk = new ZooKeeper(host,SESSION_TIMEOUT,this);
        countDownLatch.await(); //阻塞程序执行
    }

    /**
     * 创建grop
     * @param groupName
     */
    public void create(String groupName,boolean flag) throws Exception{
        String path =  groupName;
        this.zk.exists(path,flag);
        String createpath = zk.create(path,"hello world".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE/*允许任何客户端对该znode进行读写*/, CreateMode.PERSISTENT/*持久化的znode*/);
        this.zk.exists(path,flag);
    }

    /**
     * 释放连接
     */
    public void close() {
        if (zk != null){
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                zk = null;
                System.gc();
            }
        }
    }




}



