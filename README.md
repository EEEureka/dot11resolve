# pcapresolver

## 项目简介
`pcapresolver` 是一个用于解析和处理PCAP文件的Java项目。该项目旨在提供高效、可靠的PCAP文件解析功能，帮助用户从网络捕获文件中提取和分析数据。

## 功能特性
- 支持解析PCAP文件格式
- 提供数据包过滤和提取功能
- 支持多种网络协议解析
- 高效的内存和性能管理

## 安装与使用
### 环境要求
- Java 21
- Maven 3.6 或更高版本

### 使用示例
以下是一个简单的使用示例，展示如何解析PCAP文件并提取数据包信息：
```java
import com.example.pcapresolver.PcapParser;

public class Main {
    public static void main(String[] args) {
        Resolver resolver = new Resolver("/path/to/your/pcap/file.pcap");
        resolver.resolve();
        // then the result can be accessed by resolver.beaconSourceData
        List<DOT11Beacon> sourceData = resolver.beaconSourceData;
        // beacons might be duplicated, use resolver.bssidDeduplication() to deduplicate
        sourceData = resolver.bssidDeduplication();
    }
}
```

### beacon帧对象存储

beacon帧对象存储在`Dot11Beacon`类中，以下是该类的定义:

```java

public class Dot11Beacon {
    public int channel;
    public byte[] BSSID;
    public String SSID;
    public Bandwidth BW;
    public Band band;
    public Protocol protocol;
    public Tag vhtTag = null; // 802.11ac(若支持)
    public Tag htTag = null; // 802.11n(若支持)
    public Tag ehtTag = null; // 802.11be(若支持)
    public Tag heTag = null; // 802.11ax(若支持)
    public List<Tag> tags; // beacon帧中携带的tag
    public int frameSerial; // beacon帧在pcap文件中的序列
    public int stationCount; // 终端数量, 从beacon中的QBSS Load Element中获取(如有)
    public int channelUsage; // 信道利用率，保存的值为0-255， 利用率 = channelUsage / 255, 也是从QBSS Load Element中获取(如有)
    public WPAInfo wpaInfo = null; // WPA信息(若支持) 加密相关
    public RSN rsnInfo = null; // RSN信息(若支持) 加密相关

    public Dot11Beacon(int channel, byte[] BSSID, String SSID, Bandwidth BW, Band band, Tag vhtTag, Tag htTag,
            Tag ehtTag, Tag heTag, List<Tag> tags, Protocol protocol, int frameCount, int staCount, int channelUsage,
            WPAInfo wpaInfo, RSN rsnInfo) {
        this.channel = channel;
        this.BSSID = BSSID;
        this.SSID = SSID;
        this.BW = BW;
        this.band = band;
        this.vhtTag = vhtTag;
        this.htTag = htTag;
        this.ehtTag = ehtTag;
        this.heTag = heTag;
        this.tags = tags;
        this.protocol = protocol;
        this.frameSerial = frameCount;
        this.stationCount = staCount;
        this.channelUsage = channelUsage;
        this.wpaInfo = wpaInfo;
        this.rsnInfo = rsnInfo;
    }

    private void printBSSID() {
        System.out.print("BSSID: ");
        int i = 0;
        for (byte b : BSSID) {
            if (i++ == 5) {
                System.out.print(Integer.toHexString(b & 0xFF));
            } else {
                System.out.print(Integer.toHexString(b & 0xFF) + ":");
            }
        }
        System.out.println();
    }

    public void printThis() {
        System.out.println("No." + frameSerial);
        System.out.println("Channel: " + channel);
        this.printBSSID();
        System.out.println("SSID: " + SSID);
        System.out.println("BW: " + BW);
        System.out.println("Band: " + band);
        System.out.println("Protocol: " + protocol);
        System.out.println();
        System.out.println();
    }
}
```

## 贡献指南
欢迎对本项目进行贡献！如果你有任何建议或发现了问题，请提交Issue或Pull Request。

### 提交代码
1. Fork本仓库
2. 创建你的分支 (`git checkout -b feature-branch`)
3. 提交你的修改 (`git commit -am 'Add new feature'`)
4. 推送到分支 (`git push origin feature-branch`)
5. 创建Pull Request