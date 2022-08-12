#!/bin/bash

# use this script as a document
CLOUD_MACHINE_PUBLIC_IP=35.90.205.88

# don't change 10.244.0.0/16
kubeadm init --control-plane-endpoint=$CLOUD_MACHINE_PUBLIC_IP --pod-network-cidr=10.244.0.0/16

export KUBECONFIG=/etc/kubernetes/admin.conf 	# also, add the above export to .bashrc
source ~/.bashrc 	

# install a cni network just so the log would shut-up
# this step may not be needed
kubectl apply -f https://raw.githubusercontent.com/flannel-io/flannel/master/Documentation/kube-flannel.yml

# to stop k8s, run
# kubeadm reset
