<!-- UNCOMMENT FOR RENDER
<style> html, body { font-size: 16px; line-height: 22px; } h1, h2, h3, a { color: #3BC1CAFF; } h1, h2, h3 { margin: 0; padding: 0; font-style: italic; } h1 { font-size: 2rem; line-height: 2.4rem; margin-bottom: 0.8rem; } h2 { font-size: 1.4rem; line-height: 1.8rem; margin-bottom: 0.4rem; } h3 { font-size: 1.2rem; line-height: 1.4rem; margin-bottom: 0.2rem; } ul, ol { margin-left: -0.6rem; } ul > li > ol, ol > li > ol { margin-left: -2.2rem; } ul > li > ul, ol > li > ul { margin-left: -2.6rem; } ul { list-style: none; position: relative; } ul > li:before { content: "– "; position: absolute; left: 2.6rem; width: 1rem; height: 1rem; } :not(pre):not(.hljs) > code { color: #565656; cursor: pointer; } pre, blockquote p, .hljs { margin: 0; padding: 0; border-radius: 0; } blockquote, pre:not(.hljs), pre.hljs code > div { padding: 0.35rem 0.9rem 0.4rem 0.9rem; font-size: 0.8rem; line-height: 1.4rem; cursor: pointer; } pre, .hljs, blockquote { margin-bottom: 1.2rem; } blockquote { border-color: #3BC1CAFF; } @media print { html, body { font-size: 11px; line-height: 15px; } .pagebreak { page-break-before: always; } } </style>
-->


# Kubernetes WSL Kind Podman desktop

<code id="timestamp">2024-08-22T15:41:35.972+02:00</code>

These instructions will enable you to run `podman`, `kubectl` and `kind` on Windows 11 using [Podman Desktop](https://podman-desktop.io/downloads/windows).

## Set Up Kind

1.  Install [Podman Desktop](https://podman-desktop.io/downloads/windows)
2.  During installation install `compose`, `kubectl`, `podman` and set up `podman-machine-default`
3.  Install `kubens` and `kubectx` via `choco` (first install `choco` if not installed)
    ```bash
    choco install kubens kubectx
    ```
4.  Create a yaml-file for the kind cluster `cluster-01.yml` with following content
    ```yaml
    kind: Cluster
    apiVersion: kind.x-k8s.io/v1alpha4
    nodes:
    - role: control-plane
      kubeadmConfigPatches:
      - |
        kind: InitConfiguration
        nodeRegistration:
          kubeletExtraArgs:
            node-labels: "ingress-ready=true"
      extraPortMappings:
      - containerPort: 80
        hostPort: 80
        protocol: TCP
    ```
5.  Set up the kind cluster in the console
    ```bash
    kind create cluster --config cluster-01.yml --name cluster-01
    # [output]
    # enabling experimental podman provider
    # Creating cluster "cluster-01" ...
    #  ✓ Ensuring node image (kindest/node:v1.30.0) 🖼
    #  ✓ Preparing nodes 📦
    #  ✓ Writing configuration 📜
    #  ✓ Starting control-plane 🕹️
    #  ✓ Installing CNI 🔌
    #  ✓ Installing StorageClass 💾
    # Set kubectl context to "kind-cluster-01"
    # You can now use your cluster with:

    # kubectl cluster-info --context kind-cluster-01

    # Thanks for using kind! 😊
    ```
6.  Check if the cluster was created
    ```bash
    kubectl get node --show-labels
    # [output]
    # NAME                       STATUS   ROLES           AGE   VERSION   LABELS
    # cluster-01-control-plane   Ready    control-plane   32m   v1.30.0   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,ingress-ready=true,kubernetes.io/arch=amd64,kubernetes.io/hostname=cluster-01-control-plane,kubernetes.io/os=linux,node-role.kubernetes.io/control-plane=
    ```

<p class="pagebreak"></p>

7. Create a `namespace.yml` with following content
    ```yaml
    apiVersion: v1
    kind: Namespace
    metadata:
      name: echo-space
    ```
8.  Set up the namespace
    ```bash
    kubectl apply -f .\namespace.yml
    ```
9.  Activate namespace defined in `namespace.yml`
    ```bash
    kubens echo-space
    # [output] > ✔ Active namespace is "echo-space"
    ```
10. Create new deployment
    ```bash
    kubectl create deployment echo-app --image=k8s.gcr.io/echoserver:1.4
    ```
11. Create a load balancer and expose the service
    ```bash
    kubectl expose deployment echo-app --type=LoadBalancer --port=80 --target-port=8080
    ```
12. Confirm service and service and pod are running
    ```bash
    kubectl get pod -o wide
    # [output]
    # NAME                        READY   STATUS    RESTARTS   AGE     IP           NODE                       NOMINATED NODE   READINESS GATES
    # echo-app-677767f685-s2lvz   1/1     Running   0          3m58s   10.244.0.5   cluster-01-control-plane   <none>           <none>
    ```
13. Deploy the nginx controller directly from the repo
    ```bash
    kubectl apply --filename=https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/static/provider/kind/deploy.yaml
    # [output]
    # namespace/ingress-nginx created
    # serviceaccount/ingress-nginx created
    # serviceaccount/ingress-nginx-admission created
    # role.rbac.authorization.k8s.io/ingress-nginx created
    # role.rbac.authorization.k8s.io/ingress-nginx-admission created
    # clusterrole.rbac.authorization.k8s.io/ingress-nginx created
    # clusterrole.rbac.authorization.k8s.io/ingress-nginx-admission created
    # rolebinding.rbac.authorization.k8s.io/ingress-nginx created
    # rolebinding.rbac.authorization.k8s.io/ingress-nginx-admission created
    # clusterrolebinding.rbac.authorization.k8s.io/ingress-nginx created
    # clusterrolebinding.rbac.authorization.k8s.io/ingress-nginx-admission created
    # configmap/ingress-nginx-controller created
    # service/ingress-nginx-controller created
    # service/ingress-nginx-controller-admission created
    # deployment.apps/ingress-nginx-controller created
    # job.batch/ingress-nginx-admission-create created
    # job.batch/ingress-nginx-admission-patch created
    # ingressclass.networking.k8s.io/nginx created
    # validatingwebhookconfiguration.admissionregistration.k8s.io/ingress-nginx-admission created

    kubectl wait --namespace=ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=180s
    # [output] > pod/ingress-nginx-controller-8fb8cdb7c-jqgv7 condition met
    ```

<p class="pagebreak"></p>

14. Create file `ingress.yml` for ingress controller
    ```yml
    apiVersion: networking.k8s.io/v1
    kind: Ingress
    metadata:
      name: echo-ingress
      namespace: echo-space
      annotations:
        nginx.ingress.kubernetes.io/rewrite-target: /
    spec:
      rules:
      - http:
          paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: echo-app
                port:
                  number: 80
    ```
15. Apply the yml file for the ingress controller
    ```bash
    kubectl apply -f .\ingress.yml
    # [output] > ingress.networking.k8s.io/echo-ingress created
    ```
16. Check that the ingress controller is correctly set up
    ```bash
    kubectl describe ingress echo-ingress
    # [output]
    # Name:             echo-ingress
    # Labels:           <none>
    # Namespace:        echo-space
    # Address:          localhost
    # Ingress Class:    <none>
    # Default backend:  <default>
    # Rules:
    #   Host        Path  Backends
    #   ----        ----  --------
    #   *
    #               /   echo-app:80 (10.244.0.5:8080)
    # Annotations:  nginx.ingress.kubernetes.io/rewrite-target: /
    # Events:
    #   Type    Reason  Age                    From                      Message
    #   ----    ------  ----                   ----                      -------
    #   Normal  Sync    2m56s (x2 over 3m41s)  nginx-ingress-controller  Scheduled for sync
    ```

<p class="pagebreak"></p>

17. Send curl request to localhost to check if the service is running
    ```bash
    curl localhost
    # [output]
    # StatusCode        : 200
    # StatusDescription : OK
    # Content           : CLIENT VALUES:
    #                     client_address=10.244.0.8
    #                     command=GET
    #                     real path=/
    #                     query=nil
    #                     request_version=1.1
    #                     request_uri=http://localhost:8080/

    #                     SERVER VALUES:
    #                     server_version=nginx: 1.10.0 - lua: 10001

    #                     HEADERS REC...
    # RawContent        : HTTP/1.1 200 OK
    #                     Transfer-Encoding: chunked
    #                     Connection: keep-alive
    #                     Content-Type: text/plain
    #                     Date: Fri, 23 Aug 2024 08:25:14 GMT

    #                     CLIENT VALUES:
    #                     client_address=10.244.0.8
    #                     command=GET
    #                     real path=/
    #                     q...
    # Forms             : {}
    # Headers           : {[Transfer-Encoding, chunked], [Connection, keep-alive], [Content-Type, text/plain], [Date, Fri, 23 Aug 2024 08:25:14 GMT]}
    # Images            : {}
    # InputFields       : {}
    # Links             : {}
    # ParsedHtml        : mshtml.HTMLDocumentClass
    # RawContentLength  : 541
    ```
18. Reset everything to working conditions (podman, kubectl etc. will keep being installed)
    ```bash
    kubectl delete service echo-app
    # [output] > service "echo-app" deleted
    kubectl delete deployment echo-app
    # [output] > deployment.apps "echo-app" deleted
    kubens default
    # [output] > ✔ Active namespace is "default"
    kubectl delete namespace echo-space
    # [output] > namespace "echo-space" deleted
    kubectl delete --filename=https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/static/provider/kind/deploy.yaml
    # [output] > delete statements for all the services in the ingress file
    ```

<p class="pagebreak"></p>

## Installing Krew Packet Manager and Stern

1.  Download Krew (`krew.exe`) from [here](https://github.com/kubernetes-sigs/krew/releases)
2.  Open an elevated administrator `cmd`, go to download folder and execute
    ```bash
    .\krew install krew
    ```
3.  Add `krew` binary folder to your `PATH` variable (folder C:/Users/[username]/.krew/bin)
4.  Restart your `shell`
5.  Now install `stern` from another elevated administrator `cmd`
    ```bash
    kubectl krew install stern
    # [output]
    # Updated the local copy of plugin index.
    # Installing plugin: stern
    # Installed plugin: stern
    # \
    #  | Use this plugin:
    #  |      kubectl stern
    #  | Documentation:
    #  |      https://github.com/stern/stern
    # /
    # WARNING: You installed plugin "stern" from the krew-index plugin repository.
    #    These plugins are not audited for security by the Krew maintainers.
    #    Run them at your own risk.
    ```
6.  Now you can run `stern` with following commands
    ```bash
    kubectl-stern . --all-namespaces
    # [output] > all logs of all namespaces … please be aware that this can be a lot of logs
    kubectl-stern . -n kube-system --tail 0
    # [output] > log output of namespace "kube-system"
    ```
7.  Read up on more use cases for stern [here](https://github.com/stern/stern?tab=readme-ov-file#examples)




<!-- UNCOMMENT FOR RENDER
<script> const getISODateTZString = () => { const pad = (num, len = 2) => { const numString = `${num}`.split('').reverse().join(''); return [...Array(len).keys()].map(i => numString.charAt(i) ? numString.charAt(i) : '0').reverse().join(''); }; const getTZOffset = () => { const tzOffsetMin = new Date().getTimezoneOffset(); const delimiter = (tzOffsetMin < 0) ? '+' : (tzOffsetMin > 0) ? '-' : 'Z'; const tzAbs = Math.abs(tzOffsetMin); const hrOffset = pad(parseInt(tzAbs / 60)); const minOffset = pad(tzAbs % 60); return `${delimiter}${hrOffset}:${minOffset}`; }; const tzOffset = getTZOffset(); const dt = new Date(); const dateString = `${ dt.getFullYear()}-${ pad(dt.getMonth() + 1)}-${ pad(dt.getDate())}T${ pad(dt.getHours())}:${ pad(dt.getMinutes())}:${ pad(dt.getSeconds())}.${ pad(dt.getMilliseconds(), 3)}`; return `${dateString + tzOffset}`; }; timestamp.innerText = getISODateTZString(); </script>
-->