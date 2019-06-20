# postboard.online app

This application is based on Silhouette seed Template: 

https://github.com/mohiva/play-silhouette-seed
 
 
Install 
	Neo4j for database access 
	MQTT for real time messaging (preferably mosquitto)


 
export _JAVA_OPTIONS="-Xms128m -Xmx256m"
sbt clean dist 



sudo nohup bash postboard_server -Dconfig.file=/home/ubuntu/prod/1/postboard_server-1.0.0/conf/application.prod.conf  -Dhttp.port=80

 




export _JAVA_OPTIONS="-Xms256m -Xmx256m"




https://bitbucket.org/ajaygeorge7/postboard_server



http {
	server {
	    listen 80;
	    server_name postboard.online;
	    location / {
	        proxy_pass         http://127.0.0.1:9000;
	    }
	}
}

ws {
	server {
	    listen 80;
	    server_name postboard.online;
	    location / {
	        proxy_pass         ws://127.0.0.1:9000;
	    }
	}
}
