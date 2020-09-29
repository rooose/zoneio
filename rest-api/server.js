// Create express app
var express = require("express")
var app = express()
var db = require("./database.js")
var md5 = require("md5")

var bodyParser = require("body-parser");
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

var jwt = require('jsonwebtoken');
var config = require('./config');

// Server port
var HTTP_PORT = 8000
// Start server
app.listen(HTTP_PORT, () => {
    console.log("Server running on port %PORT%".replace("%PORT%", HTTP_PORT))
});

// Root endpoint
app.get("/", (req, res, next) => {
    res.json({ "message": "Ok" })
});

app.post("/api/coordinates/", (req, res, next) => {
    var errors = []

    if (!req.body.latitude) {
        errors.push("No latitude provided");
    }
    if (!req.body.longitude) {
        errors.push("No longitude provided");
    }

    var longitude = parseFloat(req.body.longitude)
    var latitude = parseFloat(req.body.latitude)

    if (Number.isNaN(longitude) || Number.isNaN(latitude)) {
        errors.push("Latitude or Longitude contain invalid characters");
    }

    if (errors.length) {
        res.status(400).json({ "error": errors.join(",") });
        return;
    }

    var token = req.headers['x-access-token'];
    if (!token) return res.status(401).send({ auth: false, message: 'No token provided.' });

    jwt.verify(token, config.secret, (err, decoded) => {
        if (err) { return res.status(500).send({ auth: false, message: 'Failed to authenticate token.' }) }

        var data = {
            user_id: decoded.id,
            latitude: req.body.latitude,
            longitude: req.body.longitude
        }

        var sql = 'INSERT INTO coordinate (user_id, latitude, longitude) VALUES (?,?,?)'
        var params = [data.user_id, data.latitude, data.longitude]

        db.run(sql, params, function (err, result) {
            if (err) {
                res.status(400).json({ "error": err.message })
                return;
            }
            res.status(200).json({
                "message": "Coordinates added successfully"
            });
            return;
        });
    });
});


app.post("/api/register/", (req, res, next) => {
    var errors = []

    if (!req.body.password) {
        errors.push("No password specified");
    }
    if (!req.body.username) {
        errors.push("No username specified");
    } else if (!/^[a-z0-9]+$/i.test(req.body.username)) {
        errors.push("Username contains invalid characters");
    }
    if (errors.length) {
        res.status(400).json({ "error": errors.join("\n") });
        return;
    }

    var data = {
        username: req.body.username,
        password: md5(req.body.password)
    }

    // does the user already exist
    var isExistingUserSQL = "select * from user where username = ?"
    var params = [data.username]

    db.get(isExistingUserSQL, params, (err, row) => {
        if (err) {
            res.status(400).json({ "error": err.message });
            return;
        } else if (row) {
            res.status(400).json({ "error": "Username is already in use" });
            return;
        } else {
            // insert the user into the database
            var sql = 'INSERT INTO user (username, password) VALUES (?,?)'
            var params = [data.username, data.password]

            db.run(sql, params, function (err, result) {
                if (err) {
                    res.status(400).json({ "error": err.message })
                    return;
                }

                console.log("REGISTERED USER: ", data.username)
                res.json({
                    "message": "User successfully registered."
                });
                return;
            });
        }
    });
})


app.post("/api/login/", (req, res, next) => {
    var errors = []
    if (!req.body.password) {
        errors.push("No password specified");
    }
    if (!req.body.username) {
        errors.push("No username specified");
    } else if (!/^[a-z0-9]+$/i.test(req.body.username)) {
        errors.push("Username contains invalid characters");
    }
    if (errors.length) {
        res.status(400).json({ "error": errors.join(",") });
        return;
    }

    var data = {
        username: req.body.username,
        password: md5(req.body.password)
    }

    var isExistingUserSQL = "select * from user where username = ? and password = ?"
    var params = [data.username, data.password]

    db.get(isExistingUserSQL, params, (err, row) => {
        if (err) {
            res.status(400).json({ "error": err.message });
            return;
        } else if (!row) {
            res.status(400).json({ "error": "User not found" });
            return;
        } else {
            var token = jwt.sign({ id: row.id }, config.secret, {
                expiresIn: 86400 // expires in 24 hours
            });

            res.json({
                "message": "success",
                "token": token
            })
            return;
        }
    })
});

// Default response for any other request
app.use(function (req, res) {
    res.status(404);
});
