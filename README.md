# Benzol-Service-Platform
### Introduction
  Benzol Service is a cloud service platform used to collect edge device information. The goal of this document is demonstrate the totle operations of creating Benzol Service. 
* Set up K8s
* Deploy Thingsboard (Version 3.4.4)
* Setting Connection
* Account Info
* Thingsboard Operation
* Mock Device connection
* OTA update
* IoT Gateway
* Customize UI
### Version
* Proxmox v7.2
* kubernetes v1.22.8
* talos v1.0.0
* thingsboard v3.4.4
* thingsboard-gateway(in kv) v2.5.2
* thingsboard-gateway(in Ubuntu) v2.9
<div STYLE="page-break-after: always;"></div>

## Set up k8s 
### Introduction
Before we build Benzol service, we need a cloud system. We have many choices to build a cloud system(Minikube, k3s or k8s). In this document, I used talos on the proxmox for our private cloud. You can follow this https://github.com/Ninox-RD/Edge-Cloud to build a cloud.

## Deploy Thingsboard
### Introduction
In this step, we will deploy storageClass, kafka, thingsboard, postgres, ingress and so on in this step.
We can follow instructions to deploy our service.
```script=
## Get resource
cd ~
git clone https://github.com/NinoX-RD/Benzol-Service-Platform.git

## First we need to deploy StorageClass
cd ~/Benzol-Service-Platform/Thingsboard/rancher-local-path-provisioner
kubectl apply -k .

## Then we can deploy thingsboard
cd ~/Benzol-Service-Platform/Thingsboard
./k8s-install-tb.sh --loadDemo ## wait for a few minutes
./k8s-deploy-thirdparty.sh
./k8s-deploy-resources.sh

## Set up others
cd ~/Benzol-Service-Platform/Thingsboard/01-metallb
kubectl apply -k .
cd ~/Benzol-Service-Platform/Thingsboard/02-nginx
kubectl apply -k .
cd ~/Benzol-Service-Platform/Thingsboard/03-kubed
kubectl apply -k .
cd ~/Benzol-Service-Platform/Thingsboard/04-cert-manager
kubectl apply -k .
```
<div STYLE="page-break-after: always;"></div>

## Setting Connection
### Introduction 
Because we used kubernetes as a cloud management, Our computer can not connect to our service directly. Hence, we can only connect to Proxmox and we set up ip rules(iptables) on Proxmox to do prerouting to our services.
Port 80, 443 is for http and https. Port 1883 is for mqtt transport. 
```script=
## Create Rule
iptables -t nat -A PREROUTING -i wlp2s0 -p tcp --dport 80 -j DNAT --to 10.20.0.210
iptables -t nat -A PREROUTING -i wlp2s0 -p tcp --dport 443 -j DNAT --to 10.20.0.210
iptables -t nat -A PREROUTING -i wlp2s0 -p tcp --dport 1883 -j DNAT --to 10.20.0.210

## Delete Rules (If you set a wrong rule, you can delete this rule)
iptables -t nat -D PREROUTING -i wlp2s0 -p tcp --dport 80 -j DNAT --to 10.20.0.210

## Set DNS
nano /etc/hosts
10.20.0.210 thingsboard.ninox
```
In your Host
```script=
nano /etc/hosts
10.1.2.168 thingsboard.ninox
```
## Account Info
* System Administrator: 
sysadmin@thingsboard.org / sysadmin
If you installed DataBase with demo data (using --loadDemo flag) you can also use the following credentials:

* Tenant Administrator: 
tenant@thingsboard.org / tenant
* Customer User: 
customer@thingsboard.org / customer
<div STYLE="page-break-after: always;"></div>

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

After modifying fiels, we need to restart thingsboard-gateway
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

## Customize UI
### Introduction
After we deploy the resource, we need to change something for our Benzol Service. We should modify files in the pod of web-ui.
we can use kubecp to get the css file or you can use modified file in https://github.com/NinoX-RD/Benzol-Service-Platform.git/cus_tb
### Quick Changes
```script=
cd ~/Benzol-Service-Platform/cus_tb
## Get web-ui pod name
kubectl get po (find web-ui name)

## replace $name in kubecp.sh with new web-ui name
nano ~/Benzol-Service-Platform/cus_tb/kubecp.sh
bash kubecp.sh

## access web-ui pod
kubectl exec -it pod/${pod_name} -- bash

##put modified files to each origin place.
cp modify_6610 web/publish/6610.XXXXXXXXX.js
- modify_6610 -> web/publish/6610.XXXXXXXXX.js
- modify_index -> web/publish/indes.XXXXXXX.html
- modify_main -> web/publish/main.XXXXXXX.js
- modify_style -> web/publish/style.XXXXXXX.css
- thingsboard.ico -> web/publish/thingsboard.ico
- alpha_svg -> web/publish/assets/logo_title_white.svg
```
## Details about web-ui (Option)
If you want to change color , picrute or name you can use followings to do your modifications.
"mat-toolbar.mat-primary 1/6" means use Alt+F find the sentance "mat-toolbar.mat-primary" matches 6,and we will change a color in the 1st match.
### Change primary toolbar color
In web/public/style.css 
find the "mat-toolbar.mat-primary 1/6"
### Change dashboard toolbar color
In web/public/style.css 
find the ".tb-default mat-fab-toolbar .mat-fab-toolbar-background  1/5"
### Change home button color
In web/public/style.css 
find the "mat-raised-button.mat-primary 3/10"
mat-raised-button.mat-primary
### Change web head name
In web/public/index.html
find body->title
### Remove Watermark
In web/public/6610.js
find "ut.thingsboardVersion" delete those words
### Change web destination URL
In web/public/main.js
find "thingsboard.io" replace all of them with ninox.ai
### Change logo picture
In web/public/assets/logo_title_white.svg
change /usr/share/tb-web-ui/web/public/assets/logo_title_white.svg
### Change web icon
In web/public/thingsboard.ico
change /usr/share/tb-web-ui/web/public/thingsboard.ico