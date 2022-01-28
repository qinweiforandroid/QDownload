# QDownload
QDownload是基于Android平台实现的下载框架。API简洁易上手，只需5分钟即可实现一个多任务、多线程、断点下载的功能

支持功能如下：

* 支持多个下载任务同时下载
* 单个任务支持开多个线程下载
* 支持断点下载，在断网、进程被划掉可恢复下载
* 自动校验服务器文件服务器是否支持断点下载，如果不支持则会开启单线程任务下载
* 支持应用全局监听下载进度回调
* 支持下载速度显示
* 支持添加下载任务，暂停下载，恢复下载，取消下载
* 支持批量暂停，批量恢复下载

先贴个效果图

**demo主页**

<img src="/png/device-2022-01-28-204742.png" style="zoom:33%;text-align:left" />

**多任务多线程断点下载页面**

<img src="/png/device-2022-01-28-204810.png" style="zoom:33%;" />

## 1、如何使用

### 1.1、导入依赖

```groovy
implementation 'com.qw.download:download:1.0.0-alpha01'
```

### 1.2、初始化下载组件

```java
public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化下载组件(可在子线程中做)
        DownloadManager.init(this);
    }    
}
```

### 1.3、核心控制器DownloadManager

api如下

```java
public class DownloadManager {
    private static DownloadManager mInstance;
    private final Context context;
    
    private DownloadManager(Context context) {}
    //初始化组件
    public static void init(Context context) {}
    //开启下载
    public static void add(DownloadEntry entry) {}  
    //暂停下载
    public static void pause(DownloadEntry entry) {}    
    //暂停所有任务
    public static void pauseAll() {}
    //恢复下载
    public static void resume(DownloadEntry entry) {}   
    //恢复所有任务
    public static void recoverAll() {}
}
```

### 1.4、监听下载进度

需要监听下载进度可通过注册DownloadWatcher来监听下载信息的变化

```java
private DownloadWatcher watcher = new DownloadWatcher() {
    @Override
    protected void onDataChanged(DownloadEntiry entry) {
	    //这里监听下载的实时信息
        mDownloadInfoLabel.setText(entry.toString());
        Log.e("MainActivity", entry.toString());
    }
};

@Override
protected void onResume() {
    super.onResume();
    //注册观察者
    DownloadManager.addObserver(watcher);
}

@Override
protected void onPause() {
    super.onPause();
    //移除观察者
    DownloadManager.removeObserver(watcher);
}
```

其中第**3**行 `DownloadEntiry entry` 存储了下载相关信息

```java
public class DownloadEntry implements Serializable {
    public String id;//下载的唯一标识
    public String url;//下载文件的url地址
    public boolean isSupportRange;//是否支持断点续传
    public long contentLength;//文件长度
    public long currentLength;//已下载文件长度
    public State state;//任务状态
    public HashMap<Integer, Long> ranges;//存储每个线程下载开始区块
    public int speed;//下载速度 单位s
}
```

其中state有如下几个值

```java
public enum State {
    IDLE,//空闲
    CONNECT,//连接中(用户预取文件信息)
    ING,//下载中
    PAUSED,//已暂停
    CANCELLED,//已取消
    ERROR,//错误
    DONE,//完成
    WAIT//等待
}
```



### 1.5、下载相关的操作

**添加一个下载**

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

可通过下载任务id  然后调用`DownloadManager.findById(id)` 函数获取DownloadEntry

```java
public void pauseDownload(DownloadEntry entry){
    //添加一个下载任务
    DownloadManager.pause(entry)    
}
```

**恢复下载**
```java
public void resumeDownload(DownloadEntry entry){
    //添加一个下载任务
    DownloadManager.resume(entry)    
}
```
**暂停所有下载任务**
```java
public void pauseAll(DownloadEntry entry){
    //添加一个下载任务
    DownloadManager.resume(entry)    
}
```
**恢复所有下载任务**
```java
public void recoverAll(DownloadEntry entry){
    //添加一个下载任务
    DownloadManager.recoverAll(entry)    
}
```



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
public void downloadApk(ApkEntry apkEntry){
    //1、先check当前apk是否在下载
    DownloadEntry entry = DownloadManager.findById(apkEntry.id())
    if(entry==null || entry.state==State.PAUSED || entry.state==State.ERROR || entry.state==State.CANCELLED){
        //这四种情况：没有下载任务 || 任务是暂停 || 下载失败 || 任务已被取消
        DownloadManager.add(entry)
    }
}
```

暂停apk下载
```java
public void pauseDownloadApk(ApkEntry apkEntry){
    //1、先check当前apk是否在下载任务中
    DownloadEntry entry = DownloadManager.findById(apkEntry.id())
    if(entry!=null){
        DownloadManager.pause(entry)
    }
}
```



更多功能请参考demo中实现<img src="png\519C949C.png" alt="img" style="zoom:50%;" />

在对接过程中如有遇到问题也可以+我QQ：435231045