## Benzol Service Operation
### Introduction
After the service platform is deployed, we want to collect edge telemetrics and show them on the screen. Thus, We should set up a device to accept telemetry, then we can show data on the dashboard.
Reference: https://thingsboard.io/docs/getting-started-guides/helloworld/?connectdevice=mqtt-linux
### Create Device
![](https://i.imgur.com/u37WbZ3.png)
*set name and profile(defualt or set by youself)*
![](https://i.imgur.com/nLvwqVJ.png)
*Get Access Token. You can use default or change the token in your mind*
![](https://i.imgur.com/wIm8kjF.png)
<div STYLE="page-break-after: always;"></div>

We can use instruction to simulate devices' infomation.
```script=
## Install mqtt-client
sudo apt-get install mosquitto-clients

## test mqtt connection
mosquitto_pub -d -q 1 -h "${ProxmoxIP}" -p "1883" -t "v1/devices/me/telemetry" -u "${AccessToken}" -m {"temperature":25}
```

Then we can see the result on the web.
![](https://i.imgur.com/aTpWh4T.png)


### Create dashboard
*Click and set your title*
![](https://i.imgur.com/GFpVQ3M.png)
*Set entity alias with your device.*
![](https://i.imgur.com/tqbr5Sp.png)
*Then you can add widget to show your graphs and data.*
<div STYLE="page-break-after: always;"></div>

### Set Alarm rule
If we want to set up alarm, we need to set a rule in device profile.
![](https://i.imgur.com/H92xBQv.png)
#### note: 
* Device should use this device profile
* If we create alarm rules, we must set a clear alarm rule.
*(Otherwise, this rule will not warn mutiple times until you clear it manually.)*  

## Mock Device (OPTION)
### Introduction
Here is the transmission of data to simulate edge devices.
There are two different code to do the same thing.
1. Python (prefer)
In .Benzol-Service-Platform/Thingsboard/Mock/mqtt.py
```script=
cd ~/Benzol-Service-Platform/Thingsboard/Mock
python3 mqtt.py
```
In this python file , we need to modify several settings.
```python=
broker_addr = "10.1.2.168" ## your IP
client.username_pw_set("55688") ## your device token
```
2. Java

  Reference: https://bytesofgigabytes.com/thingsboard/sending-data-to-thingsboard-using-java/

![](https://i.imgur.com/RUOASS4.png)
<div STYLE="page-break-after: always;"></div>

## OTA update
### Introduction
An over-the-air (OTA) update is the wireless delivery of new software, firmware or other data to mobile devices. In this case, we will upload data from our service to edges. Before we start it, we need to know that Nginx and Web-node have size limited about ota upload files. For Nginx we must set annotation in thingsboard ingress to increase file size. Then Web-node we need to set enviroment variable to increase upload size. (All of things were finished when you deployed the service.)

***Know we have three steps to do OTA update.***
### 1. Upload firmware to the service
![](https://i.imgur.com/o83skjf.png)
#### Note: 
* Title must be same as file's name
* version: Everything (eq.: v1.0)
* Device prfile: It should be same as device profile.
* browse file: Choose file 

### 2. Edit device's firmware
![](https://i.imgur.com/bAAr9Zu.png)
### 3. Run update on Device
Use this python code in the edge (from Appaul) , and we can see firmware status from initated -> Downloading -> Downloaded -> Verified -> Updating.
<div STYLE="page-break-after: always;"></div>

## IoT Gateway Set Up
### Introduction
IoT gateway is installed on the edge. We can use gateway to collect all data of edge devices, and we can tranmiss to our service.
Now, we have two ways to install thingsboard-gateway
1. Install on Kv260
We can use petalinux bps 2022.1 (there is thingsboard-gateway in this bps), or you can use yocto to build this reciepe.This document will not focus on build layer.

2. Install on Ubuntu
We will use this doc https://thingsboard.io/docs/iot-gateway/install/deb-installation/ 
#### Note: we need to use version v2.9 not latest one.
```script=
## Get 2.9 deb package
wget https://github.com/thingsboard/thingsboard-gateway/release/download/2.9/python3-thingsboard-gateway.deb

## install this deb package
sudo apt install ./python3-thingsboard-gateway.deb -y

## Check gateway status
systemctl status thingsboard-gateway
```

After Install gateway, we will create a gateway device.
![](https://i.imgur.com/6fLYMEA.png)
#### Note: Check "Is gateway"

In gateway. We will set up two files to make a connection.
/etc/thingsboard-gateway/config/mqtt.json
![](https://i.imgur.com/PHbJpND.png)
#### Note:
* Broker will set 127.0.0.1 (Broker is in local)
* topicFilter: It was a topic when you send your message
* serialNumber: your device's name
* sensorType: your device's profile
* attributes and timeseries can be created or deleted. 
* key & value: Show on the web 
* 
The other file /etc/thingsboard-gateway/config/tb_gateway.yaml
![](https://i.imgur.com/PahaZQd.png)
#### Note:
* host should be thingsboard host
* remoteShell: true
* accessToken: use your gateway device's token.

After modifying files, we need to restart thingsboard-gateway
```script=
## Restart
sudo systemctl restart thingsboard-gateway

## Check
systemctl status thingsboard-gateway
```
Test thingsboard-gateway worked or not
```python3=
## use python to send mqtt
import paho.mqtt.client as mqtt
import json
import random
import time

broker_addr = "127.0.0.1" # local Broker
client = mqtt.Client()
client.connect(broker_addr,1883)
payload = {"serialNumber":"AutoCar","sensorType":"Car","sensorModel":"Yolo","hum":20,"time":60}
print(json.dumps(payload))
client.publish("/sensor/date",json.dumps(payload),qos=1)
```
Then we can see service will create a new device with thoose telemetrics.
#### Debug
If you want to see more Info
```script=
## Check gateway service
systemctl status thingsboard-gateway
```
<div STYLE="page-break-after: always;"></div>
