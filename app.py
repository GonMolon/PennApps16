#!/usr/bin/env python
from flask import Flask, jsonify, abort, make_response, request, render_template
from firebase import Firebase
firebase = Firebase('https://vivid-inferno-6896.firebaseio.com', None)
import traceback
import goslate
import json



app = Flask(__name__)
inputMessage = "I'm good, how are you?"
language2 = 'de'
@app.route('/')
def index():
	# return render_template('index.html')
	return "Hello world"

@app.route('/translate', methods=['POST'])
def translate():
	if not request.json or not 'chatId' in request.json:
		abort(400)
	try:
		gs = goslate.Goslate()
		messageList = Firebase('https://vivid-inferno-6896.firebaseio.com/123456789/123456789/messages')

		if request.method == 'POST':
			direct_To = firebase.get('https://vivid-inferno-6896.firebaseio.com/123456789/123456789/messages/0/for')
			print direct_To
			messageList = Firebase('https://vivid-inferno-6896.firebaseio.com/123456789/123456789/messages')
			print "Before" + messageList

			language1 = Firebase('https://vivid-inferno-6896.firebaseio.com/123456789/123456789/language1').get()
			print language1
			language2 = Firebase('https://vivid-inferno-6896.firebaseio.com/123456789/123456789/language2').get()
			print language2
			if gs.detect(inputMessage) == language1: #Confirms language of input text
				outputMessage = gs.translate(inputMessage,language2);
			#Add condition which changes value of "for" field based on last "for" value in previous message dictionary
			messageList.post({"text":"outputMessage","for":"1","read":"false"})
			print "After" + messageList
		if request.method =='GET':

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
