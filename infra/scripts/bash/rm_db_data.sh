#!/bin/bash

if [ "$EUID" -ne 0 ]; then
  echo "This script must be run with sudo"
  exit 1
fi

if [ -d "../../../local/mysql_data" ]; then
  rm -rf ../../../local/mysql_data
  echo "Local mysql_data directory removed successfully"
else
  echo "Local mysql_data directory not found"
fi

if [ -d "../../../local/mariadb_data" ]; then
  rm -rf ../../../local/mariadb_data
  echo "Local mariadb_data directory removed successfully"
else
  echo "Local mariadb_data directory not found"
fi