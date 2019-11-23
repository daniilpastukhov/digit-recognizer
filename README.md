# Digit Recognizer
Digit classification program written in Java (based on MNIST digits dataset).

## Building the project
- Delete all automatically created files:
```sh
mvn clean
```

- Build the project and generate documentation:
```sh
mvn assembly:assembly
```

## Usage
- Jar file is automatically generated in *target/* folder, so you need to run it from there:
```sh
java -jar target/digit-recognizer.jar
```
> There is also already built jar file with the same name located in the root folder.

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
            ├── Main.css (CSS styles)
            └── ui.fxml (JavaFX layout)
```
