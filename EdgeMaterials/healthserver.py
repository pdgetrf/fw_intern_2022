#!/usr/bin/env python3

from http.server import BaseHTTPRequestHandler, HTTPServer
from datetime import datetime
from db_utils import db_connect

con = db_connect()
cur = con.cursor()
sql = """ 
CREATE TABLE IF NOT EXISTS patients (
id integer NOT NULL,
heartrate real NOT NULL,
date text NOT NULL,
time text NOT NULL)"""

cur.execute(sql)

class S(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        pass

    def _set_response(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        self._set_response()

    def do_POST(self):
        content_length = int(self.headers['Content-Length']) 
        post_data = self.rfile.read(content_length) 
        #timezone_Seattle = pytz.timezone('Asia/China')
        #now = datetime.now(timezone_Seattle)
        now = datetime.now()
        current_date = now.strftime("%m/%d/%Y")
        current_time = now.strftime("%H:%M:%S")
        para = post_data.decode('utf-8').split('&')
        rate = float(para[0].split("rate=")[1])
        patient = int(para[1].split("patient=")[1])
        #print(patient, ',', rate, ',', current_date, ',', current_time)
        sql = f"INSERT INTO patients VALUES ('{patient}','{rate}','{current_date}','{current_time}')"
        
        data = str(rate) + ", " + str(current_date) + ", " + str(current_time) + "\n"
        dataFile = open("edge_data.txt", "a")  # append mode
        dataFile.write(data)
        dataFile.close()

        cur.execute(sql)

        self._set_response()

def run(server_class=HTTPServer, handler_class=S, port=6547):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    con.commit()
    con.close()

if __name__ == '__main__':
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()
