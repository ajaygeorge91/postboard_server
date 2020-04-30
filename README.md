# postboard.online app

 
Install 
	Neo4j for database access 
	MQTT for real time messaging (preferably mosquitto)


sbt clean dist 



sudo nohup bash postboard_server -Dconfig.file=/home/ubuntu/prod/1/postboard_server-1.0.0/conf/application.prod.conf  -Dhttp.port=80

 







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
