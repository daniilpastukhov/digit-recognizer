mongoimport --host ${MONGODB_IMPORT_URL} --drop --ssl --username ${MONGODB_ADMIN} --password ${MONGODB_PASSWORD} --authenticationDatabase admin --db digits --collection digits_train --type csv --file ../../data/mnist_train.csv --headerline
mongoimport --host ${MONGODB_IMPORT_URL} --drop --ssl --username ${MONGODB_ADMIN} --password ${MONGODB_PASSWORD} --authenticationDatabase admin --db digits --collection digits_test --type csv --file ../../data/mnist_test_halfed.csv --headerline