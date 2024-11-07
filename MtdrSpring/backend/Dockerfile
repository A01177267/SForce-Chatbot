FROM openjdk:22
#COPY pom.xml /tmp/
#COPY src /tmp/src/
WORKDIR /tmp/
EXPOSE 8080
COPY target/MyTodoList-0.0.1-SNAPSHOT.jar MyTodoList.jar
RUN mkdir -p /mtdrworkshop/creds
COPY wallet/ /mtdrworkshop/creds
ENV db_user="ADMIN"
#ENV ui_username="admin"
#ENV ui_password="none0001"
ENV dbpassword="None10001000"
ENV db_url="jdbc:oracle:thin:@reacttodorrs2o_tp?TNS_ADMIN=/mtdrworkshop/creds"
ENV driver_class_name="oracle.jdbc.OracleDriver"
ENV _JAVA_OPTIONS=-Dlogging.level.org.springframework=INFO
ENTRYPOINT ["java","-jar","MyTodoList.jar"]