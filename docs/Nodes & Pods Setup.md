### Note:  Before jumping into the setup instruction, please make sure
           1. You have followed all the steps list in the 'K8s & KubeEdge Setup' and seen the expected output.
           2. There are three nodes running on the proper virtual machines.

[K8s & KubeEdge Setup](https://github.com/pdgetrf/fw_intern_2022/blob/main/docs/K8s%20%26%20KubeEdge%20Setup.md)\
[AWS Installation](https://linuxhint.com/install_aws_cli_ubuntu/)

# Setting up the nodes & pods environment
## 1. Edge Configuration
On the Edge machine, we need to first install the aws under the **EdgeMaterials directory** use the command
```bash
sudo apt-get update
```
and then run
```bash
sudo apt-get install awscli
```

Next, config the AWS CLI by running
```bash
root@ip-172-31-9-23:~/fw_intern_2022/EdgeMaterials# aws configure
AWS Access Key ID [None]: --[your access key id]--
AWS Secret Access Key [None]: --[your secret access key]--
Default region name [None]: --[your default region, for example us-west-2]--
Default output format [None]: text
```
Now, if you use '_ls -la_', you should see the folder named '.aws' exist along with the files.\
\
Next, we need to build the docker image so that the deployment from Cloud can be successfully assigned on Edge.\
\
To build the docker image, run
```bash
cd /root/fw_intern_2022/EdgeMaterials
docker build -t edgeimage .
```
and run the following command to check if your image is ready
```bash
docker images
```
If everything goes well, you should see something like the following\
![Edge Docker image picture](https://user-images.githubusercontent.com/108478119/186229404-e86620ca-1b39-4bd0-a404-c5864ebe079d.png)\

Open the file 'UploadData.go' and change line 15 to the following format with your own information
```bash
up_arg3 := "s3://[$your AWS s3 bucket name]/[$your desired file name]"
```
An example would be
```bash
up_arg3 := "s3://demo-bucket/demo_data.txt"
```

Now, copy the link of up_arg3, and you are ready to move to configure the Cloud Worker!


## 2. Cloud Worker Configuration
On the Cloud Worker machine, open the file 'FetchData.go', and replace the link assign to the 'arg2' to the link you just copied at line 14. Save the file and exit.\
\
To match the previous example, it should looks like
```bash
arg2 := "s3://demo-bucket/demo_data.txt"
```
Similiar with what we did on the Edge, the Cloud Worker also needs to have aws installed, and a docker image to handle the deployment request from the Cloud. **Please make sure you have .aws under the CloudMaterial directory before building the docker image.**\
\
To build the docker image, run
```bash
docker build -t cloudimage .
```
Use the same command above to check if the image is ready. If everything goes well, you should see something like the following\
![Cloud Worker Docker image picture](https://user-images.githubusercontent.com/108478119/186229036-b516f4dd-ee73-4d33-918d-b5def0d668fd.png)

Since kuberbets used containerd as the runtime container environment, you need to switch the docker image to containerd version.\
To do that, run '_docker save -o xxx.tar $imageList_' to save the docker image into .tar first. An example would be
```bash
docker save -o test.tar cloudimage
```
Then, to use the .tar to create containerd image, run '_ctr -n=k8s.io image import  xxx.tar_'. An example would be
```bash
ctr -n=k8s.io image import test.tar
```
Once you have the containerd image, it the time to move forward to the Cloud side!


## 3. Cloud Configuration
On the Cloud machine, for assigning the pod to the node precisely, you may want to label your nodes so that they are easy to be distinguished.\
To check the node with existing labels, run
```bash
kubectl get nodes --show-labels
```
To label the nodes, run the command '_kubectl label nodes $your-node-name workerType=$your-node-worker-type_'. In this project's setup, 
they will look like
```bash
kubectl label nodes ip-172-31-10-73 workerType=cloudworker
```
and
```bash
kubectl label nodes ip-172-31-8-170 workerType=edgeworker
```
Then, you should see something similiar with the following
![Three nodes with labels picture](https://user-images.githubusercontent.com/108478119/186236786-04301d3e-544f-43cf-a1a0-3c019d97ff55.png)
**Note: You only change the node name in the command to your own node name, don't modify the workerType.**

If everything works, congrats! Now the preparation work is done, and you will be able to properly assign pods to your nodes!


## 4. Connecting All Components
Please make sure you are in the Cloud machine since it is the master.\
\
To assign the deployment on the Cloud worker, run
```bash
kubectl apply -f CloudWorker_Deployment.yaml
```
To assign the deployment on the Edge worker, run
```bash
kubectl apply -f EdgeWorker_Deployment.yaml
```
Now, when you run the command '_kubectl get pods -o wide_', you should have 2 pods running on the correct node, looks like:
![two pods running picture](https://user-images.githubusercontent.com/108478119/186237938-fb227013-e9b0-44e0-b44b-28c040396c9c.png)
If you see this status, congratulations! All the preparation and configuration are done! You are ready to move to the [next step](https://github.com/pdgetrf/fw_intern_2022/blob/main/docs/Program%20Execution.md)!










