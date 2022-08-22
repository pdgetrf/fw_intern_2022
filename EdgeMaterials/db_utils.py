# db_utils.py
import os
import sqlite3

DEFAULT_PATH = os.path.join(os.path.dirname(__file__), 'database.sqlite3')

def db_connect(db_path=DEFAULT_PATH):
    try:
       con = sqlite3.connect(db_path)
       return con
    except Error:
       print(Error)
