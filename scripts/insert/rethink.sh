rethinkdb import -f ../../data/mnist_test.csv --format csv --table digits.digits_test
rethinkdb import -f ../../data/mnist_train.csv --format csv --table digits.digits_train
