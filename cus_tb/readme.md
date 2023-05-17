# Customize UI
In Benzol service, we have two choices to cusromize web ui.
First, we will customize before we package it. The other one is after we package. Now we will introduce how to package and how to modify files before or after package.
## Prepare
We need to get resources from thingsboard github.
```script=
cd ~
git clone https://github.com/thingsboard/thingsboard.git -b release-3.4
```
Before packaging, we need to install maven first. If you have already installed it, you can use it to check version.
```script=
## check version
mvn -v
```
## How to Package
Here, we will **learn how to package** first in order to ensure the source code is worked.
If we want to change the web ui. Hence, **we need to repackage ui-ngx and msa folder after you modify those files.**
**Note:** 
  Because msa will use the package which created by ui-ngx, we should package ui-ngx first.
```
cd ~/thingsboard/ui-ngx
mvn clean install -DskipTests=true
```
Then, we can package msa folder
```script=
cd ~/thingsboard/web-ui
mvn clean install -DskipTests=true -Ddockerfile.skip=false
```
If we success, we can see our docker images. There is thingsboard/web-ui.
```script=
## see docker image
docker images
```
## Modify before Package
Now we can customize our ui, and repackage ui-ngx and mas. Then we can get our web-ui image.
### Primary Color
In  ~/thingsboard/ui-ngx/src/theme.scss
find $tb-primary-color
### Web icon
In ~/thingsboard/ui-ngx/src
replace thingsboard.ico with your comapny icon.
### Web Logo
In ~/thingsboard/ui-ngx/src/assets
replace logo_title_white.svg with your company logo.
### Dashboard toolbar color
In ~/thingsboard/ui-ngx/src/them.scss
find
```css=
&.mat-primary {
      .mat-fab-toolbar-background {
        background: mat.get-color-from-palette($primary);
        color: mat.get-color-from-palette($primary, default-contrast);
      }
    }
```
replace  mat.get-color-from-palette($primary) with #3a263a
```css=
&.mat-primary {
      .mat-fab-toolbar-background {
        background: #3a263a;
        color: mat.get-color-from-palette($primary, default-contrast);
      }
    }
```
### Remove watermark
In ~/thingsboard/ui-ngx/src/app/modules/home/components/dashboard-page/dashboard-page.component.html
find "Power" and remove it.
### Change help website
In ~/thingsboard/ui-ngx/src/app/shared/components/logo.component.ts
replace thingsboard with ninox.ai
## Modify after Package
### Change primary toolbar color
In {YOUR_WORKDIR}/web/public/style.css 
find the "mat-toolbar.mat-primary 1/6"
### Change dashboard toolbar color
In {YOUR_WORKDIR}/web/public/style.css 
find the ".tb-default mat-fab-toolbar .mat-fab-toolbar-background  1/5"
### Change home button color
In {YOUR_WORKDIR}/web/public/style.css 
find the "mat-raised-button.mat-primary 3/10"
mat-raised-button.mat-primary
### Change web head name
In {YOUR_WORKDIR}/web/public/index.html
find body->title
### Remove Watermark
In {YOUR_WORKDIR}/web/public/6610.js
find "ut.thingsboardVersion" delete those words
### Change web destination URL
In {YOUR_WORKDIR}/web/public/main.js
find "thingsboard.io" replace all of them with ninox.ai
### Change logo picture
In {YOUR_WORKDIR}/web/public/assets/logo_title_white.svg
change /usr/share/tb-web-ui/web/public/assets/logo_title_white.svg
### Change web icon
In {YOUR_WORKDIR}/web/public/thingsboard.ico
change /usr/share/tb-web-ui/web/public/thingsboard.ico
