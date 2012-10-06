#!/bin/sh

tar -zxvf c-ray-1.1.tar.gz

cd c-ray-1.1/
cc -o c-ray-mt c-ray-mt.c -lm -lpthread -O3 $CFLAGS
echo $? > ~/install-exit-status
cd ..

echo "#!/bin/sh
cd c-ray-1.1/
RT_THREADS=\$((\$NUM_CPU_CORES * 16))
./c-ray-mt -t \$RT_THREADS -s 1600x1200 -r 8 -i sphfract -o output.ppm > \$LOG_FILE 2>&1
echo \$? > ~/test-exit-status" > c-ray
chmod +x c-ray
