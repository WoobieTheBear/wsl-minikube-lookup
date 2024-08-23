# Basic Web App Node

This is a web app node as basic as possible. The Container uses linux alpine to install nodejs and start up express with a single route `/theimage` that returns `{ message: 'you found the image' }`.

## Setup

To setup the container and load the image to minikube run following commands:

1. `podman build . -t=the-image:1.0.0`
2. `podman save -o the-image.tar the-image:1.0.0`
3. `minikube image load the-image.tar`
4. Execute the steps to setup ingress and the web-service for the image (explained in detail in `README.md`)
    ```bash
    sudo chmod 777 -R /dev/kvm
    sudo mount --make-rshared /
    podman machine init --cpus 6 --disk-size 256 --memory 8192 --rootful
    podman machine start
    minikube start --driver=podman --container-runtime=cri-o --network=host
    minikube addons enable ingress
    minikube addons enable ingress-dns
    kubectl apply -f image-web.yaml
    kubectl apply -f image-ingress.yaml
    ip -4 addr show eth0 | grep -oP '(?<=inet\s)\d+(\.\d+){3}'
    # [output] > 172.28.30.69
    route add 192.168.49.0 mask 255.255.255.0 172.28.30.69 metric 1
    ```
