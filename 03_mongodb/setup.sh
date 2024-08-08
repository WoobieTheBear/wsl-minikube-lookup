# to set up everything at once

kubectl apply -f ./black-namespace.yaml
kubectl apply -f ./black-secret.yaml
kubectl apply -f ./black-configmap.yaml
kubectl apply -f ./black-db.yaml
kubectl apply -f ./black-express.yaml
