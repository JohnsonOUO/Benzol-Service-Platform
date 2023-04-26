import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ThingsboardPublish2 implements Runnable {
	private String message;
	public static float x = 20;
    public static float y = 70;
	public float random_humi;
    public float random_temp;
    public static String temp			= "\"Temp1\":"+x;
    public static String Humi			= "\"Speed\":"+y;
    public static String both			= "{"+temp+","+Humi+"}";
    
    public static void main(String[] args)
    {   	
    	// Thingsboard topic Topic name 
        String topic        = "v1/devices/me/telemetry";
        //data to be send
        ThingsboardPublish2 thread1 = new ThingsboardPublish2("message1");
        //float x = 20;
        Thread thr = new Thread( thread1 ); 
        thr.start();
        //float y = 70;
        //float random_humi;
        //float random_temp;
        //String temp			= "\"Temp\":"+x;
        //String Humi			= "\"Humi\":"+y;
        //String both			= "{"+temp+","+Humi+"}";
        //String content      = "{\"Temp\":20,\"Humi\":70}";
        int qos             = 0;
        int stop=0 ;
        /*demo.thingsboard.io is the where thingsboard listen data on port 1883*/ 
        String broker       = "tcp://10.1.2.168:1883";
        String clientId     = "TB1";
        MemoryPersistence persistence = new MemoryPersistence();
        while (stop==0){
	        try {
	            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
	            MqttConnectOptions connOpts = new MqttConnectOptions();
	            connOpts.setCleanSession(true);
	            connOpts.setKeepAliveInterval(60);
	            System.out.println("please get the token from thingsboard device");
	            //connOpts.setUserName("8vN2haOBZQAY6Vq5jlNA");
	            connOpts.setUserName("SqTgCw2aSkFlOH94ouft");
	            System.out.println("Connecting to broker: "+broker);
	            sampleClient.connect(connOpts);
	            System.out.println("Connected to thingsboard broker");
	            //System.out.println("Publishing message:"+content);
	            System.out.println("Publishing message:"+both);
	            //MqttMessage message = new MqttMessage(content.getBytes());
	            MqttMessage message = new MqttMessage(both.getBytes());
	            message.setQos(qos);           
	            sampleClient.publish(topic, message);
	            System.out.println("Message published ");
	            System.out.println("Please check data in telemetry of your device on thingsboard");
	            //
	            //Timer timer = new Timer();
	            //timer.schedule(new DoSomethingTimerTask("PeriodDemo"),2000L,1000L);
	            //if else
	            /*
	            if (x>35) {
	            	random_temp = (float)(Math.random()-0.8);
	            }
	            else if (x<35 && x > 15) {
	            	random_temp = (float)(Math.random()-0.5);
	            }
	            else {
	            	random_temp = (float)(Math.random()-0.2);
	            }
	            if (y>82) {
	            	random_humi = (float)(Math.random()-0.8);
	            }
	            else if (x<82 && x > 50) {
	            	random_humi = (float)(Math.random()-0.5);
	            }
	            else {
	            	random_humi = (float)(Math.random()-0.2);
	            }
	            x += random_temp;
	            y += random_humi;
	            temp			= "\"Temp\":"+x;
	            Humi			= "\"Humi\":"+y;
	            both			= "{"+temp+","+Humi+"}";
	            */
	            } 
	        
	        catch(MqttException me) 
	        {
	            System.out.println("reason "+me.getReasonCode());
	            System.out.println("msg "+me.getMessage());
	            System.out.println("loc "+me.getLocalizedMessage());
	            System.out.println("cause "+me.getCause());
	            System.out.println("excep "+me);
	            stop = 1;
	            me.printStackTrace();
	        }
        }
    }
    public ThingsboardPublish2(String message){
        this.message = message;
    }
    @Override
    public void run() {
    	while(true) {
	        try {
	            Thread.sleep(10000L);
	            if (x>35) {
	            	random_temp = (float)(Math.random()-0.8);
	            }
	            else if (x<35 && x > 15) {
	            	random_temp = (float)(Math.random()-0.5);
	            }
	            else {
	            	random_temp = (float)(Math.random()-0.2);
	            }
	            if (y>82) {
	            	random_humi = (float)(Math.random()-0.8);
	            }
	            else if (x<82 && x > 50) {
	            	random_humi = (float)(Math.random()-0.5);
	            }
	            else {
	            	random_humi = (float)(Math.random()-0.2);
	            }
	            ThingsboardPublish2.x += random_temp;
	            ThingsboardPublish2.y += random_humi;
	            ThingsboardPublish2.temp			= "\"Temp1\":"+x;
	            ThingsboardPublish2.Humi		= "\"speed\":"+y;
	            ThingsboardPublish2.both			= "{"+ThingsboardPublish2.temp+","+ThingsboardPublish2.Humi+"}"; 
	            //System.out.println("Thread message is:" + this.message);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
    	}
    } 
    
}  