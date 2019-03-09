import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.ws.Action;
import javax.xml.ws.RespectBinding;

/**
 * @program: zookeeper-study
 * @author: KDF
 * @create: 2019-03-06 17:23
 **/
public class CreateGroupTest {

    public static String hosts = "127.0.0.1:2181";
    private static String groupName = "/TestZoo3";
    private static String chlid = "/TestZoo3/TestZoo19";

    private CreateGroup createGroup;

    @Before
    public void init() throws Exception {
        createGroup = new CreateGroup();
        createGroup.connect(hosts);

    }
//    @After
    public void destroy(){
        createGroup.close();
    }

    @Test
    public void testCreateGroup() throws Exception {
        createGroup.create(groupName,true);
        String string = createGroup.readData(groupName, true);
        System.out.println("新节点的数据为: " + string );


    }

    @Test
    public void testGetData(){
        String string = createGroup.readData("/TestZoo", true);
        System.out.println(string);
    }

    @Test
    public void testWriteData() throws InterruptedException {
        createGroup.writeData(groupName,"测试111");
        String string = createGroup.readData("/TestZoo", true);
        System.out.println(string);

        Thread.sleep(1000);
        createGroup.writeData("/TestZoo","测试1111");
        String string2 = createGroup.readData("/TestZoo", true);
        System.out.println(string2);
    }


    @Test
    public void testGetChildren() throws Exception {
        createGroup.getChildren(groupName,true);
        createGroup.create(chlid,true );
        Thread.sleep(1000);
    }

    public void deleteChild(){

    }

}



