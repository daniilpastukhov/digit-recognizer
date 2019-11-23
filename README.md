# digit-recognizer
Digit classification program written in Java.

## Project structure

```
.
├── data
│   ├── mnist_test.csv (test dataset)
│   ├── mnist_test_halfed.csv (halfed test dataset for free MongoDB cluster)
│   └── mnist_train.csv (train dataset)
├── javadoc (documentation)
│   └── ...
├── model.ser (serialized trained model)
├── pom.xml
├── scripts
│   ├── insert
│   │   ├── couchdb.sh (import data to local CouchDB database)
│   │   ├── mongo.sh (import data to MongoDB cluster (environment variables are required!)
│   │   └── rethink.sh (import data to local RethinkDB database)
│   ├── rundb.sh (run local CouchDB database as Docker container)
│   └── stopdb.sh (stop local CouchDB database and remove its Docker image)
└── src
    └── main
        ├── java
        │   ├── META-INF
        │   │   └── MANIFEST.MF
        │   └── com
        │       └── daniilpastukhov
        │           ├── Main.java
        │           ├── controllers
        │           │   ├── Controller.java
        │           │   └── package-info.java
        │           ├── database
        │           │   ├── CouchDB.java
        │           │   ├── MongoDB.java
        │           │   ├── NoSqlDatabase.java
        │           │   ├── RethinkDB.java
        │           │   └── package-info.java
        │           ├── models
        │           │   ├── Model.java
        │           │   └── package-info.java
        │           └── package-info.java
        └── resources
            ├── META-INF
            │   └── MANIFEST.MF
            ├── Main.css
            └── ui.fxml
```
