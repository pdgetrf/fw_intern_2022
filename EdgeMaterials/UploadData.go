package main

import (
	"fmt"
	"os/exec"
	"time"
)

func main() {

	up_app  := "aws"
	up_arg0 := "s3"
	up_arg1 := "cp"
	up_arg2 := "edge_data.txt"
	up_arg3 := "s3://fw-yuanjun-test/edge_data.txt"

	for {
		cmd := exec.Command(up_app, up_arg0, up_arg1, up_arg2, up_arg3)
		_, err := cmd.Output()

		if err != nil {
			fmt.Println(err.Error())
		}

		time.Sleep(time.Second * 5)
	}
}
