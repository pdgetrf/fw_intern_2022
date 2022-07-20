#!/bin/bash

# use this script as a document
CLOUD_MACHINE_PUBLIC_IP=35.90.205.88

# install kubeedge binaries
# only need to do this install once
wget  https://github.com/kubeedge/kubeedge/releases/download/v1.9.2/keadm-v1.9.2-linux-amd64.tar.gz
tar zxvf  keadm-v1.9.2-linux-amd64.tar.gz
cd keadm-v1.9.2-linux-amd64/

# start the cloud-core
keadm/keadm init --kube-config=$KUBECONFIG --advertise-address $CLOUD_MACHINE_PUBLIC_IP 

# get token, and use this token on the edge-core machine
keadm/keadm gettoken --kube-config=$KUBECONFIG

# to stop cloud-core, run
# keadm reset
