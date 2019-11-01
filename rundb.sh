docker pull couchdb
docker run -d -e COUCHDB_USER=admin -e COUCHDB_PASSWORD=admin --name couchdb -p 5984:5984 couchdb
curl -u admin:admin -X PUT 0.0.0.0:5984/digits_train
curl -u admin:admin -X PUT 0.0.0.0:5984/digits_test
cat ./data/mnist_train.csv | couchimport --url 127.0.0.1:5984 --database "digits_train" --overwrite true
cat ./data/mnist_test.csv | couchimport --url 127.0.0.1:5984 --database "digits_test" --overwrite true
