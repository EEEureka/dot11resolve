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
- Java 8 或更高版本
- Maven 3.6 或更高版本

### 安装步骤
1. 克隆项目代码：
    ```bash
    git clone https://github.com/3iuy-prog/Dot11Scanner.git
    ```
2. 进入项目目录：
    ```bash
    cd pcapresolver
    ```
3. 使用Maven构建项目：
    ```bash
    mvn clean install
    ```

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

## 贡献指南
欢迎对本项目进行贡献！如果你有任何建议或发现了问题，请提交Issue或Pull Request。

### 提交代码
1. Fork本仓库
2. 创建你的分支 (`git checkout -b feature-branch`)
3. 提交你的修改 (`git commit -am 'Add new feature'`)
4. 推送到分支 (`git push origin feature-branch`)
5. 创建Pull Request