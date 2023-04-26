import paho.mqtt.client as mqtt
import json
import random
import time

broker_addr = "192.168.1.2"
client = mqtt.Client()
client.username_pw_set("55688")
client.connect(broker_addr,1883)
wind_out = 5
wind_in = 5
rpm = 1000
vel = 50
lat = 25.05220
long = 121.52270
driver = "safety"
s_time = 5
dir = 1
while True:
    t0 = random.randint(0,9)
    t1 = random.randint(0,200)
    t2 = random.randint(0,11)
    t3 = random.randint(0,10)
    t1 = t1 - 100
    if(t3==8):
        s_time = 10
        driver = "using cellphone"
    elif(t3==9):
        s_time = 10
        driver = "sleep"
    else:
        s_time = 5
        driver = "safety"
    if(long>121.56700):
        dir = -1
    elif(long<121.52270):
        dir = 1
    long = long + (dir*0.002)
    lat = lat - (dir*0.00002)
    payload = {"serialNumber":"AutoCar","sensorType":"Car","sensorModel":"Yolo","Wind_Out":wind_out+((t2-5)/10),"Wind_In":wind_in+((t0-5)/10),"Lower_bound":"2","Upper_bound":"8","rmp":(rpm+t1)/100,"vel":vel+(t1/20),"driver":driver,"latitude":lat,"longitude":long}
    print(json.dumps(payload))
    #client.publish("/sensor/date",json.dumps(payload),qos=1)
    client.publish("v1/devices/me/telemetry",json.dumps(payload),qos=1)
    time.sleep(s_time)
