FROM couchdb:latest

# Import data
COPY ./data/mnist_train.csv ./data/mnist_train.csv
COPY ./data/mnist_test.csv ./data/mnist_test.csv

# Install Node and couchdb
RUN apt-get update
RUN apt-get -y install curl gnupg
RUN curl -sL https://deb.nodesource.com/setup_11.x  | bash -
RUN apt-get -y install nodejs
RUN npm install

# Import data to couchdb
RUN npm install -g couchimport
RUN cat ./data/mnist_train.csv | couchimport --url localhost:5984 --database "digits_train"
RUN cat ./data/mnist_train.csv | couchimport --url localhost:5984 --database "digits_train"
CMD couchdb
