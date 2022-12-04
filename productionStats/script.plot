set terminal png size 500,500
set output ARG1.'.png'
set datafile separator ','
plot ARG1.'-'.ARG2.'.log' with lines, ARG1.'-'.ARG3.'.log' with lines, ARG1.'-'.ARG4.'.log' with lines
