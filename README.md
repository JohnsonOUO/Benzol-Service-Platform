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
* Restart System
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
Before we build Benzol service, we need a cloud system. We have many choices to build a cloud system(Minikube, k3s or k8s). In this document, I used talos on the proxmox for our private cloud. You can follow this https://github.com/Ninox-RD/Edge-Cloud to build a cloud.(00-Prepare、01-VMCluster)


## Deploy Thingsboard
### Introduction
In this step, we will deploy storageClass, kafka, thingsboard, postgres, ingress and so on in this step.
We can follow instructions to deploy our service.
```script=
## Get resource
cd ~
git clone https://github.com/NinoX-RD/Benzol-Service-Platform.git

## Set Infra
cd ~/Benzol-Service-Platform/Thingsboard/01-metallb
kubectl apply -k .
cd ~/Benzol-Service-Platform/Thingsboard/02-nginx
kubectl apply -k .
cd ~/Benzol-Service-Platform/Thingsboard/03-kubed
kubectl apply -k .
cd ~/Benzol-Service-Platform/Thingsboard/04-cert-manager
kubectl apply -k .
cd ~/Benzol-Service-Platform/Thingsboard/05-StorageClass
kubectl apply -k .

## Then we can deploy thingsboard
cd ~/Benzol-Service-Platform/Thingsboard
./k8s-install-tb.sh --loadDemo ## wait for a few minutes
./k8s-deploy-thirdparty.sh ## zookeeper kafka redis
./k8s-deploy-resources.sh ## transport main-pod

## Set mqtt service for nginx
cd ~/Benzol-Service-Platform/Thingsboard
kubectl apply -f tcp-services.yaml

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
> If you installed DataBase with demo data (using --loadDemo flag) you can also use the following credentials

* Tenant Administrator: 
tenant@thingsboard.org / tenant
* Customer User: 
customer@thingsboard.org / customer
<div STYLE="page-break-after: always;"></div>
## MQTT Test
We will create a device first.
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

## Customize UI
### Introduction
After we deploy the platform, we need to change css and html for our Benzol Service. We should modify files in the pod of web-ui.
we can use kubecp to get the css file or you can use modified file in https://github.com/NinoX-RD/Benzol-Service-Platform.git/cus_tb
### Quick Changes
```script=
cd ~/Benzol-Service-Platform/cus_tb
## Get web-ui pod name
kubectl get po (find web-ui name)

## replace $name in kubecp.sh with new web-ui name
nano ~/Benzol-Service-Platform/cus_tb/kubecp.sh
bash kubecp.sh
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

## Restart System
If your nodes are shutdown, we have two ways to restart nodes.
* Proxmox
* Terraform

### Proxmox
Open the proxmox website. Choose each vm and click "start".
In the script.
```script=
qm start vm_id
```
### Terraform
We need to check main.tf pm_api_url is the same as your proxmox_ip first. Then use terraform to rebuild.
```script=
## check api_url
nano ~/Edge-Cloud/01-VMCluster/main.tf
## rebuild
terraform apply 
```
There is a issue we met before, but we can not reproduce it.
When we wanted to restart the node and we did not change api_url, the command "terraform apply" was failed and showed the error like this.
```script=
╷
│ Error: Plugin did not respond
│ 
│   with module.scope.aws_vpc_ipam_scope.vpc_ipam_scope,
│   on ../../terraform-modules/terraform-aws-vpc-ipam-scope/main.tf line 12, in resource "aws_vpc_ipam_scope" "vpc_ipam_scope":
│   12: resource "aws_vpc_ipam_scope" "vpc_ipam_scope" {
│ 
│ The plugin encountered an error, and failed to respond to the plugin.(*GRPCProvider).ReadResource call. The plugin logs may contain more details.
```
I thought that provider which was changed caused a problem. I found a method to solve this problem. we could use
```script=
terraform state replace-provider OLD_PROVIDER NEW_PROVIDER
```
Maybe it will work.
Reference: https://github.com/hashicorp/terraform/blob/main/website/docs/cli/commands/state/replace-provider.mdx
