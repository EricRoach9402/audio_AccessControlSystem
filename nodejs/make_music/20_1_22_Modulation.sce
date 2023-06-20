clear
//轉16進制ASCII碼
function y = encode(ch)
    y(1) = floor(ascii(ch)/16);
    y(2) = modulo(ascii(ch),16);
endfunction

y1 = loadwave('D:\xampp\htdocs\sound_networking\nodejs\make_music\nomusic.wav');//要混音的音樂檔
y1_2 = y1(1,:)*0.5+y1(2,:)*0.5;//將雙聲道合為單聲道

txtMsg='DZ';//字串
symbol_size=0.125;//長度
Fs=48000;//取樣率
Ts=1/Fs;//取樣時間或取樣週期

ff = 17500;
fp=20048;//sync頻率
fend=20400;//結束頻率

symbol_N = symbol_size * Fs;//symbol的取樣點數, 訊號個數

//產生sync頻率的波形
for i=1:symbol_N
    chirp(i) = 0.5 * (cos(2 * %pi * fp * i * Ts) + cos(2 * %pi * ff * i * Ts));//產生sync波形
end

//產生結束頻率的波形
for i=0:symbol_N-1
    chirp_end(i+1)=cos(2 * %pi * fend * i * Ts);//產生END波形
end

f0 = 18000;//first frequency
fsk = f0 + 128 * (0:15);//fsk frequency

t = (1:symbol_N) * Ts;

for i = 1:16//fsk
    carrier(i,:) = cos(2*%pi*fsk(i)*t);
end

disp(carrier());

total_symbol = 2 * length(txtMsg) + 1;

for i = 1:length(txtMsg)
    getcode = encode(part(txtMsg, i));
    disp("get code:", i, "index: ");
    for j = 1:2
        tx_code(2*(i-1)+j) = getcode(j);
        disp(getcode(j));//print in dest
    end
end

//tx_code(length(tx_code)+1) = 0;

ham_win = window('hm',symbol_N);
han_win = window('hn',symbol_N);



//產生調變訊號， TxSignal為FSK調變訊號
for i=1:total_symbol
    if i == 1 then
        for j=1:symbol_N
            // sync
            TxSignal(j) = chirp(j) * han_win(j);
        end
    else
        for j=1:symbol_N
            TxSignal(j + symbol_N * (i-1)) = carrier(tx_code(i - 1) + 1, j) * han_win(j);
        end
    end
end
//2023/02/25 新增END參數
for j=1:symbol_N
    // END
    TxSignal(j + symbol_N *total_symbol) = chirp_end(j) * han_win(j);
end

//顯示調變訊號
ds = length(TxSignal);//20/1/19
ttime = Ts*(0:ds-1);
clf(); plot(ttime,TxSignal)//以t為橫座標，txSignal為縱座標，畫圖

SNR=1;

len1 = length(y1_2);

TxSignal_delay=[zeros(1:48000*2) TxSignal'];//延遲2秒"移除TxSignal'符號

//混音
len2 = length(TxSignal_delay);
if len1 > len2 then
    y2 = [TxSignal_delay zeros(1:len1-len2)];
    yout = y1_2 / max(y1_2) + SNR * y2 / max(y2);//2019/2/18
    yout2 = yout/max(yout);//2019/2/19_歸一化
elseif len2 > len1 then
    y1_1 = [y1_2 zeros(1:len2-len1)];
    yout = y1_1 / max(y1_1) + SNR * TxSignal_delay;//2019/2/18
    yout2 = yout/max(yout);//2019/2/19_歸一化
end

//檔名, 內容, 取樣率, 位元深度
//savewave('E:\music'+txtMsg+'_haveEND'+'string(fend)'+'.wav',yout2,Fs,16);//輸出混音音樂檔
savewave('D:\xampp\htdocs\sound_networking\nodejs\music_library\WAV\'+txtMsg+'_'+string(symbol_size)+'_haveEND_'+'string(fend)'+'.wav',TxSignal,Fs,16);//輸出原始音檔
