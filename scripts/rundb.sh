#!/bin/bash

docker pull couchdb
docker run -d -e COUCHDB_USER=admin -e COUCHDB_PASSWORD=admin --name couchdb -p5984:5984 couchdb

sleep 15

curl -u admin:admin -X PUT http://0.0.0.0:5984/digits_train
curl -u admin:admin -X PUT http://0.0.0.0:5984/digits_test

cat ../data/mnist_train.csv | couchimport --url http://0.0.0.0:5984 --database "digits_train" --overwrite true
cat ../data/mnist_test.csv | couchimport --url http://0.0.0.0:5984 --database "digits_test" --overwrite true