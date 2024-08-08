#!/bin/sh

apk add --update npm

cd /home/app
npm install
npm start
