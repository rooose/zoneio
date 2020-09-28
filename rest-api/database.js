var sqlite3 = require('sqlite3').verbose()
var md5 = require('md5')

const DBSOURCE = "./zoneio.db"

let db = new sqlite3.Database(DBSOURCE, (err) => {
    if (err) {
      // Cannot open database
      console.error(err.message)
      throw err
    }else{
        console.log('Connected to the SQLite database.')
        // Table USER
        db.run(`CREATE TABLE user (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username text UNIQUE, 
            password text,
            CONSTRAINT username_unique UNIQUE (username)
            )`,
        (err) => {
            if (err) {
                // console.log(err)
            }
        });
        // Table COORDINATE
        db.run(`CREATE TABLE coordinate (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            latitude REAL,
            longitude REAL,
            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY(user_id) REFERENCES user(id)
            )`,
        (err) => {
            if (err) {
                // console.log(err) 
            }
        });  
    }
});


module.exports = db