#!/bin/bash

# use this script as a document

kubectl get nodes

kubectl run edge-node-nginx-test --image=nginx	# this is to run on the edge node

kubectl get pods -o wide
