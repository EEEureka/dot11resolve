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

beacon帧对象存储在`Dot11Beacon`类中，包含以下信息
- `bssid`：基本服务集标识符
- `ssid`：服务集标识符
- `channel`：信道
- `rssi`：接收信号强度指示
- `timestamp`：时间戳
- `beaconInterval`：信标间隔
- `capability`：能力信息
- `taggedParameters`：标签参数
- .11n/ac/ax/be对应的操作帧
- `frameSerial`：帧序号
- `stationCount`：关联的设备数量
- `channelUsage`：信道利用率
- `wpaInfo`：WPA信息
- `rsnInfo`：RSN信息
- `authMethods`：认证方法
- `isESS`：是否为ESS

## 贡献指南
欢迎对本项目进行贡献！如果你有任何建议或发现了问题，请提交Issue或Pull Request。

### 提交代码
1. Fork本仓库
2. 创建你的分支 (`git checkout -b feature-branch`)
3. 提交你的修改 (`git commit -am 'Add new feature'`)
4. 推送到分支 (`git push origin feature-branch`)
5. 创建Pull Request