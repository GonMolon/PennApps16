#!/usr/bin/env python
from flask import Flask, jsonify, abort, make_response, request, render_template
import traceback


app = Flask(__name__)


@app.route('/')
def index():
	# return render_template('index.html')
	return "Hello world"

@app.route('/translate', methods=['POST'])
def translate():
	if not request.json or not 'chatId' in request.json:
		abort(400)
	try:
		chatId = request.json['chatId']
		print chatId
	except Exception, err:
		print traceback.format_exc()
	return jsonify({'result': "success"})


@app.errorhandler(400)
def not_found(error):
	return make_response(jsonify({'error': 'Bad request'}), 400)


@app.errorhandler(404)
def not_found(error):
	return make_response(jsonify({'error': 'Not found'}), 404)


if __name__ == '__main__':
	app.run(debug=True)
