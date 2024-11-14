# ArtemisMQ Cluster

## Requirements

- `podman` installed
- `kind` installed
- `kubectl` installed
- lots and lots fo nerves


## Setup

1. Make sure your console is in project folder
2. Run following commands to set up and configure the cluster:
```bash
kind create cluster --config amq-cluster.yaml
# will confirm in output if you are even able to set up the cluster control-plane
kubectl apply -f ./amq-network-policy.yaml
# will create the network policy to allow the services to talk to each other
kubectl apply -f ./amq-ingress.yaml
# will create an ingress configuration for the cluster
```
3. To create 2 stateful sets as brokers and use the image `apache/activemq-artemis:latest-alpine` you run:
```bash
kubectl apply -f ./amq-broker-config.yaml
# will create a broker config to configure two brokers and a queue called 'queueOne'
kubectl apply -f ./amq-broker-service.yaml
# will create a broker config to configure two brokers and a queue called 'queueOne'
```
4. Then you create the containers using:
```bash
podman build . -f .\Containerfile.Plainconsumer -t amq-plainconsumer:1.0.1
# this will create the consumer in the plainconsumer folder [check if the version number in the amq-plainconsumer.yaml matches!]
podman build . -f .\Containerfile.Plainproducer -t amq-plainproducer:1.0.6
# this will create the producer in the plainproducer folder  [check if the version number in the amq-plainproducer.yaml matches!]
```
5. Then you have to transfer the two local images to the `kind` cluster using podman desktop
6. Then you can create the two pods using
```bash
kubectl apply -f .\amq-plainconsumer.yaml
# this will create the actual cluster pod for the consumer
kubectl apply -f .\amq-plainproducer.yaml
# this will create the actual cluster pod for the producer
kubectl get all
# [output]
# NAME                                                READY   STATUS      RESTARTS      AGE
# pod/amq-broker-0                                    1/1     Running     0             19h
# pod/amq-broker-1                                    1/1     Running     0             19h
# pod/amq-plainconsumer-deployment-869bb59d8c-hnpzg   0/1     Completed   2 (58s ago)   60s
# pod/amq-plainproducer-deployment-5cbfb87654-fkpbg   0/1     Completed   1 (2s ago)    3s

# NAME                                TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)     AGE
# service/amq-broker-service          ClusterIP   10.96.61.167   <none>        61616/TCP   19h
# service/amq-plainconsumer-service   ClusterIP   10.96.46.138   <none>        61616/TCP   60s
# service/amq-plainproducer-service   ClusterIP   10.96.119.31   <none>        61616/TCP   3s
# service/kubernetes                  ClusterIP   10.96.0.1      <none>        443/TCP     21h

# NAME                                           READY   UP-TO-DATE   AVAILABLE   AGE
# deployment.apps/amq-plainconsumer-deployment   0/1     1            0           60s
# deployment.apps/amq-plainproducer-deployment   0/1     1            0           3s

# NAME                                                      DESIRED   CURRENT   READY   AGE
# replicaset.apps/amq-plainconsumer-deployment-869bb59d8c   1         1         0       60s
# replicaset.apps/amq-plainproducer-deployment-5cbfb87654   1         1         0       3s

# NAME                          READY   AGE
# statefulset.apps/amq-broker   2/2     19h
```
7. To access the logs you can use
```bash
kubectl logs pod/amq-plainconsumer-deployment-869bb59d8c-hnpzg
# will log the messages recieved
kubectl logs pod/amq-plainproducer-deployment-5cbfb87654-fkpbg
# will log the messages sent
```

## Teardown

1. Remove the images from the control-plane
```bash
podman exec -ti amq-cluster-control-plane bash
# this will open a bash session in the control-plane container of podman
crictl images
# this will log all images and show their names
crictl rmi amq-plainconsumer:1.0.1
# this will remove the specified image [check version and only delete image that exists on control-plane]
crictl rmi amq-plainproducer:1.0.6
# this will remove the specified image [check version and only delete image that exists on control-plane]
```
2. Shut down the producer and consumer pods
```bash
kubectl delete -f .\amq-plainconsumer.yaml
# this will stop the cluster pod for the consumer
kubectl delete -f .\amq-plainproducer.yaml
# this will stop the cluster pod for the producer
```
> NOTE: if this does not work use:<br/>
```bash
kubectl delete pod $YOUR_PODNAME --grace-period=0 --force -n $YOUR_NAMESPACE
```
3. Shut down the queue
```bash
kubectl apply -f ./amq-broker-config.yaml
# will create a broker config to configure two brokers and a queue called 'queueOne'
kubectl apply -f ./amq-broker-service.yaml
# will create a broker config to configure two brokers and a queue called 'queueOne'
```
4. Check if everything is down
```bash
kubectl get all
# [output]
# NAME                 TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
# service/kubernetes   ClusterIP   10.96.0.1    <none>        443/TCP   22h
```
