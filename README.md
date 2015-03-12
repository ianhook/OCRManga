OCRManga
========

Manga reader with OCR to add Kanji learning.

This project includes source from [Tesseract](https://code.google.com/p/tesseract-ocr/) and [eyes-free](https://code.google.com/p/eyes-free/), which have been wrapped to work in Android in a repo that I've forked [here](https://github.com/ianhook/tess-two).  Because I've just switched to gradle, I've included the full source of these projects here.  I've got a bit more work the actual project before I clean up the compile libraries.

Additionally, the machine learning portion of the code uses [JGAP](http://jgap.sourceforge.net/) to power the genentic algorithm.  To use this code you must change the value of com.ianhook.android.ocromanga.util.OcrGeneticDetection:mDoGA to true;

 Also assume that you've installed the Japanese Langauge file for Tesseract under Context.getExternalFilesDir in a folder called tessdata, which is not possible outside of developer mode I think.
