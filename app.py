#!/usr/bin/env python
from flask import Flask, jsonify, abort, make_response, request, render_template
from firebase import Firebase
import traceback
import goslate
import json
import requests
import urllib


app = Flask(__name__)
inputMessage = "I'm good, how are you?"
language2 = 'es'
firebase = Firebase('https://vivid-inferno-6896.firebaseio.com')
payload = {"client_id":"PennApps", "client_secret":"yR8VRcs+MsUPiqt7ee9IipEcoy03Bs35mvZbSGFtZ2o=", "scope":"http://api.microsofttranslator.com", "grant_type":"client_credentials"}
microsoftAuthUrl = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13"


@app.route('/')
def index():
	# return render_template('index.html')
	return "Hello world"

@app.route('/translate', methods=['POST'])
def translate():
	if not request.json:
		abort(400)
	else:
		req = request.json
		if not 'chatId' in req or not 'for' in req or not 'message' in req:
			abort(400)
		else:
			try:
				gs = goslate.Goslate()
				chatObj = Firebase('https://vivid-inferno-6896.firebaseio.com/' + req['chatId'])
				language1 = chatObj.child("language1").get()
				language2 = chatObj.child("language2").get()

				translatedText = ""

				if req['for'] == 1:
					print language1
					# translatedText = gs.translate(req['message'], language1)
					r = requests.post(microsoftAuthUrl, data=payload)
					resJSON = r.json()
					accessToken = resJSON["access_token"]
					translationParams = {"text":req['message'], "from":language2, "to":language1}
					translationUrl = "http://api.microsofttranslator.com/v2/Http.svc/Translate?" + urllib.urlencode(translationParams)
					translationHeader = {'Authorization': "Bearer " + accessToken}
					r = requests.get(translationUrl, headers=translationHeader)
					print r.status_code
					print r.text
				elif req['for'] == 2:
					# translatedText = gs.translate(req['message'], language2) 
					print "Stuff"

				print translatedText
				# direct_To = firebase.get('https://vivid-inferno-6896.firebaseio.com/123456789/123456789/messages/0/for')
				# print direct_To
				# messageList = Firebase('https://vivid-inferno-6896.firebaseio.com/123456789/123456789/messages')
				# print "Before" + messageList

				# language1 = Firebase('https://vivid-inferno-6896.firebaseio.com/123456789/123456789/language1').get()
				# print language1
				# language2 = Firebase('https://vivid-inferno-6896.firebaseio.com/123456789/123456789/language2').get()
				# print language2
				# if gs.detect(inputMessage) == language1: #Confirms language of input text
				# 	outputMessage = gs.translate(inputMessage,language2);
				# #Add condition which changes value of "for" field based on last "for" value in previous message dictionary
				# messageList.post({"text":"outputMessage","for":"1","read":"false"})
				# print "After" + messageList
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
