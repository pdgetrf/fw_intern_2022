### Note:  Before run the program, please make sure
           1. You have followed all the steps list in the 'K8s & KubeEdge Setup' and 'Nodes & Pods Setup'.
           2. You have Andriod Studio installed on your computer, and the smart watch emulator has been configured.
           
# Program Execution
## 1. Starting the server on the edgeside
To start the server, go to the Edge machine and run the following command to get into the container
```bash
docker exec -it [$Container ID] sh
```
Simply replace the '[$Container ID]' with your own container infomation, it should looks like
![exec Edge container](https://user-images.githubusercontent.com/108478119/186481943-25e9d67f-ddf1-4a54-a4ce-7f2f68d6dcd7.png)\
\
As you can notice, now we are in the container environment. To start the server, run
```bash
python3 healthserver.py &
```
**Note: Please remember to kill the process after use if you run it in the background.**

## 2. Open the emulator and check your AWS s3 storage
In Android Studio, go find the app named 'HeartRatetoWeb', put your public IP address of your Edge machine and run it.
![Emulator Screenshot](https://user-images.githubusercontent.com/108478119/186484098-09836e26-0679-4728-834e-57f93042ffc9.png)\
If you go the the upper right corner, click on the button made by three dots which represent 'extended controls', you will be able to change the 
heart rate(bpm). 
<img width="950" height="140" alt="Screen Shot 2022-08-24 at 11 44 48 AM" src="https://user-images.githubusercontent.com/108478119/186498409-a9be3a27-0769-41e0-ad0f-c8e70908560b.png">

Now, if you open the edge_data.txt file on you Edge machine, you should see all the changes has been recorded here
![Edge data recieve from Android Studio](https://user-images.githubusercontent.com/108478119/186497471-312ebb69-f600-40eb-8c98-5ef8aa95aa46.png)

Open the AWS s3 link you updated in [Nodes & Pods Setup](https://github.com/pdgetrf/fw_intern_2022/blob/main/docs/Nodes%20%26%20Pods%20Setup.md), you
should see the edge_data.txt is already there.

If the data in the file matches what you have on the Edge machine, you are ready to move forward!

## 3. Recieve the information in Cloud
On your Cloud machine, run the command
```bash
kubectl get pods -o wide
```
you should see a pod's name begin with 'cloud-end', like
![Screen Shot 2022-08-24 at 10 35 17 AM](https://user-images.githubusercontent.com/108478119/186485921-372aa9d5-4d3d-4f9d-915e-0719d69ecf9f.png)

To get into the Cloud Worker container, run
```bash
kubectl exec -it [$Container name] sh
```
**Note: In this case, the container name is 'cloud-end-7dd8c48676-ghj25'.**\
\
Now you can check the edge_data.txt to see the heart rate data that you recieved from cloud storage! The expected output should be something like
![Screen Shot 2022-08-24 at 10 32 41 AM](https://user-images.githubusercontent.com/108478119/186485455-a2d220c2-dbb5-4468-a536-9a73b7bf8695.png)
\
**Congratulations! You are able to play with it by your own now!**
