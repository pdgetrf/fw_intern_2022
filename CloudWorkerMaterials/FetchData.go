package main

import (
	"fmt"
	"os/exec"
	"time"
)

func main() {
	app := "aws"

	arg0 := "s3"
	arg1 := "cp"
	arg2 := "s3://fw-yuanjun-test/edge_data.txt"
	arg3 := "edge_data.txt"

	for {
		cmd := exec.Command(app, arg0, arg1, arg2, arg3)
		_, err := cmd.Output()

		if err != nil {
			fmt.Println(err.Error())
			return
		}

		cmd2 := exec.Command("echo", "Fetching the data from cloud storage......")
		stdout2, err := cmd2.Output()

		if err != nil {
			fmt.Println(err.Error())
			return
		}

		// Print the output
		fmt.Println(string(stdout2))

		time.Sleep(time.Second * 5)
	}
}
