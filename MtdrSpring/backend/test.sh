docker rm agileOrganizerContainer
docker rmi mx-queretaro-1.ocir.io/axzmghqes1gn/reacttodo/x7djf/todolistapp-springboot:0.1
mvn verify
docker build -f Dockerfile --platform linux/amd64 -t mx-queretaro-1.ocir.io/axzmghqes1gn/reacttodo/x7djf/todolistapp-springboot:0.1 .
docker run -it --name agileOrganizerContainer -p 8080:8080 mx-queretaro-1.ocir.io/axzmghqes1gn/reacttodo/x7djf/todolistapp-springboot:0.1