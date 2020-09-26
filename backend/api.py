import os
from flask import Flask, request
from flask_api import status
import sqlite3


app = Flask(__name__)
app.config["DEBUG"] = True

# GET

@app.route('/', methods=['GET'])
def hello_world():
    return 'This is the paperio development server :)'


# POST

@app.route('/coordinates', methods=['POST'])
def coordinates():
    user_id   = request.form['user_id']
    x         = request.form['x']
    y         = request.form['y']
    timestamp = request.form['timestamp']

    query = 'INSERT INTO coordinates (user_id, x, y, timestamp) values (?, ?, ?, ?)'
    db = sqlite3.connect('zoneio.db')
    db.execute(query, (user_id, x, y, timestamp))
    db.commit()

    return 'Coordinates endpoint', status.HTTP_200_OK


@app.route('/login', methods=['POST'])
def login():
    username = request.form['username']
    password = request.form['password']
    error = None
    db = sqlite3.connect('zoneio.db')
    user = db.execute(
        'SELECT * FROM user WHERE username = ?', (username,)
    ).fetchone()

    if user is None:
        error = 'Incorrect username.'
    elif not check_password_hash(user['password'], password):
        error = 'Incorrect password.'

    if error is None:
        session.clear()
        session['user_id'] = user['id']

    return 'Login endpoint', status.HTTP_200_OK

@app.route('/register', methods=['POST'])
def register():
    username = request.form['username']
    password = request.form['password']
    error = None
    db = sqlite3.connect('zoneio.db')
    if not username:
        error = 'Username is required.'
    elif not password:
        error = 'Password is required.'
    elif db.execute(
        'SELECT id FROM users WHERE username = ?', (username,)
    ).fetchone() is not None:
        error = 'User {} is already registered.'.format(username)

    if error is None:
        db.execute(
            'INSERT INTO users (username, password) VALUES (?, ?)',
            (username, password)
        )
        db.commit()
        return 'Register endpoint success'

    return 'Register endpoint', status.HTTP_200_OK


@app.errorhandler(404)
def page_not_found(e):
    return "<h1>404</h1><p>The resource could not be found.</p>", 404

app.run()