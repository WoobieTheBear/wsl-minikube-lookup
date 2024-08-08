# Basic Web App Node

This is a web app node as basic as possible. The Container uses linux alpine to install nodejs and start up express with a single route `/theimage` that returns `{ message: 'you found the image' }`.

## Setup

To setup the container run following commands:

1. `podman build . -t=theimage`
2. `podman run -d --name=container-name-of-the-image -p 1337:1337 theimage`
3. Open a browser on `localhost:1227`