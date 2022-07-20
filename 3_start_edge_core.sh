#!/bin/bash

# use this script as a document
CLOUD_MACHINE_PUBLIC_IP=35.90.205.88

# install kubeedge binaries
# only need to do this install once
wget  https://github.com/kubeedge/kubeedge/releases/download/v1.9.2/keadm-v1.9.2-linux-amd64.tar.gz
tar zxvf  keadm-v1.9.2-linux-amd64.tar.gz
cd keadm-v1.9.2-linux-amd64/

# get token from the cloud-core machine
# save the token in a variable so we don't go berserk 
token=[the token from the cloud core machine]

# start the edge-core by joining to the cloud-core
keadm/keadm join --cloudcore-ipport=$CLOUD_MACHINE_PUBLIC_IP:10000 --token=$token


# to stop edge-core, run
# keadm reset
