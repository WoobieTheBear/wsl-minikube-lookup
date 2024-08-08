# to set up everything at once

kubectl delete -f ./black-express.yaml
kubectl delete -f ./black-db.yaml
kubectl delete -f ./black-configmap.yaml
kubectl delete -f ./black-secret.yaml
kubectl delete -f ./black-namespace.yaml
