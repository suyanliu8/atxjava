package atx.client;

import atx.client.common.*;
import atx.client.enums.Attribute;
import atx.client.enums.AttributeMask;
import atx.client.enums.Const;
import atx.client.enums.MethodEnum;
import atx.client.model.AtxDriver;
import atx.client.model.DesiredCapabilities;
import atx.client.model.DeviceInfo;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.*;

/**
 * Created by 飞狐 on 2018/4/22.
 */
public class AtxClient {

    private AtxDriver atxContexts = new AtxDriver();

    private ElementObj element = new ElementObj(atxContexts);

    private Session session = Session.getInstance(atxContexts);

    private OkHttpClientMethod okHttpClientMethod = OkHttpClientMethod.getInstance();


    public AtxClient initDriver(DesiredCapabilities desiredCapabilities){
        session.createSession(desiredCapabilities);
        return this;
    }



    /**
     * 判断元素是否存在
     * @param jsonObjectList
     * @return
     * @throws Exception
     */
    private boolean findElement(List<JSONObject> jsonObjectList) throws Exception {

        List<Object> params = new ArrayList<Object>();
        params.addAll(jsonObjectList);
        params.add(20000);


        JSONObject requestParams = new JSONObject();
        requestParams.put("jsonrpc","2.0");
        requestParams.put("id",SortUtils.jsonrpcIdValue(MethodEnum.WAIT_FOR_EXISITS.getValue()));
        requestParams.put("method", MethodEnum.WAIT_FOR_EXISITS.getValue());
        requestParams.put("params",params);

        //查找元素是否存在
        JSONObject response = (JSONObject) okHttpClientMethod.postByteMethod(atxContexts.getAtxHost() + Const.BASE_URI,requestParams);
        boolean isExists = response.getBoolean("result");
        if (isExists) {
            //把元素信息存储起来
            int index = getNotJSONIndex(params);
            params.remove(index);
            atxContexts.setSearchEleInfo(params);
            //获取元素基础信息
            JSONObject objInfo = (JSONObject) okHttpClientMethod.postByteMethod(atxContexts.getAtxHost() + Const.BASE_URI, ElementObj.baseRequestJson(MethodEnum.OBJ_INFO.getValue(),params));
            atxContexts.setObjInfo(objInfo);
            atxContexts.setCoordinateInfo(null);
            return true;
        } else {
            return false;
        }

    }

    /**
     * 查找出不是json对象的坐标
     * @param params
     * @return
     */
    private Integer getNotJSONIndex(List<Object> params){
        if(params != null) {
            for(int i = 0 ; i < params.size(); i++){
                if(params.get(i) instanceof JSONObject){

                }else {
                    return i;
                }
            }
        }
        return null;
    }

    /**
     * 根据 text 查找
     * @param name
     * @return
     * @throws Exception
     */
    public ElementObj elementByName(String name) throws Exception {
        JSONObject jsonObject = new JSONObject();
        List<Integer> fields = new ArrayList<Integer>();
        fields.add(AttributeMask.TEXT.getValue());

        jsonObject.put("mask", SortUtils.maskValue(fields));
        jsonObject.put("childOrSibling",new ArrayList<>());
        jsonObject.put("childOrSiblingSelector",new ArrayList<>());
        jsonObject.put(AttributeMask.TEXT.getDes(),name);
        List<JSONObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(jsonObject);

        boolean isExist = findElement(jsonObjectList);
        return isExist ? element : null;
    }

    /**
     * 根据描述查找
     * @param des
     * @return
     * @throws Exception
     */
    public ElementObj elementByDesc(String des) throws Exception {
        JSONObject jsonObject = new JSONObject();

        List<Integer> fields = new ArrayList<Integer>();
        fields.add(AttributeMask.DESCRIPTION.getValue());

        jsonObject.put("mask", SortUtils.maskValue(fields));
        jsonObject.put(AttributeMask.DESCRIPTION.getDes(), des);

        List<JSONObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(jsonObject);

        boolean isExist = findElement(jsonObjectList);
        return isExist ? element : null;
    }

    /**
     * 根据resourceId查找
     * @param resourceId
     * @return
     * @throws Exception
     */
    public ElementObj elementById(String resourceId) throws Exception {
        JSONObject jsonObject = new JSONObject();

        List<Integer> fields = new ArrayList<Integer>();
        fields.add(AttributeMask.RESOURCEID.getValue());

        jsonObject.put("mask", SortUtils.maskValue(fields));
        jsonObject.put(AttributeMask.RESOURCEID.getDes(), resourceId);

        List<JSONObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(jsonObject);

        boolean isExist = findElement(jsonObjectList);
        return isExist ? element : null;
    }


    /**
     * 根据resourceId查找
     *  XPATH格式要求:"//*[@resource-id=\"com.netease.cloudmusic:id/a5g\"]"
     *  多级元素查找
     *  "//*[@resource-id=\"com.netease.cloudmusic:id/gt\"]/*[@class=\"android.widget.LinearLayout\"][1]"
     * @param xpath
     * @return
     * @throws Exception
     */
    public ElementObj elementByXpath(String xpath) throws Exception {

        //第一步肯定是先获取xml咯
        dumpHierarchy();

        File directory  = new File(".");
        String path = null;
        try {
            path = directory.getCanonicalPath();
            path = path + Const.XML_PATH;
        }catch (Exception e){
            e.printStackTrace();
        }

        Document document = new SAXReader().read(new File(path));

        List<Node> list = document.selectNodes(xpath);

        boolean isExist = false;

        if(list != null && list.size() == 0){
            isExist = true;
        }

        if(list.size() > 1){
            System.out.println("xpath元素指定不够精确，元素过多");
            return null;
        }

        isExist = true;
        Element node = (Element) list.get(0);
        //主要目标是根据坐标进行点击，因此 计算坐标
//        convertCoordinate(node.attribute("bounds").getValue());


        Map<String,Object> searchObjs = new HashMap<String,Object>();

        if(StringUtils.isNotEmpty(node.attribute(Attribute.INDEX.getName()).getValue())) {
            searchObjs.put(AttributeMask.INDEX.getDes(), node.attribute(Attribute.INDEX.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.PACKAGE.getName()).getValue())) {
            searchObjs.put(AttributeMask.PACKAGE_NAME.getDes(), node.attribute(Attribute.PACKAGE.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.CLASS.getName()).getValue())) {
            searchObjs.put(AttributeMask.CLASS_NAME.getDes(), node.attribute(Attribute.CLASS.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.TEXT.getName()).getValue())) {
            searchObjs.put(AttributeMask.TEXT.getDes(), node.attribute(Attribute.TEXT.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.CONTENT_DESC.getName()).getValue())) {
            searchObjs.put(AttributeMask.DESCRIPTION.getDes(), node.attribute(Attribute.CONTENT_DESC.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.RESOURCE_ID.getName()).getValue())){
            searchObjs.put(AttributeMask.RESOURCEID.getDes(),node.attribute(Attribute.RESOURCE_ID.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.CHECKABLE.getName()).getValue())) {
            searchObjs.put(AttributeMask.CHECKABLE.getDes(), node.attribute(Attribute.CHECKABLE.getName()).getValue());
        }
        if(StringUtils.isNotEmpty(node.attribute(Attribute.CLICKABLE.getName()).getValue())) {
            searchObjs.put(AttributeMask.CLICKABLE.getDes(), node.attribute(Attribute.CLICKABLE.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.CHECKED.getName()).getValue())) {
            searchObjs.put(AttributeMask.CHECKED.getDes(), node.attribute(Attribute.CHECKED.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.ENABLED.getName()).getValue())) {
            searchObjs.put(AttributeMask.ENABLED.getDes(), node.attribute(Attribute.ENABLED.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.CHECKABLE.getName()).getValue())) {
            searchObjs.put(AttributeMask.CHECKABLE.getDes(), node.attribute(Attribute.CHECKABLE.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.FOCUSED.getName()).getValue())) {
            searchObjs.put(AttributeMask.FOCUSED.getDes(), node.attribute(Attribute.FOCUSED.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.LONG_CLICKABLE.getName()).getValue())) {
            searchObjs.put(AttributeMask.LONGCLICKABLE.getDes(), node.attribute(Attribute.LONG_CLICKABLE.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.SCROLLABLE.getName()).getValue())) {
            searchObjs.put(AttributeMask.SCROLLABLE.getDes(), node.attribute(Attribute.SCROLLABLE.getName()).getValue());
        }

        if(StringUtils.isNotEmpty(node.attribute(Attribute.SELECTED.getName()).getValue())) {
            searchObjs.put(AttributeMask.SELECTED.getDes(), node.attribute(Attribute.SELECTED.getName()).getValue());
        }

        return elementByMult(searchObjs);

    }

    /**
     *
     * [73,941][196,1038]
     * {"bottom":1038,"left":73,"right":196,"top":941}
     * 计算坐标
     */
    private void convertCoordinate(String boundsValue){

        String[] str = boundsValue.split("[^0-9]");

        List<Integer> list = new ArrayList<Integer>();

        for(String s : str){
            if(!s.isEmpty()){
                list.add(Integer.valueOf(s));
            }
        }
        List<Float> result = new ArrayList<Float>();

        Integer left = list.get(0);
        Integer top = list.get(1);
        Integer right = list.get(2);
        Integer bottom = list.get(3);

        Float x = 0f;
        Float y = 0f;

        y = Float.valueOf(String.valueOf((bottom - top) / 2 + top));

        x = Float.valueOf(String.valueOf((right - left) / 2 + left));

        result.add(x);
        result.add(y);

        atxContexts.setCoordinateInfo(result);
    }



    /**
     * 根据className查找
     * @param className
     * @return
     * @throws Exception
     */
    public ElementObj elementByClass(String className) throws Exception {
        JSONObject jsonObject = new JSONObject();

        List<Integer> fields = new ArrayList<Integer>();
        fields.add(AttributeMask.CLASS_NAME.getValue());

        jsonObject.put("mask", SortUtils.maskValue(fields));
        jsonObject.put(AttributeMask.CLASS_NAME.getDes(), className);

        List<JSONObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(jsonObject);

        boolean isExist = findElement(jsonObjectList);
        return isExist ? element : null;
    }

    /**
     *
     * 多元素查找匹配
     * 判断元素是否存在
     * 详细支持对象元素查看 AttributeMask 枚举类
     * @param searchParams
     * @return
     * @throws Exception
     */
    public ElementObj elementByMult(Map<String,Object> searchParams) throws Exception {
        JSONObject jsonObject = new JSONObject();
        List<Integer> fields = new ArrayList<Integer>();

        jsonObject.put("childOrSibling",new ArrayList<>());
        jsonObject.put("childOrSiblingSelector",new ArrayList<>());

        for (String key : searchParams.keySet()) {
            fields.add(AttributeMask.iterationFindByDes(key).getValue());
            jsonObject.put(AttributeMask.iterationFindByDes(key).getDes(),searchParams.get(key).toString());
        }

        jsonObject.put("mask", SortUtils.maskValue(fields));

        List<JSONObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(jsonObject);

        boolean isExist = findElement(jsonObjectList);
        return isExist ? element : null;

    }


    /**
     * 判断元素是否存在
     * @param wayToFind
     * @param value
     * @return
     * @throws Exception
     */
    public ElementObj waitForElement(AttributeMask wayToFind, String value) throws Exception {
        int count = 0;
        int timeLeft = Const.WAIT_ElEMENT_TIMEOUT;
        boolean satisfied = false;
        while (timeLeft > 0) {
            boolean elementExist = false;
            System.out.println(String.format("attempt to search the element for %d times", count++));
            elementExist = isElementExist(wayToFind, value);
            if (!elementExist) {
                // not find element ,keep searching
                this.sleep(Const.WAIT_ELEMENT_TIME_INTERVAL);
                timeLeft -= Const.WAIT_ELEMENT_TIME_INTERVAL;
            } else {
                // finded , break
                satisfied = true;
//                getElement(wayToFind, value);
                break;
            }
        }
        if (!satisfied) {
            System.out.println("can't find the element:" + value);
            return null;
        }
        return element;
    }

    /**
     * 休眠
     * @param ms
     * @return
     * @throws Exception
     */
    public AtxClient sleep(int ms) throws Exception {
        Thread.sleep(ms);
        return this;
    }

    /**
     * 元素是否存在
     * @param wayToFind
     * @param value
     * @return
     * @throws Exception
     */
    public boolean isElementExist(AttributeMask wayToFind, String value) throws Exception {
        try {


            JSONObject jsonObject = new JSONObject();
            List<Integer> fields = new ArrayList<Integer>();
            fields.add(wayToFind.getValue());

            jsonObject.put("mask", SortUtils.maskValue(fields));
            jsonObject.put("childOrSibling",new ArrayList<>());
            jsonObject.put("childOrSiblingSelector",new ArrayList<>());
            jsonObject.put(wayToFind.getDes(),value);

            List<JSONObject> jsonObjectList = new ArrayList<>();
            jsonObjectList.add(jsonObject);
            return findElement(jsonObjectList);
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }

//		return element.isDisplayed();
    }


    /**
     * 获取设备信息
     * @return
     */
    public DeviceInfo getDeviceInfo(){
        JSONObject result = JSONObject.fromObject(okHttpClientMethod.postByteMethod(atxContexts.getAtxHost() + Const.BASE_URI,ElementObj.baseRequestJson(MethodEnum.DEVICE_INFO.getValue(),new JSONObject())));

        DeviceInfo deviceInfo = new DeviceInfo();

        if(result.containsKey("result")){
            deviceInfo.setCurrentPackageName(result.getJSONObject("result").getString("currentPackageName"));
            deviceInfo.setDisplayHeight(result.getJSONObject("result").getInt("displayHeight"));
            deviceInfo.setDisplayWidth(result.getJSONObject("result").getInt("displayWidth"));
            deviceInfo.setDisplayRotation(result.getJSONObject("result").getInt("displayRotation"));
        }
        return deviceInfo;
    }

    /**
     * 获取当前app xml结构
     */
    public void dumpHierarchy(){
        List<Object> params = new ArrayList<Object>();
        params.add(false);
        params.add(null);
        JSONObject result = JSONObject.fromObject(okHttpClientMethod.postByteMethod(atxContexts.getAtxHost() + Const.BASE_URI, ElementObj.baseRequestJson(MethodEnum.DUMP_WINDOWS_HIERARCHY.getValue(),params)));
        FileMethodUtils.generateXML(Const.XML_PATH,result.getString("result"));

    }

    /**
     * 获取当前activity
     */
    public String getCurrentActivity(){
        Map<String,Object> headers = new HashMap<String,Object>();
        headers.put("Content-Type","application/x-www-form-urlencoded");

        StringBuffer commandStr = new StringBuffer();
        commandStr.append("dumpsys activity top");

        Map<String,Object> params = new HashMap<String,Object>();
        params.put("command",commandStr.toString());

        JSONObject result = JSONObject.fromObject(okHttpClientMethod.postMethod(atxContexts.getAtxHost()+Const.SHELL_URI,params,headers));
        if(result.containsKey("output")){
            String value = result.getString("output").split("\\n")[1];
            return value.split(" ")[3];
        }
        return null;
    }

    /**
     * 点击 按钮事件
     * @param keyEvent
     */
    public void press(String keyEvent){
        List<Object> params = new ArrayList<Object>();
        params.add(keyEvent);
        okHttpClientMethod.postByteMethod(atxContexts.getAtxHost() + Const.BASE_URI, ElementObj.baseRequestJson(MethodEnum.PRESS_KEY.getValue(),params));
    }


    /**
     * 截图
     * @param fileName
     * @throws Exception
     */
    public void takeScreenshot(final String fileName) throws Exception {
        okHttpClientMethod.downLoadMethod(atxContexts.getAtxHost() + Const.SCREENSHOT_URI, fileName);

    }

    /**
     * 启动服务
     */
    public void startUiAutomator(){
        okHttpClientMethod.postMethod(atxContexts.getAtxHost() + Const.UIAUTOMAROR_URI,new HashMap<String,Object>() ,new HashMap<String,Object>());
    }

    /**
     * 停止服务
     */
    public void stopUiAutomator(){
        okHttpClientMethod.deleteMethod(atxContexts.getAtxHost() + Const.UIAUTOMAROR_URI,null,new HashMap<String,Object>() ,new HashMap<String,Object>());
    }


    /**
     * 设置输入法
     */
    public void setKeybord(String keybord){

        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setRemoteHost(atxContexts.getAtxHost());

        String keyboardList = ShellUtils.getInstance(desiredCapabilities).shellPost("ime list -s");

        System.out.println(keyboardList.split("\\n")[0]);
        System.out.println(keyboardList.split("\\n")[1]);
        System.out.println(keyboardList.split("\\n")[2]);

        for(String str : keyboardList.split("\\n")){
            if(str.equals(keybord)){
                ShellUtils.getInstance(desiredCapabilities).shellPost("ime set " + keybord );
            }
        }
    }

}
