## Deploy Nginx
```
kubectl apply -k .
```
## Add IP & Name in proxmox etc/hosts
```
10.20.0.210 gitea.ninox.com harbor.ninox.com argocd.ninox.com argoworkflow.ninox.com
```
## Add IP & Name in Your etc/hosts
```
{ProxmoxIP} gitea.ninox.com harbor.ninox.com argocd.ninox.com argoworkflow.ninox.com
```
