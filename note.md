# 802.11报文格式


## 从抓取的报文中分辨无线协议类型

1. 一般取beacon帧，以及probe response帧
2. 打开IEEE 802.11 Wireless Management->Tagged Parameters
3. 有看到EHT Capabilities信息, 说明支持802.11be
4. 有看到HE Capabilities信息, 说明支持802.11ax
5. 有看到VHT Capabilities信息, 说明支持是802.11ac
6. 有看到HT Capabilities信息, 说明支持80211n

## 从抓取的报文中分辨SSID名称

1. 打开IEEE 802.11 Wireless Management->Tagged Parameters->Tag: SSID parameter->SSID: "@eg310组网海外"

## 从抓取的报文中分辨频宽

1. 打开IEEE 802.11 Wireless Management->Tagged Parameters->Tag: VHT Operation->VHT Operation Info
2. 0: 20/40
3. 1: 80
4. 2: 160
5. 3: 80+80

Tag: HT Information (802.11n D1.10)->Primary Channel: 指示主信道
Tag: HT Information (802.11n D1.10)->HT Information Subset (1 of 3): .... ..01 -> 使用40频宽， 次要信道在上面
Tag: HT Information (802.11n D1.10)->HT Information Subset (1 of 3): .... ..11 -> 使用40频宽， 次要信道在下面
Tag: HT Information (802.11n D1.10)->HT Information Subset (1 of 3): .... ..00 -> 使用20频宽

## 从报文中分辨使用的信道

1. 打开IEEE 802.11 Wireless Management->Tagged Parameters->Tag: DS Parameter Set->Channel: 36

## 解析方案

1. 手动解析， 使用pcap4j，通过packet.getRawData()获取byte[]
2. 定位到tagged parameters
   1. radiotap header 有header length, 占2字节，通过网络字节序解析, 位置固定0x0002, 0x0003， 定位到beacon帧头部
   2. 帧头部固定24字节， fixed parameters固定12字节， 后续直至结束都是tagged parameters
3. 解析tagged parameters
   1. 指针从tagged parameters第一位开始， 一定为tag number， 占1字节， 下面一个字节为长度，指针接下去跳转到长度后面
   2. 如果新的tag number为0xff， 那么是ext tag, 下一字节为ext tag 长度， 通过长度跳转到下一tag