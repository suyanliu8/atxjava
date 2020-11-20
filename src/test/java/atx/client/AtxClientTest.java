package atx.client;

import atx.client.enums.KeybordEnums;
import atx.client.enums.AttributeMask;
import atx.client.model.DesiredCapabilities;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 飞狐 on 2018/4/22.
 */
public class AtxClientTest {

    AtxClient driver = new AtxClient();
    private DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

    @Before
    public void setUp() throws Exception{
        desiredCapabilities.setPackageName("com.netease.cloudmusic");
        desiredCapabilities.setRemoteHost("172.16.0.1");

        driver.initDriver(desiredCapabilities);
    }

    @Test
    public void testNetE() throws Exception{
        driver.elementByName("私人FM").click();
        driver.elementByDesc("转到上一层级").click();
        driver.elementByName("每日推荐").click();
        driver.elementByDesc("转到上一层级").click();
        driver.elementByName("歌单").click();
        driver.elementByDesc("转到上一层级").click();
        driver.elementByName("排行榜").click();
        driver.elementByDesc("转到上一层级").click();
    }

    /**
     * 搜索歌曲
     */
    @Test
    public void testSearchSong() throws Exception{
//        driver.elementByDesc("搜索").click();

//        driver.setKeybord(KeybordEnums.FASTINPUT.getValue());

        Map<String,Object> searchParams = new HashMap<String,Object>();
        searchParams.put(AttributeMask.DESCRIPTION.getDes(),"搜索");
        searchParams.put(AttributeMask.CLASS_NAME.getDes(),"android.widget.TextView");
        driver.elementByMult(searchParams).click();

        driver.elementById("com.netease.cloudmusic:id/search_src_text").sendKeys("大王叫我来巡山");

        Map<String,Object> songParams = new HashMap<String,Object>();
        songParams.put(AttributeMask.TEXT_STARTS_WITH.getDes(),"搜索");
        songParams.put(AttributeMask.CLASS_NAME.getDes(),"android.widget.TextView");
        driver.elementByMult((songParams)).click();

        //选择一个音乐开始播放咯
        String xpath = "//*[@resource-id=\"com.netease.cloudmusic:id/gt\"]/*[@class=\"android.widget.LinearLayout\"][4]/*[@class=\"android.widget.LinearLayout\"][1]/*[@class=\"android.widget.RelativeLayout\"][1]";
        driver.elementByXpath(xpath).click();

        driver.sleep(5000);

        //由于xml不断变化，导致 播放暂停无法找到，因此直接使用xpath进行解决

        System.out.println("播放暂停");
        String xpath1 = "//*[@content-desc=\"播放暂停\"]";

        driver.elementByXpath(xpath1).click();


        System.out.println("转到上一层级");
        driver.elementByDesc("转到上一层级").click();

        driver.elementByDesc("收起").click();

    }

    /**
     * 查找多级元素
     */
    @Test
    public void testElements() throws Exception{
        driver.dumpHierarchy();
        driver.elementByXpath("//*[@resource-id=\"com.netease.cloudmusic:id/gt\"]/*[@class=\"android.widget.LinearLayout\"][1]/*[@class=\"android.widget.LinearLayout\"][3]/*[@class=\"android.widget.ImageView\"][1]").click();
    }

    /**
     * 测试杂七杂八
     */
    @Test
    public void testGetOther() throws Exception{
//        System.out.println(driver.getCurrentActivity());
//        driver.elementByName("私人FM").click();
//        driver.press(KeyEventEnum.VOLUME_UP.getValue());
//        driver.press(KeyEventEnum.VOLUME_DWON.getValue());
//        driver.press(KeyEventEnum.VOLUME_MUTE.getValue());
        //测试截图
//        String fileName = "/Users/huqingen/Desktop/Finger/Git/test/atxuiautomatorclient/picture/1.jpg";
//        driver.takeScreenshot(fileName);

//        ShellUtils.getInstance(desiredCapabilities).shellPost("ime list -s");

//        AdbDevice.getInstance(desiredCapabilities).startAtxAgent();

        //启动停止ui
//        driver.stopUiAutomator();
//        driver.startUiAutomator();

//        driver.setKeybord("aa");
          driver.setKeybord(KeybordEnums.FASTINPUT.getValue());
    }

}
