@echo off
echo ----- 備份開始 -----
echo ----- 從%date%這天，傳送資料備份到指定的磁碟-----
xcopy/D/S/Y/E/C/I/R/A *.* F:\Android\workspace\soundnet\audio_soundnet
echo ------完成差異備份------


PAUSE