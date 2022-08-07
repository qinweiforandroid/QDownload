# QDownload
QDownload是基于Android平台实现的下载框架。API简洁易上手，只需5分钟即可实现一个多任务、多线程、断点下载的功能

支持功能如下：

1. 支持添加下载任务，暂停下载，恢复下载，取消下载
2. 支持多个下载任务同时下载
3. 单个任务支持开多个线程下载
4. 支持断点下载，在断网、进程被划掉可恢复下载
5. 自动校验服务器文件服务器是否支持断点下载，如果不支持则会开启单线程任务下载
6. 支持应用全局监听下载进度回调
7. 支持下载速度显示
8. 支持批量暂停，批量恢复下载

先贴个效果图

**demo主页**

 <img src="https://img-blog.csdnimg.cn/img_convert/d74438b7f8f7a5710fd023887359a1a6.png"   width="30%">

**多任务多线程断点下载页面**

 <img src="https://img-blog.csdnimg.cn/img_convert/709186fda0c9011c60e5daa9641b161f.png"   width="30%">

## 1、Quick Setup

**Step 1.** Add it in your root build.gradle at the end of repositories

```groovy
allprojects {
    repositories {
        ...
            maven { url 'https://jitpack.io' }
    }
}
```

**Step 2.** Add the dependency [![](https://jitpack.io/v/qinweiforandroid/QDownload.svg)](https://jitpack.io/#qinweiforandroid/QDownload)

```groovy
dependencies {
  implementation 'com.github.qinweiforandroid:QDownload:2.0.0807'
}
```

**Step 3.** init

在application中初始化

```java
public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        //下載全局配置
        DownloadConfig.init(new DownloadConfig.Builder()
                .setConnectTimeout(10000)//连接超时时间
                .setReadTimeout(10000)//读取超时时间
                .setMaxTask(3)//最多3个任务同时下载
                .setMaxThread(3)//1个任务分3个线程分段下载
                .setAutoResume(true)//启动自动恢复下载
                .setRetryCount(3)//单个任务异常下载失败重试次数
                .setDownloadDir(getExternalCacheDir().getAbsolutePath())
                .builder());
        //初始化下载组件(可在子线程中做)
        DownloadManager.init(this);
    }    
}
```

**Step 4.** 添加一个下载任务

```java
public class SingleDownloadActivity extends AppCompatActivity {
    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onChanged(DownloadFile e) {
            if (TextUtils.equals(id, e.getId())) {
                //更新ui
                mSingleDownloadInfoLabel.setText(e.getId() + "\n" + e.getCurrentLength() + "/"
                        + e.getContentLength());
            }
        }
    };
    
    public void addDownload(){
        //todo check perimission
        //step 1 创建request
        FileRequest.create(id)// 生成一个唯一id
            .setRange(false)//不适用断点下载
            .setName("weixin_680.apk")//设置下载的文件名称
            .setUrl("http://gdown.baidu.com/weixin_680.apk")//设置下载链接
            .setDir(getExternalCacheDir().getAbsolutePath())//设置下载的文件路径
            .addDownload();//执行下载
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        //注册下载监听器
        DownloadManager.addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadManager.removeObserver(watcher);
    }
}
```

**step 5.** 获取下载文件信息

可以根据下载的 **id** 获取文件信息

```java
public static DownloadFile getFile(String id) {
    return DownloadManager.getFile(id);
}
```

文件信息如下：

```java
public interface DownloadFile extends Serializable {
    String getId();//下载文件id
    String getPath();//文件地址
    String getUrl();//远程文件地址
    long getCurrentLength();//当前进度
    long getContentLength();//文件大小
    long getSpeed();//下载速度
    boolean isDone();//是否已下载
    boolean isConnecting();//连接中
    boolean isDownloading();//下载中
    boolean isPaused();//暂停
    boolean isWait();//等待下载
    boolean isError();//下载失败
    boolean isIdle();//文件处于空闲，即将下载
}
```



## 2、入门用法

通过DownloadEntry构建下载实体

```java

public void addDownload(String id,String url){
    //id为任务的一个唯一标识，url为文件地址
    DownloadEntry entry = DownloadEntry.obtain(id,url)
    //添加一个下载任务
    DownloadManager.add(entry)    
}
```

**暂停下载**

```java
public void pauseDownload(String id){
    //添加一个下载任务
   FileRequest.create(id).pauseDownload(); 
}
```

**恢复下载**

```java
public void resumeDownload(DownloadEntry entry){
    FileRequest.create(apk.id()).resumeDownload();
}
```
**暂停所有下载任务**
```java
public void pauseAll(){
    DownloadManager.pauseAll(entry)    
}
```
**恢复所有下载任务**

```java
public void recoverAll(){
    //添加一个下载任务
    DownloadManager.recoverAll(entry)    
}
```



## 3、项目实战

1.6、应用市场apk下载的一个场景

ApkEntry实体数据用来描述apk的基本信息

```java
public class ApkEntry {
    public String id;//包id
    public String cover;//apk 图标
    public String name;//apk 名称
    public String url;//apk 下载地址
    public long length;//apk 大小

    public String id() {
        if (TextUtils.isEmpty(id)) {
            //如果服务端没有返回唯一标记则用url的md5值作为下载唯一标识
            id = FileUtilities.getMd5FileName(url);
        }
        return id;
    }
}
```

这是我要下载这个apk就可以这么做

```java
public void downloadApk(ApkEntry apk){
    //1、先check当前apk是否在下载
    DownloadFile file = FileRequest.getFile(apk.id());
    if (file == null || file.isIdle()) {
        FileRequest.create(apk.id())
            .setName(apk.name+".apk")
            .setUrl(apk.url)
            .addDownload();
    }
}
```

暂停apk下载
```java
public void pauseDownloadApk(ApkEntry apk){
    //1、先check当前apk是否在下载任务中
    DownloadFile file = FileRequest.getFile(apk.id());
    if(file!=null){
        FileRequest.create(id).pauseDownload(); 
    }
}
```

更多功能请参考demo中实现<img src="png\519C949C.png" alt="img" style="zoom:50%;" />

在对接过程中如有遇到问题也可以+我QQ：435231045