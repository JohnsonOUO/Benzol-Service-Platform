name="tb-web-ui-5496cc5c-tkss7"
path="$name:/usr/share/tb-web-ui"
style=$(kubectl exec pod/$name -- find web/public/ -iname 'styles.*.css')
var=$(kubectl exec pod/$name -- find web/public/ -iname '6610.*.js')
main=$(kubectl exec pod/$name -- find web/public/ -iname 'main.*.js')
index=web/public/index.html
ico=web/public/thingsboard.ico
logo=web/public/assets/logo_title_white.svg
echo $path/$style
echo $path/$var
echo $path/$main
echo $path/$index
echo $path/$ico
echo $path/$logo

kubectl cp thingsboard.ico $path/$ico
kubectl cp modify_6610.js $path/$var
kubectl cp modify_index.html $path/$index
kubectl cp  modify_main.js $path/$main
kubectl cp  modify_style.css $path/$style
kubectl cp alpha.svg $path/$logo