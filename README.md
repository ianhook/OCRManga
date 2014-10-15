OCRManga
========

Manga reader with OCR to add Kanji learning.

This project is setup with the understanding that [Tesseract](https://code.google.com/p/tesseract-ocr/) and 
[eyes-free](https://code.google.com/p/eyes-free/), which have been wrapped to work in Android in a repo that I've 
forked [here](https://github.com/ianhook/tess-two) are available somewhere.  I've got a bit more work the actual 
project before I clean up the compile libraries.

 Also assume that you've installed the Japanese Langauge file for Tesseract under Context.getExternalFilesDir in a folder called tessdata, which is not possible outside of developer mode I think.
